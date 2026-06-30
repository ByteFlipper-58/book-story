/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.data.translation

import android.content.Context
import android.util.Log
import com.byteflipper.everbook.R
import com.byteflipper.everbook.data.notification.AppNotificationManager
import com.byteflipper.everbook.data.notification.NotificationChannelType
import com.byteflipper.everbook.domain.translation.nativeTranslationLanguageName
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

private const val TRANSLATION_MODEL_NOTIFICATION_ID_BASE = 2095
private const val TRANSLATION_MODELS_LOG = "TranslationModels"

@Singleton
class TranslationModelNotificationController @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appNotifications: AppNotificationManager
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
        appNotifications.cancel(notificationIdFor(languageCode))
    }

    private fun show(
        languageCode: String,
        title: String,
        text: String
    ) {
        if (!appNotifications.canPostNotifications()) {
            Log.w(
                TRANSLATION_MODELS_LOG,
                "Model notification skipped: notification permission missing language=$languageCode"
            )
            return
        }

        appNotifications.ensureChannel(NotificationChannelType.TranslationModels)

        val notification = appNotifications.builder(NotificationChannelType.TranslationModels)
            .setContentTitle(title)
            .setContentText(text)
            .setTicker(title)
            .setOngoing(true)
            .setSilent(true)
            .setProgress(0, 0, true)
            .build()

        val posted = appNotifications.notify(
            notificationIdFor(languageCode),
            notification,
            TRANSLATION_MODELS_LOG
        )
        if (posted) {
            Log.i(
                TRANSLATION_MODELS_LOG,
                "Model notification shown: language=$languageCode"
            )
        }
    }

    private fun notificationIdFor(languageCode: String): Int =
        TRANSLATION_MODEL_NOTIFICATION_ID_BASE + Math.floorMod(languageCode.hashCode(), 1_000)
}
