/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.data.distribution

import android.util.Log
import androidx.activity.ComponentActivity
import com.byteflipper.everbook.domain.distribution.ManualUpdateChecker
import com.byteflipper.everbook.domain.distribution.UpdateCheckResult
import kotlinx.coroutines.suspendCancellableCoroutine
import ru.rustore.sdk.appupdate.manager.factory.RuStoreAppUpdateManagerFactory
import ru.rustore.sdk.appupdate.model.AppUpdateOptions
import ru.rustore.sdk.appupdate.model.AppUpdateType
import ru.rustore.sdk.appupdate.model.UpdateAvailability
import javax.inject.Inject
import kotlin.coroutines.resume

private const val TAG = "RuStoreUpdateCheck"

/**
 * Manual update check via RuStore In-App Update SDK.
 * On [UpdateCheckResult.UpdateAvailable] the [onStartUpdate] callback launches the
 * FLEXIBLE in-app update flow.
 */
class RuStoreManualUpdateChecker @Inject constructor() : ManualUpdateChecker {

    override suspend fun checkForUpdate(
        activity: ComponentActivity
    ): UpdateCheckResult {
        val manager = RuStoreAppUpdateManagerFactory.create(activity)

        val info = suspendCancellableCoroutine { cont ->
            manager.getAppUpdateInfo()
                .addOnSuccessListener { cont.resume(it) }
                .addOnFailureListener { cont.resume(null) }
        }

        if (info == null || info.updateAvailability != UpdateAvailability.UPDATE_AVAILABLE) {
            return UpdateCheckResult.UpToDate
        }

        val versionName = info.availableVersionName ?: "???"
        val type = if (info.updatePriority >= IMMEDIATE_PRIORITY_THRESHOLD) {
            AppUpdateType.IMMEDIATE
        } else {
            AppUpdateType.FLEXIBLE
        }
        return UpdateCheckResult.UpdateAvailable(
            version = versionName,
            useStoreNativeFlow = true,
            onStartUpdate = { act ->
                val options = AppUpdateOptions.Builder()
                    .appUpdateType(type)
                    .build()
                manager.startUpdateFlow(info, options)
                    .addOnFailureListener { err ->
                        Log.w(TAG, "startUpdateFlow failed", err)
                    }
            }
        )
    }

    private companion object {
        const val IMMEDIATE_PRIORITY_THRESHOLD = 4
    }
}
