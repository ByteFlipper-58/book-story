/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.domain.use_case.translation

import android.util.Log
import com.byteflipper.everbook.domain.repository.BookTranslationRepository
import com.byteflipper.everbook.domain.repository.TranslationRepository
import com.byteflipper.everbook.domain.translation.BookTranslation
import com.byteflipper.everbook.domain.translation.BookTranslationEntry
import com.byteflipper.everbook.domain.translation.BookTranslationStatus
import com.byteflipper.everbook.domain.translation.BOOK_TRANSLATION_TEXT_CHANGED_MESSAGE
import com.byteflipper.everbook.domain.translation.BookTranslationRescheduleException
import com.byteflipper.everbook.domain.translation.TranslationException
import com.byteflipper.everbook.domain.translation.TranslationProviderMode
import com.byteflipper.everbook.domain.translation.TranslationRateLimitedException
import com.byteflipper.everbook.domain.translation.TranslationRequest
import com.byteflipper.everbook.domain.use_case.book.GetBookById
import com.byteflipper.everbook.domain.use_case.book.GetText
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import kotlin.coroutines.coroutineContext
import kotlin.math.pow

private const val GOOGLE_MAX_ATTEMPTS = 4
private const val DEFAULT_MAX_ATTEMPTS = 2
private const val MAX_UNIT_FAILURE_LOGS = 5
// Rate-limit auto-recovery: when the provider keeps returning 429, the executor waits and retries
// the same batch (escalating backoff). If the *consecutive* waiting exceeds the per-run budget, it
// stops gracefully and asks the worker to reschedule via WorkManager instead of failing.
private const val RATE_LIMIT_BASE_BACKOFF_MS = 4_000L
private const val RATE_LIMIT_MAX_BACKOFF_MS = 60_000L
private const val RATE_LIMIT_RUN_BUDGET_MS = 6 * 60 * 1_000L
private const val ML_KIT_LIVE_BATCH_MAX_CHARS = 800
private const val ML_KIT_LIVE_BATCH_MAX_PIECES = 3
private const val ML_KIT_BACKGROUND_BATCH_MAX_CHARS = 4_500
private const val ML_KIT_BACKGROUND_BATCH_MAX_PIECES = 24
private const val ML_KIT_MAX_PIECE_CHARS = 800
private const val GOOGLE_LIVE_BATCH_MAX_CHARS = 700
private const val GOOGLE_LIVE_BATCH_MAX_PIECES = 3
private const val GOOGLE_BACKGROUND_BATCH_MAX_CHARS = 3_200
private const val GOOGLE_BACKGROUND_BATCH_MAX_PIECES = 12
private const val GOOGLE_MAX_PIECE_CHARS = 700
private const val LIVE_FORWARD_UNITS = 60
private const val LIVE_BACKWARD_UNITS = 12
private const val BATCH_MARKER_PREFIX = "<<<EVERBOOK:"
private const val BATCH_MARKER_END = ">>>"
private const val BOOK_TRANSLATION_LOG = "BookTranslation"

