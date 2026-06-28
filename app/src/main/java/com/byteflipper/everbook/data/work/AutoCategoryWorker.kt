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
import com.byteflipper.everbook.domain.use_case.category.DropStaleReadingBooks
import com.byteflipper.everbook.ui.library.LibraryScreen
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

private const val AUTO_CATEGORY_LOG = "AutoCategory"

/**
 * Daily maintenance worker: demotes stale "Reading" books to "Dropped".
 * Scheduled from [com.byteflipper.everbook.Application.onCreate].
 */
@HiltWorker
class AutoCategoryWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val dropStaleReadingBooks: DropStaleReadingBooks
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = runCatching {
        val dropped = dropStaleReadingBooks(now = System.currentTimeMillis())
        if (dropped.isNotEmpty()) {
            Log.i(AUTO_CATEGORY_LOG, "Moved ${dropped.size} stale book(s) to Dropped: $dropped")
            LibraryScreen.refreshListChannel.trySend(0)
        }
        Result.success()
    }.getOrElse { throwable ->
        Log.e(AUTO_CATEGORY_LOG, "AutoCategoryWorker failed", throwable)
        Result.retry()
    }

    companion object {
        const val UNIQUE_WORK_NAME = "auto_category_dropped"
    }
}
