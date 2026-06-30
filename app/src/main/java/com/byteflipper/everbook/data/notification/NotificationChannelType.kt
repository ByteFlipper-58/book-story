/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.data.notification

import android.app.NotificationManager
import androidx.annotation.StringRes
import com.byteflipper.everbook.R

/**
 * Single source of truth for the app's notification channels.
 *
 * Each entry owns its channel id, user-visible name (string resource) and importance.
 * Channels are registered lazily by [AppNotificationManager.ensureChannel] before the
 * first notification is posted on that channel.
 */
enum class NotificationChannelType(
    val channelId: String,
    @StringRes val nameRes: Int,
    val importance: Int
) {
    BookTranslation(
        channelId = "book_translation",
        nameRes = R.string.book_translation_notification_channel,
        importance = NotificationManager.IMPORTANCE_LOW
    ),
    TranslationModels(
        channelId = "translation_models",
        nameRes = R.string.translation_model_notification_channel,
        importance = NotificationManager.IMPORTANCE_LOW
    ),
    AppUpdate(
        channelId = "app_updates",
        nameRes = R.string.updates_notification_channel,
        importance = NotificationManager.IMPORTANCE_DEFAULT
    )
}
