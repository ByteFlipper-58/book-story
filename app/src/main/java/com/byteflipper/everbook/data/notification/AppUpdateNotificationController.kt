/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.data.notification

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import androidx.datastore.preferences.core.stringPreferencesKey
import com.byteflipper.everbook.R
import com.byteflipper.everbook.data.local.data_store.DataStore
import com.byteflipper.everbook.domain.distribution.AvailableUpdate
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

private const val APP_UPDATE_NOTIFICATION_ID = 2096
private const val APP_UPDATE_LOG = "AppUpdate"
private val LAST_NOTIFIED_UPDATE_KEY = stringPreferencesKey("last_notified_update_version")

/**
 * Posts the localized "new version available" notification. Flavor-agnostic: it receives an
 * [AvailableUpdate] (already resolved by a [com.byteflipper.everbook.domain.distribution.UpdateNotificationChecker])
 * and delegates channel registration, the permission gate and resilient posting to
 * [AppNotificationManager].
 */
@Singleton
class AppUpdateNotificationController @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appNotifications: AppNotificationManager,
    private val dataStore: DataStore
) {
    /**
     * Posts the notification for [update], unless the user has already been notified about this
     * exact version (so a dismissed-but-not-installed update isn't re-shown every cycle). Once the
     * user updates, the background check stops returning that version, so it never re-appears.
     */
    suspend fun notifyUpdateAvailable(update: AvailableUpdate) {
        if (dataStore.getNullableData(LAST_NOTIFIED_UPDATE_KEY) == update.versionLabel) return

        appNotifications.ensureChannel(NotificationChannelType.AppUpdate)

        val contentIntent = openUriIntent(update.uri)
        val notification = appNotifications.builder(NotificationChannelType.AppUpdate)
            .setContentTitle(context.getString(R.string.updates_notification_title))
            .setContentText(
                context.getString(R.string.updates_notification_desc, update.versionLabel)
            )
            .setStyle(
                androidx.core.app.NotificationCompat.BigTextStyle().bigText(
                    context.getString(R.string.updates_notification_desc, update.versionLabel)
                )
            )
            .setAutoCancel(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(contentIntent)
            .addAction(
                0,
                context.getString(R.string.updates_notification_action),
                contentIntent
            )
            .build()

        val posted = appNotifications.notify(APP_UPDATE_NOTIFICATION_ID, notification, APP_UPDATE_LOG)
        // Remember the version only once it actually reached the user, so a suppressed post
        // (no permission) is retried on the next check instead of being silently skipped.
        if (posted) {
            dataStore.putData(LAST_NOTIFIED_UPDATE_KEY, update.versionLabel)
        }
    }

    private fun openUriIntent(uri: String): PendingIntent {
        val intent = Intent(Intent.ACTION_VIEW, uri.toUri())
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return PendingIntent.getActivity(
            context,
            APP_UPDATE_NOTIFICATION_ID,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }
}
