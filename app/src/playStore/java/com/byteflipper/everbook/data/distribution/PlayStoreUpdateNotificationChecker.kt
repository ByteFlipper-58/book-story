/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.data.distribution

import android.content.Context
import com.byteflipper.everbook.BuildConfig
import com.byteflipper.everbook.R
import com.byteflipper.everbook.domain.distribution.AvailableUpdate
import com.byteflipper.everbook.domain.distribution.UpdateNotificationChecker
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.UpdateAvailability
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume

/**
 * Background update check via the Google Play In-App Update SDK (no Activity required for the
 * query). Tapping the notification opens the Play Store listing — the in-app update flow itself
 * still runs from the About screen / on next launch.
 */
class PlayStoreUpdateNotificationChecker @Inject constructor(
    @ApplicationContext private val context: Context
) : UpdateNotificationChecker {

    override suspend fun check(): AvailableUpdate? {
        val appUpdateManager = AppUpdateManagerFactory.create(context)

        val info = suspendCancellableCoroutine { cont ->
            appUpdateManager.appUpdateInfo
                .addOnSuccessListener { cont.resume(it) }
                .addOnFailureListener { cont.resume(null) }
        }

        if (info == null || info.updateAvailability() != UpdateAvailability.UPDATE_AVAILABLE) {
            return null
        }

        return AvailableUpdate(
            versionLabel = context.getString(
                R.string.updates_notification_version_build,
                info.availableVersionCode().toString()
            ),
            uri = "https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}"
        )
    }
}
