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
import com.byteflipper.everbook.data.notification.AppUpdateNotificationController
import com.byteflipper.everbook.domain.distribution.UpdateNotificationChecker
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

private const val UPDATE_CHECK_LOG = "UpdateCheck"

/**
 * Periodic background worker: checks for a newer app version via the flavor-bound
 * [UpdateNotificationChecker] and, if one is available, posts the localized notification.
 * Scheduled from [com.byteflipper.everbook.Application.onCreate].
 */
@HiltWorker
class UpdateCheckWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val updateNotificationChecker: UpdateNotificationChecker,
    private val notificationController: AppUpdateNotificationController
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = runCatching {
        val update = updateNotificationChecker.check()
        if (update != null) {
            Log.i(UPDATE_CHECK_LOG, "Update available: ${update.versionLabel}")
            notificationController.notifyUpdateAvailable(update)
        }
        Result.success()
    }.getOrElse { throwable ->
        // Network/SDK hiccups are expected — don't retry-storm, just try again next cycle.
        Log.w(UPDATE_CHECK_LOG, "Update check failed", throwable)
        Result.success()
    }

    companion object {
        const val UNIQUE_WORK_NAME = "app_update_check"
    }
}
