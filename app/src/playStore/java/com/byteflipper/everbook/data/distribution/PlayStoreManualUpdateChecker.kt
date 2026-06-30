/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.data.distribution

import android.util.Log
import androidx.activity.ComponentActivity
import com.byteflipper.everbook.R
import com.byteflipper.everbook.domain.distribution.ManualUpdateChecker
import com.byteflipper.everbook.domain.distribution.UpdateCheckResult
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume

private const val TAG = "PlayStoreUpdateCheck"

/**
 * Manual update check via Google Play In-App Update SDK.
 * On [UpdateCheckResult.UpdateAvailable] the [onStartUpdate] callback launches the
 * FLEXIBLE in-app update flow — the store UI takes over from there.
 */
class PlayStoreManualUpdateChecker @Inject constructor() : ManualUpdateChecker {

    override suspend fun checkForUpdate(
        activity: ComponentActivity
    ): UpdateCheckResult {
        val appUpdateManager = AppUpdateManagerFactory.create(activity)

        val info = suspendCancellableCoroutine { cont ->
            appUpdateManager.appUpdateInfo
                .addOnSuccessListener { cont.resume(it) }
                .addOnFailureListener { cont.resume(null) }
        }

        if (info == null || info.updateAvailability() != UpdateAvailability.UPDATE_AVAILABLE) {
            return UpdateCheckResult.UpToDate
        }

        val versionCode = info.availableVersionCode()
        val type = if (info.updatePriority() >= IMMEDIATE_PRIORITY_THRESHOLD) {
            AppUpdateType.IMMEDIATE
        } else {
            AppUpdateType.FLEXIBLE
        }
        return UpdateCheckResult.UpdateAvailable(
            version = activity.getString(
                R.string.updates_notification_version_build,
                versionCode.toString()
            ),
            useStoreNativeFlow = true,
            onStartUpdate = { act ->
                val options = AppUpdateOptions.newBuilder(type).build()
                appUpdateManager.startUpdateFlowForResult(
                    info, act, options, UPDATE_REQUEST_CODE
                )
            }
        )
    }

    companion object {
        private const val UPDATE_REQUEST_CODE = 1_003
        private const val IMMEDIATE_PRIORITY_THRESHOLD = 4
    }
}
