/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.data.repository

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.byteflipper.everbook.data.work.BookTranslationWorker
import com.byteflipper.everbook.domain.repository.BookTranslationWorkScheduler
import com.byteflipper.everbook.domain.translation.BookTranslation
import com.byteflipper.everbook.domain.translation.TranslationProviderMode
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

private const val BOOK_TRANSLATION_LOG = "BookTranslation"

@Singleton
class BookTranslationWorkSchedulerImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : BookTranslationWorkScheduler {
    override fun enqueue(translation: BookTranslation, replaceExisting: Boolean) {
        Log.i(
            BOOK_TRANSLATION_LOG,
            "Scheduling translation work: translationId=${translation.id} " +
                    "bookId=${translation.bookId} provider=${translation.providerMode} " +
                    "wifiOnly=${translation.requireWifi} replaceExisting=$replaceExisting"
        )
        if (replaceExisting) cancel(translation.id)

        val requiredNetworkType = when (translation.providerMode) {
            TranslationProviderMode.GOOGLE_TRANSLATE -> NetworkType.CONNECTED
            TranslationProviderMode.IN_APP,
            TranslationProviderMode.EXTERNAL -> NetworkType.NOT_REQUIRED
        }
        val request = OneTimeWorkRequestBuilder<BookTranslationWorker>()
            .setInputData(
                workDataOf(BookTranslationWorker.INPUT_TRANSLATION_ID to translation.id)
            )
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(requiredNetworkType)
                    .build()
            )
            .addTag(BookTranslationWorker.tagFor(translation.id))
            .build()

        val existingWorkPolicy = if (replaceExisting) {
            ExistingWorkPolicy.REPLACE
        } else {
            ExistingWorkPolicy.KEEP
        }
        Log.i(
            BOOK_TRANSLATION_LOG,
            "WorkManager enqueue: translationId=${translation.id} " +
                    "workId=${request.id} network=$requiredNetworkType " +
                    "policy=$existingWorkPolicy workName=${workNameFor(translation.id)}"
        )
        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                workNameFor(translation.id),
                existingWorkPolicy,
                request
            )
    }

    override fun cancel(translationId: Long) {
        Log.i(BOOK_TRANSLATION_LOG, "Cancelling scheduled work: translationId=$translationId")
        WorkManager.getInstance(context)
            .cancelAllWorkByTag(BookTranslationWorker.tagFor(translationId))
    }

    private fun workNameFor(translationId: Long): String =
        "book_translation_work_$translationId"
}
