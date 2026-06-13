/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.domain.use_case.translation

import android.util.Log
import com.byteflipper.everbook.domain.reader.ReaderText
import com.byteflipper.everbook.domain.repository.BookTranslationRepository
import com.byteflipper.everbook.domain.repository.BookTranslationWorkScheduler
import com.byteflipper.everbook.domain.repository.TranslationRepository
import com.byteflipper.everbook.domain.translation.AUTO_TRANSLATION_LANGUAGE
import com.byteflipper.everbook.domain.translation.BookTranslation
import com.byteflipper.everbook.domain.translation.BookTranslationStatus
import com.byteflipper.everbook.domain.translation.FALLBACK_TRANSLATION_TARGET_LANGUAGE
import com.byteflipper.everbook.domain.translation.TranslationException
import com.byteflipper.everbook.domain.translation.TranslationFeature
import com.byteflipper.everbook.domain.translation.TranslationProviderMode
import com.byteflipper.everbook.domain.translation.normalizeTranslationLanguageCode
import com.byteflipper.everbook.domain.translation.resolveTranslationLanguageCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

private const val BOOK_TRANSLATION_LOG = "BookTranslation"

class EnqueueBookTranslation @Inject constructor(
    private val bookTranslationRepository: BookTranslationRepository,
    private val translationRepository: TranslationRepository,
    private val workScheduler: BookTranslationWorkScheduler,
    private val planner: BookTranslationPlanner
) {
    suspend fun execute(
        bookId: Int,
        text: List<ReaderText>,
        sourceLanguageCode: String,
        targetLanguageCode: String,
        providerMode: TranslationProviderMode,
        requireWifi: Boolean
    ): BookTranslation = withContext(Dispatchers.IO) {
        val coercedProviderMode = TranslationFeature.coerceFullBookProviderMode(providerMode)
        Log.i(
            BOOK_TRANSLATION_LOG,
            "UseCase enqueue requested: bookId=$bookId textItems=${text.size} " +
                    "provider=$coercedProviderMode requestedProvider=$providerMode " +
                    "source=$sourceLanguageCode target=$targetLanguageCode wifiOnly=$requireWifi"
        )
        if (!translationRepository.capability.isAvailable(coercedProviderMode)) {
            Log.w(
                BOOK_TRANSLATION_LOG,
                "UseCase enqueue rejected: provider unavailable provider=$coercedProviderMode"
            )
            throw TranslationException("Selected translation provider is unavailable.")
        }

        val source = normalizeTranslationLanguageCode(sourceLanguageCode)
            ?.takeIf { sourceLanguageCode != AUTO_TRANSLATION_LANGUAGE }
        val target = resolveTranslationLanguageCode(targetLanguageCode)
            ?: FALLBACK_TRANSLATION_TARGET_LANGUAGE
        val units = planner.buildUnits(text)
        if (units.isEmpty()) {
            Log.w(BOOK_TRANSLATION_LOG, "UseCase enqueue rejected: no translation units")
            throw TranslationException("This book has no text to translate.")
        }

        val sourceFingerprint = planner.fingerprint(text)
        val existing = bookTranslationRepository.findTranslation(
            bookId = bookId,
            providerMode = coercedProviderMode,
            sourceLanguageCode = source,
            targetLanguageCode = target,
            sourceFingerprint = sourceFingerprint
        )

        if (existing != null) {
            Log.i(
                BOOK_TRANSLATION_LOG,
                "UseCase found existing translation: translationId=${existing.id} " +
                        "status=${existing.status} canRead=${existing.canRead} " +
                        "isBusy=${existing.isBusy} canRetry=${existing.canRetry} " +
                        "canResume=${existing.canResume}"
            )
            return@withContext when {
                existing.canRead -> existing
                existing.status == BookTranslationStatus.QUEUED ||
                        existing.status == BookTranslationStatus.PENDING ||
                        existing.canRetry ||
                        existing.canResume -> queueExistingTranslation(existing, units.size)
                existing.isBusy -> existing
                else -> existing
            }
        }

        val translation = bookTranslationRepository.createTranslation(
            bookId = bookId,
            providerMode = coercedProviderMode,
            sourceLanguageCode = source,
            targetLanguageCode = target,
            requireWifi = requireWifi,
            sourceFingerprint = sourceFingerprint,
            totalUnits = units.size
        )
        workScheduler.enqueue(translation)
        Log.i(
            BOOK_TRANSLATION_LOG,
            "UseCase created translation: translationId=${translation.id} units=${translation.totalUnits}"
        )
        translation
    }

    private suspend fun queueExistingTranslation(
        translation: BookTranslation,
        totalUnits: Int
    ): BookTranslation {
        val queued = translation.copy(
            status = BookTranslationStatus.QUEUED,
            totalUnits = totalUnits,
            errorMessage = null,
            queuedAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            completedAt = null
        )
        bookTranslationRepository.updateTranslation(queued)
        workScheduler.enqueue(queued, replaceExisting = true)
        Log.i(
            BOOK_TRANSLATION_LOG,
            "UseCase queued existing translation: translationId=${queued.id} " +
                    "units=${queued.totalUnits}"
        )
        return queued
    }
}
