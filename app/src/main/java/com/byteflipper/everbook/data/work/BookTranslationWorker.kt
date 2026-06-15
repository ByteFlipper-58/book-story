/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.data.work

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.byteflipper.everbook.domain.repository.BookTranslationRepository
import com.byteflipper.everbook.domain.translation.BookTranslationRescheduleException
import com.byteflipper.everbook.domain.translation.BookTranslationStatus
import com.byteflipper.everbook.domain.use_case.translation.BookTranslationExecutor
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val BOOK_TRANSLATION_LOG = "BookTranslation"
private const val BOOK_TRANSLATION_NOTIFICATION_UPDATE_MS = 1_000L

@HiltWorker
class BookTranslationWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val bookTranslationRepository: BookTranslationRepository,
    private val notificationController: BookTranslationNotificationController,
    private val executor: BookTranslationExecutor
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val translationId = inputData.getLong(INPUT_TRANSLATION_ID, -1L)
            .takeIf { it > 0L }
            ?: run {
                Log.e(BOOK_TRANSLATION_LOG, "Worker failed: missing translation id")
                return Result.failure()
            }

        Log.i(BOOK_TRANSLATION_LOG, "Worker started: translationId=$translationId workId=$id")
        val foregroundReady = runCatching {
            Log.i(BOOK_TRANSLATION_LOG, "Worker foreground setup started: translationId=$translationId")
            setForeground(
                notificationController.createForegroundInfo(
                    translationId = translationId,
                    translation = null
                )
            )
            Log.i(BOOK_TRANSLATION_LOG, "Worker foreground setup finished: translationId=$translationId")
            true
        }.getOrElse { throwable ->
            Log.e(
                BOOK_TRANSLATION_LOG,
                "Worker foreground setup failed: translationId=$translationId",
                throwable
            )
            false
        }
        return runCatching {
            coroutineScope {
                val progressJob = launch(Dispatchers.IO) {
                    if (foregroundReady) {
                        updateForegroundProgress(translationId)
                    }
                }
                try {
                    executor.execute(translationId)
                } finally {
                    progressJob.cancelAndJoin()
                }
            }
        }.fold(
            onSuccess = { translation ->
                Log.i(
                    BOOK_TRANSLATION_LOG,
                    "Worker finished: translationId=$translationId status=${translation.status} " +
                            "completed=${translation.completedUnits}/${translation.totalUnits} " +
                            "failed=${translation.failedUnits} foregroundReady=$foregroundReady"
                )
                if (translation.status == BookTranslationStatus.PAUSED) {
                    notificationController.showPaused(translationId)
                }
                notificationController.refreshGroupSummary()
                Result.success()
            },
            onFailure = { throwable ->
                withContext(NonCancellable) {
                    val translation = bookTranslationRepository.getTranslation(translationId)
                    Log.e(
                        BOOK_TRANSLATION_LOG,
                        "Worker failed: translationId=$translationId stopped=$isStopped " +
                                "status=${translation?.status}",
                        throwable
                    )
                    if (translation?.status == BookTranslationStatus.PAUSED) {
                        notificationController.showPaused(translationId)
                    }
                    notificationController.refreshGroupSummary()
                }
                // Cap retries: an error that escapes the executor (e.g. the book text can't be
                // loaded, or a persistent DB error) is effectively permanent. Without a cap,
                // Result.retry() would loop forever with exponential backoff across reboots.
                when {
                    throwable is BookTranslationRescheduleException -> {
                        // Not a failure: the provider is rate-limiting. Reschedule (uncapped) so the
                        // run resumes later via WorkManager backoff.
                        Log.i(
                            BOOK_TRANSLATION_LOG,
                            "Worker rescheduling after rate limit: translationId=$translationId " +
                                    "runAttemptCount=$runAttemptCount"
                        )
                        Result.retry()
                    }

                    isStopped || runAttemptCount >= MAX_RUN_ATTEMPTS -> {
                        Log.w(
                            BOOK_TRANSLATION_LOG,
                            "Worker giving up: translationId=$translationId " +
                                    "stopped=$isStopped runAttemptCount=$runAttemptCount"
                        )
                        Result.failure()
                    }

                    else -> Result.retry()
                }
            }
        )
    }

    private suspend fun updateForegroundProgress(translationId: Long) {
        var lastCompleted = -1
        var lastTotal = -1
        while (currentCoroutineContext().isActive) {
            val translation = bookTranslationRepository.getTranslation(translationId)
            if (translation != null &&
                (
                        translation.completedUnits != lastCompleted ||
                                translation.totalUnits != lastTotal
                        )
            ) {
                lastCompleted = translation.completedUnits
                lastTotal = translation.totalUnits
                runCatching {
                    setForeground(
                        notificationController.createForegroundInfo(
                            translationId = translationId,
                            translation = translation
                        )
                    )
                    notificationController.refreshGroupSummary()
                }.onFailure { throwable ->
                    Log.w(
                        BOOK_TRANSLATION_LOG,
                        "Worker foreground progress update failed: translationId=$translationId",
                        throwable
                    )
                }
            }
            delay(BOOK_TRANSLATION_NOTIFICATION_UPDATE_MS)
        }
    }

    companion object {
        const val INPUT_TRANSLATION_ID = "translation_id"

        /** Maximum WorkManager run attempts before a failing job gives up instead of retrying. */
        private const val MAX_RUN_ATTEMPTS = 5

        fun tagFor(translationId: Long): String = "book_translation_$translationId"
    }
}
