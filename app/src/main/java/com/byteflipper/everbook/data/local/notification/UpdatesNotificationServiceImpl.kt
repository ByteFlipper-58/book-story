/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.data.local.notification

import android.app.Application
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.net.Uri
import com.byteflipper.everbook.R
import com.byteflipper.everbook.data.remote.dto.LatestReleaseInfo
import com.byteflipper.everbook.presentation.core.constants.Constants
import com.byteflipper.everbook.presentation.core.constants.provideDownloadLatestReleasePage
import com.byteflipper.everbook.presentation.core.constants.provideReleasesPage
import javax.inject.Inject

class UpdatesNotificationServiceImpl @Inject constructor(
    private val application: Application
) : UpdatesNotificationService {

    private val notificationManager = application.getSystemService(Context.NOTIFICATION_SERVICE)
            as NotificationManager

    /**
     * Sends notification about new update.
     *
     * @param releaseInfo [LatestReleaseInfo].
     */
    override fun postNotification(releaseInfo: LatestReleaseInfo) {
        val downloadLatestVersionIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse(Constants.provideDownloadLatestReleasePage())
        )
        val latestReleaseIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse(Constants.provideReleasesPage())
        )

        val downloadLatestVersionPendingIntent = PendingIntent.getActivity(
            application,
            1,
            downloadLatestVersionIntent,
            PendingIntent.FLAG_IMMUTABLE
        )
        val latestReleasePendingIntent = PendingIntent.getActivity(
            application,
            1,
            latestReleaseIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = Notification.Builder(
            application,
            UpdatesNotificationService.CHANNEL_ID
        )
            .setSmallIcon(R.drawable.notification_icon)
            .setContentTitle(application.getString(R.string.updates_notification_title))
            .setContentText(
                application.getString(
                    R.string.updates_notification_desc,
                    releaseInfo.name
                )
            )
            .setContentIntent(latestReleasePendingIntent)
            .addAction(
                Notification.Action.Builder(
                    Icon.createWithResource(application, R.drawable.notification_icon),
                    application.getString(R.string.updates_notification_action),
                    downloadLatestVersionPendingIntent
                ).build()
            )
            .build()

        notificationManager.notify(
            0,
            notification
        )
    }
}