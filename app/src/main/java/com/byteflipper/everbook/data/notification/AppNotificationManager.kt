/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.data.notification

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.byteflipper.everbook.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Central, app-wide notification plumbing.
 *
 * Owns the shared platform concerns that every feature notification used to duplicate:
 * channel registration, the [POST_NOTIFICATIONS][Manifest.permission.POST_NOTIFICATIONS]
 * permission gate, builder defaults (small icon + channel) and resilient posting/cancelling.
 *
 * Feature controllers (book translation, ML Kit model downloads, …) keep their own
 * domain-specific notification building and delegate the plumbing here.
 */
@Singleton
class AppNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    /** Registers [channel] on API 26+. Idempotent — safe to call before every post. */
    fun ensureChannel(channel: NotificationChannelType) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager
        val notificationChannel = NotificationChannel(
            channel.channelId,
            context.getString(channel.nameRes),
            channel.importance
        )
        notificationManager.createNotificationChannel(notificationChannel)
    }

    /**
     * Whether the app is currently allowed to post notifications: notifications must be
     * enabled and, on API 33+, the runtime [POST_NOTIFICATIONS][Manifest.permission.POST_NOTIFICATIONS]
     * permission granted.
     */
    fun canPostNotifications(): Boolean {
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return false
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
    }

    /** A [NotificationCompat.Builder] preconfigured with the channel id and the app's small icon. */
    fun builder(channel: NotificationChannelType): NotificationCompat.Builder =
        NotificationCompat.Builder(context, channel.channelId)
            .setSmallIcon(R.drawable.notification_icon)

    /**
     * Posts [notification] under [id] if [canPostNotifications]. Failures are swallowed and
     * logged under [logTag] so a single misbehaving post can't crash a worker or receiver.
     *
     * @return `true` when the notification was handed to the system, `false` otherwise.
     */
    fun notify(id: Int, notification: Notification, logTag: String): Boolean {
        if (!canPostNotifications()) return false
        return runCatching {
            NotificationManagerCompat.from(context).notify(id, notification)
            true
        }.onFailure { throwable ->
            Log.w(logTag, "Notification post failed: id=$id", throwable)
        }.getOrDefault(false)
    }

    /** Cancels the notification posted under [id], if any. */
    fun cancel(id: Int) {
        NotificationManagerCompat.from(context).cancel(id)
    }
}
