/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.data.translation

import android.Manifest
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
import com.byteflipper.everbook.domain.translation.nativeTranslationLanguageName
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

private const val TRANSLATION_MODEL_NOTIFICATION_CHANNEL_ID = "translation_models"
private const val TRANSLATION_MODEL_NOTIFICATION_ID_BASE = 2095
private const val TRANSLATION_MODELS_LOG = "TranslationModels"

@Singleton
class TranslationModelNotificationController @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun showDownload(languageCode: String) {
        show(
            languageCode = languageCode,
            title = context.getString(R.string.translation_model_notification_download_title),
            text = context.getString(
                R.string.translation_model_notification_download_text,
                nativeTranslationLanguageName(languageCode)
            )
        )
    }

    fun showDelete(languageCode: String) {
        show(
            languageCode = languageCode,
            title = context.getString(R.string.translation_model_notification_delete_title),
            text = context.getString(
                R.string.translation_model_notification_delete_text,
                nativeTranslationLanguageName(languageCode)
            )
        )
    }

    fun clear(languageCode: String) {
        NotificationManagerCompat.from(context).cancel(notificationIdFor(languageCode))
    }

    private fun show(
        languageCode: String,
        title: String,
        text: String
    ) {
        if (!canPostNotifications()) {
            Log.w(
                TRANSLATION_MODELS_LOG,
                "Model notification skipped: notification permission missing language=$languageCode"
            )
            return
        }

        createNotificationChannel()

        val notification = NotificationCompat.Builder(
            context,
            TRANSLATION_MODEL_NOTIFICATION_CHANNEL_ID
        )
            .setSmallIcon(R.drawable.notification_icon)
            .setContentTitle(title)
            .setContentText(text)
            .setTicker(title)
            .setOngoing(true)
            .setSilent(true)
            .setProgress(0, 0, true)
            .build()

        runCatching {
            NotificationManagerCompat.from(context).notify(
                notificationIdFor(languageCode),
                notification
            )
        }.onFailure { throwable ->
            Log.w(
                TRANSLATION_MODELS_LOG,
                "Model notification failed: language=$languageCode",
                throwable
            )
        }.onSuccess {
            Log.i(
                TRANSLATION_MODELS_LOG,
                "Model notification shown: language=$languageCode"
            )
        }
    }

    private fun canPostNotifications(): Boolean {
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            Log.w(
                TRANSLATION_MODELS_LOG,
                "Model notification skipped: app notifications are disabled"
            )
            return false
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager
        val channel = NotificationChannel(
            TRANSLATION_MODEL_NOTIFICATION_CHANNEL_ID,
            context.getString(R.string.translation_model_notification_channel),
            NotificationManager.IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }

    private fun notificationIdFor(languageCode: String): Int =
        TRANSLATION_MODEL_NOTIFICATION_ID_BASE + Math.floorMod(languageCode.hashCode(), 1_000)
}