class BookTranslationExecutor @Inject constructor(
    private val bookTranslationRepository: BookTranslationRepository,
    private val translationRepository: TranslationRepository,
    private val getBookById: GetBookById,
    private val getText: GetText,
    private val planner: BookTranslationPlanner
) {
    suspend fun execute(translationId: Long): BookTranslation = withContext(Dispatchers.IO) {
        var current = bookTranslationRepository.getTranslation(translationId)
            ?: throw TranslationException("Book translation was not found.")

        Log.i(
            BOOK_TRANSLATION_LOG,
            "Executor started: translationId=$translationId bookId=${current.bookId} " +
                    "status=${current.status} provider=${current.providerMode} " +
                    "source=${current.sourceLanguageCode} target=${current.targetLanguageCode}"
        )

        if (
            current.status == BookTranslationStatus.CANCELLED ||
            current.status == BookTranslationStatus.PAUSED
        ) {
            Log.i(
                BOOK_TRANSLATION_LOG,
                "Executor skipped inactive translation: translationId=$translationId " +
                        "status=${current.status}"
            )
            return@withContext current
        }
        if (current.status == BookTranslationStatus.COMPLETED) {
            Log.i(BOOK_TRANSLATION_LOG, "Executor skipped completed translation: translationId=$translationId")
            return@withContext current
        }

        val text = getText.execute(current.bookId)
        if (text.isEmpty()) {
            Log.w(BOOK_TRANSLATION_LOG, "Executor found no text: translationId=$translationId")
            return@withContext fail(
                translation = current,
                message = "This book has no text to translate."
            )
        }

        val sourceFingerprint = planner.fingerprint(text)
        if (sourceFingerprint != current.sourceFingerprint) {
            Log.w(
                BOOK_TRANSLATION_LOG,
                "Executor marked stale: translationId=$translationId bookId=${current.bookId}"
            )
            current = current.copy(
                status = BookTranslationStatus.STALE,
                errorMessage = BOOK_TRANSLATION_TEXT_CHANGED_MESSAGE,
                updatedAt = System.currentTimeMillis()
            )
            bookTranslationRepository.updateTranslation(current)
            return@withContext current
        }

        val coercedProviderMode = current.providerMode
        if (!translationRepository.capability.isAvailable(coercedProviderMode)) {
            Log.w(
                BOOK_TRANSLATION_LOG,
                "Executor provider unavailable: translationId=$translationId provider=$coercedProviderMode"
            )
            return@withContext fail(
                translation = current,
                message = "Selected translation provider is unavailable."
            )
        }

        val units = planner.buildUnits(text)
        if (units.isEmpty()) {
            Log.w(BOOK_TRANSLATION_LOG, "Executor built no units: translationId=$translationId")
            return@withContext fail(
                translation = current,
                message = "This book has no text to translate."
            )
        }

        val existingEntries = bookTranslationRepository.getEntries(current.id)
        val translatedIndexes = existingEntries.mapTo(mutableSetOf()) { it.readerTextIndex }
        var completedUnits = translatedIndexes.size.coerceAtMost(units.size)
        var failedUnits = 0
        var loggedUnitFailures = 0
        var firstDetectedSource = current.detectedSourceLanguageCode
        val limits = limitsFor(current.providerMode)
        val currentReaderIndex = getBookById.execute(current.bookId)
            ?.scrollIndex
            ?.coerceAtLeast(0)
            ?: 0
        val pendingWorks = units
            .filterNot { it.readerTextIndex in translatedIndexes }
            .map { planner.buildPieces(it, limits.maxPieceChars) }
            .filter { it.pieces.isNotEmpty() }
        val pendingPlan = prioritizePendingWorks(
            works = pendingWorks,
            anchorIndex = currentReaderIndex
        )
        val batches = buildBatches(
            liveWorks = pendingPlan.liveWorks,
            backgroundWorks = pendingPlan.backgroundWorks,
            limits = limits
        )
        val translatedPiecesByUnit = mutableMapOf<Int, MutableMap<Int, String>>()
        val failedUnitIndexes = mutableSetOf<Int>()
        // Consecutive time spent waiting out rate limits; resets after any non-limited batch.
        var consecutiveRateLimitWaitMs = 0L
        var rateLimitAttempt = 0
        Log.i(
            BOOK_TRANSLATION_LOG,
            "Executor prepared units: translationId=$translationId totalUnits=${units.size} " +
                    "alreadyTranslated=$completedUnits pendingUnits=${pendingWorks.size} " +
                    "pendingPieces=${pendingWorks.sumOf { it.pieces.size }} batches=${batches.size} " +
                    "liveUnits=${pendingPlan.liveWorks.size} backgroundUnits=${pendingPlan.backgroundWorks.size} " +
                    "anchor=$currentReaderIndex provider=${current.providerMode}"
        )

        current = current.copy(
            status = BookTranslationStatus.RUNNING,
            totalUnits = units.size,
            completedUnits = completedUnits,
            failedUnits = 0,
            errorMessage = null,
            startedAt = current.startedAt ?: System.currentTimeMillis(),
            lastAttemptAt = System.currentTimeMillis(),
            retryCount = if (current.status == BookTranslationStatus.FAILED) {
                current.retryCount + 1
            } else current.retryCount,
            updatedAt = System.currentTimeMillis(),
            completedAt = null
        )
        bookTranslationRepository.updateTranslation(current)
        Log.i(
            BOOK_TRANSLATION_LOG,
            "Executor running: translationId=$translationId totalUnits=${current.totalUnits}"
        )

        try {
            batches.forEachIndexed { batchIndex, batch ->
                coroutineContext.ensureActive()

                val latest = bookTranslationRepository.getTranslation(current.id)
                if (
                    latest?.status == BookTranslationStatus.CANCELLED ||
                    latest?.status == BookTranslationStatus.PAUSED
                ) {
                    Log.i(
                        BOOK_TRANSLATION_LOG,
                        "Executor detected manual stop: translationId=$translationId " +
                                "status=${latest.status} " +
                                "completed=$completedUnits failed=$failedUnits"
                    )
                    return@withContext latest
                }

                val activeBatch = batch
                    .filterNot { it.unit.readerTextIndex in translatedIndexes }
                    .filterNot { it.unit.readerTextIndex in failedUnitIndexes }
                if (activeBatch.isEmpty()) return@forEachIndexed

                var result = translatePiecesWithFallback(
                    translation = current.forTranslationRequest(firstDetectedSource),
                    pieces = activeBatch
                )
                // Auto-recover from rate limits: wait (honoring Retry-After) and retry the SAME
                // batch. Bail to a WorkManager reschedule only if the provider keeps limiting past
                // this run's budget — the translation is never marked FAILED for a transient 429.
                while (result.rateLimitException != null) {
                    val duringWait = bookTranslationRepository.getTranslation(current.id)
                    if (
                        duringWait?.status == BookTranslationStatus.PAUSED ||
                        duringWait?.status == BookTranslationStatus.CANCELLED
                    ) {
                        return@withContext duringWait
                    }

                    val waitMs = result.rateLimitException?.retryAfterMs
                        ?: rateLimitBackoffMs(rateLimitAttempt)
                    if (consecutiveRateLimitWaitMs + waitMs > RATE_LIMIT_RUN_BUDGET_MS) {
                        current = current.copy(
                            status = BookTranslationStatus.QUEUED,
                            errorMessage = "Translation paused by the provider's rate limit. " +
                                    "It will continue automatically.",
                            completedUnits = completedUnits,
                            failedUnits = failedUnits,
                            queuedAt = System.currentTimeMillis(),
                            updatedAt = System.currentTimeMillis()
                        )
                        bookTranslationRepository.updateTranslation(current)
                        Log.w(
                            BOOK_TRANSLATION_LOG,
                            "Executor rescheduling after sustained rate limit: " +
                                    "translationId=$translationId completed=$completedUnits/${units.size} " +
                                    "waitedMs=$consecutiveRateLimitWaitMs"
                        )
                        throw BookTranslationRescheduleException(
                            result.rateLimitException?.message ?: "Translation provider rate limit reached."
                        )
                    }

                    Log.w(
                        BOOK_TRANSLATION_LOG,
                        "Executor waiting out rate limit: translationId=$translationId " +
                                "batch=${batchIndex + 1}/${batches.size} waitMs=$waitMs " +
                                "attempt=$rateLimitAttempt"
                    )
                    delay(waitMs)
                    consecutiveRateLimitWaitMs += waitMs
                    rateLimitAttempt++
                    result = translatePiecesWithFallback(
                        translation = current.forTranslationRequest(firstDetectedSource),
                        pieces = activeBatch
                    )
                }
                consecutiveRateLimitWaitMs = 0
                rateLimitAttempt = 0
                firstDetectedSource = firstDetectedSource ?: result.detectedSourceLanguageCode

                result.translations.forEach { (piece, translatedText) ->
                    translatedPiecesByUnit
                        .getOrPut(piece.unit.readerTextIndex) { mutableMapOf() }[piece.pieceIndex] =
                        translatedText
                }

                result.failures.forEach { failure ->
                    val unit = failure.piece.unit
                    if (failedUnitIndexes.add(unit.readerTextIndex)) {
                        failedUnits++
                        if (loggedUnitFailures < MAX_UNIT_FAILURE_LOGS) {
                            loggedUnitFailures++
                            Log.w(
                                BOOK_TRANSLATION_LOG,
                                "Executor unit failed: translationId=$translationId " +
                                        "readerTextIndex=${unit.readerTextIndex} " +
                                        "pieceIndex=${failure.piece.pieceIndex} " +
                                        "provider=${current.providerMode}",
                                failure.throwable
                            )
                        } else if (loggedUnitFailures == MAX_UNIT_FAILURE_LOGS) {
                            loggedUnitFailures++
                            Log.w(
                                BOOK_TRANSLATION_LOG,
                                "Executor suppressing further unit failure logs: " +
                                        "translationId=$translationId"
                            )
                        }
                    }
                }

                val completedEntries = pendingWorks
                    .filterNot { it.unit.readerTextIndex in translatedIndexes }
                    .filterNot { it.unit.readerTextIndex in failedUnitIndexes }
                    .mapNotNull { work ->
                        work.toCompletedEntryOrNull(
                            translation = current,
                            translatedPieces = translatedPiecesByUnit[work.unit.readerTextIndex],
                            detectedSourceLanguageCode = firstDetectedSource
                        )
                    }

                if (completedEntries.isNotEmpty()) {
                    bookTranslationRepository.upsertEntries(completedEntries)
                    completedEntries.forEach { entry ->
                        translatedIndexes += entry.readerTextIndex
                    }
                    completedUnits = translatedIndexes.size.coerceAtMost(units.size)
                }

                current = current.copy(
                    detectedSourceLanguageCode = firstDetectedSource,
                    completedUnits = completedUnits,
                    failedUnits = failedUnits,
                    updatedAt = System.currentTimeMillis()
                )
                // Progress-only write: never persist `status` here, otherwise a pause/cancel
                // issued mid-batch (after the line-192 check) would be clobbered back to RUNNING.
                bookTranslationRepository.updateProgress(
                    translationId = current.id,
                    completedUnits = completedUnits,
                    failedUnits = failedUnits,
                    detectedSourceLanguageCode = firstDetectedSource
                )

                val number = batchIndex + 1
                if (
                    number == 1 ||
                    number == batches.size ||
                    number % 10 == 0 ||
                    result.failures.isNotEmpty()
                ) {
                    Log.i(
                        BOOK_TRANSLATION_LOG,
                        "Executor batch processed: translationId=$translationId " +
                                "batch=$number/${batches.size} pieces=${activeBatch.size} " +
                                "completed=$completedUnits/${units.size} failed=$failedUnits"
                    )
                }
            }

            // A pause/cancel may have landed during the final batch (after its line-192 check).
            // Re-read before finalizing so we never overwrite PAUSED/CANCELLED with COMPLETED/FAILED.
            val latestBeforeFinish = bookTranslationRepository.getTranslation(current.id)
            if (
                latestBeforeFinish != null &&
                (
                    latestBeforeFinish.status == BookTranslationStatus.PAUSED ||
                    latestBeforeFinish.status == BookTranslationStatus.CANCELLED
                )
            ) {
                Log.i(
                    BOOK_TRANSLATION_LOG,
                    "Executor honoring manual stop before finalize: translationId=$translationId " +
                            "status=${latestBeforeFinish.status} " +
                            "completed=$completedUnits failed=$failedUnits"
                )
                return@withContext latestBeforeFinish
            }

            val completedAt = System.currentTimeMillis()
            val unresolvedUnits = (units.size - completedUnits - failedUnits).coerceAtLeast(0)
            val finalFailedUnits = failedUnits + unresolvedUnits
            current = current.copy(
                status = if (finalFailedUnits == 0) {
                    BookTranslationStatus.COMPLETED
                } else BookTranslationStatus.FAILED,
                errorMessage = if (finalFailedUnits == 0) {
                    null
                } else "Could not translate $finalFailedUnits text items.",
                completedUnits = completedUnits,
                failedUnits = finalFailedUnits,
                updatedAt = completedAt,
                completedAt = completedAt
            )
            bookTranslationRepository.updateTranslation(current)
            Log.i(
                BOOK_TRANSLATION_LOG,
                "Executor finished: translationId=$translationId status=${current.status} " +
                        "completed=${current.completedUnits}/${current.totalUnits} failed=${current.failedUnits}"
            )
            current
        } catch (exception: CancellationException) {
            val latest = withContext(NonCancellable) {
                bookTranslationRepository.getTranslation(current.id)
            }
            val manualStatus = latest?.status?.takeIf {
                it == BookTranslationStatus.PAUSED ||
                        it == BookTranslationStatus.CANCELLED
            }
            Log.i(
                BOOK_TRANSLATION_LOG,
                "Executor cancelled by coroutine: translationId=$translationId " +
                        "completed=$completedUnits failed=$failedUnits " +
                        "manualStatus=$manualStatus"
            )
            current = if (manualStatus != null) {
                latest.copy(updatedAt = System.currentTimeMillis())
            } else {
                current.copy(
                    status = BookTranslationStatus.CANCELLED,
                    updatedAt = System.currentTimeMillis()
                )
            }
            withContext(NonCancellable) {
                bookTranslationRepository.updateTranslation(current)
            }
            throw exception
        } catch (exception: BookTranslationRescheduleException) {
            // Not a failure — progress is already persisted and status set to QUEUED. Propagate so
            // the worker reschedules the run via WorkManager.
            throw exception
        } catch (exception: TranslationRateLimitedException) {
            Log.w(
                BOOK_TRANSLATION_LOG,
                "Executor stopped by rate limit: translationId=$translationId " +
                        "completed=$completedUnits failed=$failedUnits",
                exception
            )
            fail(
                translation = current.copy(
                    detectedSourceLanguageCode = firstDetectedSource,
                    completedUnits = completedUnits,
                    failedUnits = failedUnits,
                    updatedAt = System.currentTimeMillis()
                ),
                message = exception.message
                    ?: "Translation provider rate limit reached. Try again later."
            )
        } catch (exception: Exception) {
            Log.e(BOOK_TRANSLATION_LOG, "Executor failed: translationId=$translationId", exception)
            fail(
                translation = current,
                message = exception.message ?: "Could not translate the book."
            )
        }
    }

    private fun limitsFor(providerMode: TranslationProviderMode): BatchLimits =
        when (providerMode) {
            TranslationProviderMode.GOOGLE_TRANSLATE -> BatchLimits(
                live = BatchProfile(
                    maxChars = GOOGLE_LIVE_BATCH_MAX_CHARS,
                    maxPieces = GOOGLE_LIVE_BATCH_MAX_PIECES
                ),
                background = BatchProfile(
                    maxChars = GOOGLE_BACKGROUND_BATCH_MAX_CHARS,
                    maxPieces = GOOGLE_BACKGROUND_BATCH_MAX_PIECES
                ),
                maxPieceChars = GOOGLE_MAX_PIECE_CHARS
            )

            TranslationProviderMode.IN_APP,
            TranslationProviderMode.EXTERNAL -> BatchLimits(
                live = BatchProfile(
                    maxChars = ML_KIT_LIVE_BATCH_MAX_CHARS,
                    maxPieces = ML_KIT_LIVE_BATCH_MAX_PIECES
                ),
                background = BatchProfile(
                    maxChars = ML_KIT_BACKGROUND_BATCH_MAX_CHARS,
                    maxPieces = ML_KIT_BACKGROUND_BATCH_MAX_PIECES
                ),
                maxPieceChars = ML_KIT_MAX_PIECE_CHARS
            )
        }

    private fun BookTranslation.forTranslationRequest(
        detectedSourceLanguageCode: String?
    ): BookTranslation =
        if (sourceLanguageCode == null && detectedSourceLanguageCode != null) {
            copy(sourceLanguageCode = detectedSourceLanguageCode)
        } else this

    private fun prioritizePendingWorks(
        works: List<BookTranslationUnitWork>,
        anchorIndex: Int
    ): PendingWorkPlan {
        val liveAfter = works
            .filter { it.unit.readerTextIndex in anchorIndex..anchorIndex + LIVE_FORWARD_UNITS }
            .sortedBy { it.unit.readerTextIndex }
        val liveBefore = works
            .filter {
                it.unit.readerTextIndex < anchorIndex &&
                        it.unit.readerTextIndex >= anchorIndex - LIVE_BACKWARD_UNITS
            }
            .sortedByDescending { it.unit.readerTextIndex }
        val liveIndexes = (liveAfter + liveBefore)
            .mapTo(mutableSetOf()) { it.unit.readerTextIndex }
        val backgroundAfter = works
            .filter { it.unit.readerTextIndex > anchorIndex + LIVE_FORWARD_UNITS }
            .sortedBy { it.unit.readerTextIndex }
        val backgroundBefore = works
            .filter {
                it.unit.readerTextIndex < anchorIndex - LIVE_BACKWARD_UNITS &&
                        it.unit.readerTextIndex !in liveIndexes
            }
            .sortedBy { it.unit.readerTextIndex }

        return PendingWorkPlan(
            liveWorks = liveAfter + liveBefore,
            backgroundWorks = backgroundAfter + backgroundBefore
        )
    }

    private fun buildBatches(
        liveWorks: List<BookTranslationUnitWork>,
        backgroundWorks: List<BookTranslationUnitWork>,
        limits: BatchLimits
    ): List<List<BookTranslationPiece>> =
        buildBatches(
            works = liveWorks,
            profile = limits.live
        ) + buildBatches(
            works = backgroundWorks,
            profile = limits.background
        )

    private fun buildBatches(
        works: List<BookTranslationUnitWork>,
        profile: BatchProfile
    ): List<List<BookTranslationPiece>> {
        val batches = mutableListOf<List<BookTranslationPiece>>()
        val current = mutableListOf<BookTranslationPiece>()
        var currentChars = 0

        fun flush() {
            if (current.isNotEmpty()) {
                batches += current.toList()
                current.clear()
                currentChars = 0
            }
        }

        works.flatMap { it.pieces }.forEach { piece ->
            if (piece.text.contains(BATCH_MARKER_PREFIX)) {
                flush()
                batches += listOf(piece)
                return@forEach
            }

            val pieceChars = piece.text.length + markerOverheadChars()
            val wouldOverflow = current.isNotEmpty() &&
                    (current.size >= profile.maxPieces || currentChars + pieceChars > profile.maxChars)
            if (wouldOverflow) flush()

            current += piece
            currentChars += pieceChars
        }
        flush()
        return batches
    }

    private suspend fun translatePiecesWithFallback(
        translation: BookTranslation,
        pieces: List<BookTranslationPiece>
    ): PieceTranslationResult {
        if (pieces.isEmpty()) return PieceTranslationResult()
        if (pieces.size == 1) return translateSinglePiece(translation, pieces.single())

        if (pieces.any { it.text.contains(BATCH_MARKER_PREFIX) }) {
            return splitAndTranslatePieces(translation, pieces, null)
        }

        return try {
            translateMarkedBatch(translation, pieces)
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: TranslationRateLimitedException) {
            PieceTranslationResult(rateLimitException = exception)
        } catch (throwable: Throwable) {
            splitAndTranslatePieces(translation, pieces, throwable)
        }
    }

    private suspend fun splitAndTranslatePieces(
        translation: BookTranslation,
        pieces: List<BookTranslationPiece>,
        cause: Throwable?
    ): PieceTranslationResult {
        if (pieces.size == 1) return translateSinglePiece(translation, pieces.single())

        Log.w(
            BOOK_TRANSLATION_LOG,
            "Translation batch fallback split: translationId=${translation.id} " +
                    "pieces=${pieces.size} provider=${translation.providerMode}",
            cause
        )

        val splitIndex = pieces.size / 2
        val left = translatePiecesWithFallback(translation, pieces.take(splitIndex))
        if (left.rateLimitException != null) return left
        val right = translatePiecesWithFallback(translation, pieces.drop(splitIndex))
        return left + right
    }

    private suspend fun translateMarkedBatch(
        translation: BookTranslation,
        pieces: List<BookTranslationPiece>
    ): PieceTranslationResult {
        val nonce = UUID.randomUUID().toString().replace("-", "")
        val result = translateWithBackoff(
            translation = translation,
            chunk = buildMarkedBatchText(nonce, pieces)
        )
        return PieceTranslationResult(
            translations = parseMarkedBatch(
                translatedText = result.translatedText,
                nonce = nonce,
                pieces = pieces
            ),
            detectedSourceLanguageCode = result.sourceLanguageCode
        )
    }

    private suspend fun translateSinglePiece(
        translation: BookTranslation,
        piece: BookTranslationPiece
    ): PieceTranslationResult =
        try {
            val result = translateWithBackoff(
                translation = translation,
                chunk = piece.text
            )
            val translatedText = result.translatedText.trim()
                .takeIf { it.isNotBlank() }
                ?: throw TranslationException("Translation provider returned an empty text.")
            PieceTranslationResult(
                translations = mapOf(piece to translatedText),
                detectedSourceLanguageCode = result.sourceLanguageCode
            )
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: TranslationRateLimitedException) {
            PieceTranslationResult(rateLimitException = exception)
        } catch (throwable: Throwable) {
            PieceTranslationResult(
                failures = listOf(PieceTranslationFailure(piece, throwable))
            )
        }

    private fun buildMarkedBatchText(
        nonce: String,
        pieces: List<BookTranslationPiece>
    ): String =
        pieces.mapIndexed { index, piece ->
            val markerId = markerId(index)
            "${startMarker(nonce, markerId)}\n${piece.text}\n${endMarker(nonce, markerId)}"
        }.joinToString("\n")

    private fun parseMarkedBatch(
        translatedText: String,
        nonce: String,
        pieces: List<BookTranslationPiece>
    ): Map<BookTranslationPiece, String> {
        val translations = linkedMapOf<BookTranslationPiece, String>()
        var cursor = 0

        // Integrity check first: every start/end marker must appear exactly once, and start markers
        // must be in ascending (piece) order. If the provider dropped, duplicated, or reordered a
        // marker, the forward-cursor parse below could silently map text to the wrong piece — bail to
        // the unambiguous single-piece fallback instead.
        var previousStart = -1
        pieces.forEachIndexed { index, _ ->
            val markerId = markerId(index)
            val startMarker = startMarker(nonce, markerId)
            val endMarker = endMarker(nonce, markerId)

            val firstStart = translatedText.indexOf(startMarker)
            if (firstStart < 0 || translatedText.indexOf(startMarker, firstStart + 1) >= 0) {
                throw BatchMarkerException("Translation batch start marker missing or duplicated.")
            }
            val firstEnd = translatedText.indexOf(endMarker)
            if (firstEnd < 0 || translatedText.indexOf(endMarker, firstEnd + 1) >= 0) {
                throw BatchMarkerException("Translation batch end marker missing or duplicated.")
            }
            if (firstStart <= previousStart) {
                throw BatchMarkerException("Translation batch markers are out of order.")
            }
            previousStart = firstStart
        }

        pieces.forEachIndexed { index, piece ->
            val markerId = markerId(index)
            val startMarker = startMarker(nonce, markerId)
            val endMarker = endMarker(nonce, markerId)
            val startIndex = translatedText.indexOf(startMarker, cursor)
            if (startIndex < 0) {
                throw BatchMarkerException("Translation batch start marker was changed.")
            }

            val textStart = startIndex + startMarker.length
            val endIndex = translatedText.indexOf(endMarker, textStart)
            if (endIndex < 0) {
                throw BatchMarkerException("Translation batch end marker was changed.")
            }

            val value = translatedText.substring(textStart, endIndex).trim()
                .takeIf { it.isNotBlank() }
                ?: throw BatchMarkerException("Translation batch item was empty.")
            translations[piece] = value
            cursor = endIndex + endMarker.length
        }

        return translations
    }

    private fun BookTranslationUnitWork.toCompletedEntryOrNull(
        translation: BookTranslation,
        translatedPieces: Map<Int, String>?,
        detectedSourceLanguageCode: String?
    ): BookTranslationEntry? {
        if (translatedPieces == null || translatedPieces.size != pieces.size) return null
        val translatedText = pieces
            .mapNotNull { piece -> translatedPieces[piece.pieceIndex]?.trim() }
            .joinToString(" ")
            .trim()
            .takeIf { it.isNotBlank() }
            ?: return null
        val now = System.currentTimeMillis()
        return BookTranslationEntry(
            translationId = translation.id,
            readerTextIndex = unit.readerTextIndex,
            type = unit.type,
            originalText = unit.text,
            translatedText = translatedText,
            sourceLanguageCode = detectedSourceLanguageCode ?: translation.sourceLanguageCode,
            targetLanguageCode = translation.targetLanguageCode,
            updatedAt = now
        )
    }

    private suspend fun translateWithBackoff(
        translation: BookTranslation,
        chunk: String
    ) = retry(
        attempts = if (translation.providerMode == TranslationProviderMode.GOOGLE_TRANSLATE) {
            GOOGLE_MAX_ATTEMPTS
        } else DEFAULT_MAX_ATTEMPTS,
        baseDelayMs = if (translation.providerMode == TranslationProviderMode.GOOGLE_TRANSLATE) {
            900L
        } else 250L
    ) {
        translationRepository.translate(
            TranslationRequest(
                text = chunk,
                sourceLanguageCode = translation.sourceLanguageCode,
                targetLanguageCode = translation.targetLanguageCode,
                requireWifi = translation.requireWifi,
                providerMode = translation.providerMode
            )
        )
    }

    /** Escalating backoff between same-batch retries while the provider is rate-limiting. */
    private fun rateLimitBackoffMs(attempt: Int): Long =
        (RATE_LIMIT_BASE_BACKOFF_MS shl attempt.coerceIn(0, 5))
            .coerceAtMost(RATE_LIMIT_MAX_BACKOFF_MS)

    private fun markerId(index: Int): String =
        (index + 1).toString().padStart(4, '0')

    private fun startMarker(nonce: String, markerId: String): String =
        "$BATCH_MARKER_PREFIX$nonce:0:$markerId$BATCH_MARKER_END"

    private fun endMarker(nonce: String, markerId: String): String =
        "$BATCH_MARKER_PREFIX$nonce:1:$markerId$BATCH_MARKER_END"

    private fun markerOverheadChars(): Int =
        128

    private suspend fun <T> retry(
        attempts: Int,
        baseDelayMs: Long,
        block: suspend () -> T
    ): T {
        var lastError: Throwable? = null
        repeat(attempts) { index ->
            try {
                return block()
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: TranslationRateLimitedException) {
                throw exception
            } catch (throwable: Throwable) {
                lastError = throwable
                if (index < attempts - 1) {
                    delay((baseDelayMs * 2.0.pow(index)).toLong())
                }
            }
        }
        throw lastError ?: TranslationException("Could not translate text.")
    }

    private suspend fun fail(
        translation: BookTranslation,
        message: String
    ): BookTranslation {
        Log.w(
            BOOK_TRANSLATION_LOG,
            "Translation failed: translationId=${translation.id} message=$message"
        )
        val failed = translation.copy(
            status = BookTranslationStatus.FAILED,
            errorMessage = message,
            lastAttemptAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        bookTranslationRepository.updateTranslation(failed)
        return failed
    }
}

private data class BatchLimits(
    val live: BatchProfile,
    val background: BatchProfile,
    val maxPieceChars: Int
)

private data class BatchProfile(
    val maxChars: Int,
    val maxPieces: Int
)

private data class PendingWorkPlan(
    val liveWorks: List<BookTranslationUnitWork>,
    val backgroundWorks: List<BookTranslationUnitWork>
)

private data class PieceTranslationResult(
    val translations: Map<BookTranslationPiece, String> = emptyMap(),
    val failures: List<PieceTranslationFailure> = emptyList(),
    val detectedSourceLanguageCode: String? = null,
    val rateLimitException: TranslationRateLimitedException? = null
) {
    operator fun plus(other: PieceTranslationResult): PieceTranslationResult =
        PieceTranslationResult(
            translations = translations + other.translations,
            failures = failures + other.failures,
            detectedSourceLanguageCode = detectedSourceLanguageCode
                ?: other.detectedSourceLanguageCode,
            rateLimitException = rateLimitException ?: other.rateLimitException
        )
}

private data class PieceTranslationFailure(
    val piece: BookTranslationPiece,
    val throwable: Throwable
)

private class BatchMarkerException(message: String) : TranslationException(message)
