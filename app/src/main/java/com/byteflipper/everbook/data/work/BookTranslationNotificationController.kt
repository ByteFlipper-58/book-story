/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.data.work

import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.work.ForegroundInfo
import com.byteflipper.everbook.R
import com.byteflipper.everbook.data.notification.AppNotificationManager
import com.byteflipper.everbook.data.notification.NotificationChannelType
import com.byteflipper.everbook.domain.repository.BookTranslationRepository
import com.byteflipper.everbook.domain.translation.BookTranslation
import com.byteflipper.everbook.domain.translation.BookTranslationStatus
import com.byteflipper.everbook.domain.use_case.book.GetBookById
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton

private const val BOOK_TRANSLATION_NOTIFICATION_BASE_ID = 2094
private const val BOOK_TRANSLATION_NOTIFICATION_SUMMARY_ID = 12094
private const val BOOK_TRANSLATION_NOTIFICATION_GROUP = "book_translation_group"
private const val BOOK_TRANSLATION_LOG = "BookTranslation"

@Singleton
class BookTranslationNotificationController @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appNotifications: AppNotificationManager,
    private val repository: BookTranslationRepository,
    private val getBookById: GetBookById
) {
    suspend fun createForegroundInfo(
        translationId: Long,
        translation: BookTranslation?
    ): ForegroundInfo {
        appNotifications.ensureChannel(NotificationChannelType.BookTranslation)
        val notification = buildNotification(
            translationId = translationId,
            translation = translation,
            forceRunningActions = true
        )
        val notificationId = notificationIdFor(translationId)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(
                notificationId,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            ForegroundInfo(notificationId, notification)
        }
    }

    suspend fun showPaused(translationId: Long) {
        val translation = repository.getTranslation(translationId)
        if (translation?.status != BookTranslationStatus.PAUSED) return
        appNotifications.ensureChannel(NotificationChannelType.BookTranslation)
        notify(
            notificationId = notificationIdFor(translationId),
            notification = buildNotification(
                translationId = translationId,
                translation = translation,
                forceRunningActions = false
            )
        )
    }

    fun cancelTranslationNotification(translationId: Long) {
        appNotifications.cancel(notificationIdFor(translationId))
    }

    suspend fun refreshGroupSummary() {
        val activeTranslations = repository.getAllTranslations()
            .filter { it.isBusy || it.canResume }
        if (activeTranslations.size <= 1) {
            appNotifications.cancel(BOOK_TRANSLATION_NOTIFICATION_SUMMARY_ID)
            return
        }

        val totalUnits = activeTranslations.sumOf { it.totalUnits.coerceAtLeast(0) }
        val completedUnits = activeTranslations.sumOf { it.completedUnits.coerceAtLeast(0) }
        val notification = appNotifications.builder(NotificationChannelType.BookTranslation)
            .setContentTitle(context.getString(R.string.book_translation_notification_channel))
            .setContentText(
                context.getString(
                    R.string.book_translation_notification_summary,
                    activeTranslations.size
                )
            )
            .setOngoing(true)
            .setSilent(true)
            .setOnlyAlertOnce(true)
            .setGroup(BOOK_TRANSLATION_NOTIFICATION_GROUP)
            .setGroupSummary(true)
            .setProgress(
                totalUnits,
                completedUnits.coerceAtMost(totalUnits),
                totalUnits <= 0
            )
            .build()

        notify(BOOK_TRANSLATION_NOTIFICATION_SUMMARY_ID, notification)
    }

    private suspend fun buildNotification(
        translationId: Long,
        translation: BookTranslation?,
        forceRunningActions: Boolean
    ): android.app.Notification {
        val status = translation?.status
        val paused = status == BookTranslationStatus.PAUSED && !forceRunningActions
        val completedUnits = translation?.completedUnits?.coerceAtLeast(0) ?: 0
        val totalUnits = translation?.totalUnits?.coerceAtLeast(0) ?: 0
        val title = translation?.let { titleFor(it) }
            ?: context.getString(R.string.book_translation_notification_title)
        val contentText = when {
            paused && totalUnits > 0 -> context.getString(
                R.string.book_translation_paused_progress,
                completedUnits,
                totalUnits
            )

            paused -> context.getString(R.string.book_translation_status_paused)
            totalUnits > 0 -> context.getString(
                R.string.book_translation_progress,
                completedUnits,
                totalUnits
            )

            else -> context.getString(R.string.book_translation_notification_text)
        }

        val builder = appNotifications.builder(NotificationChannelType.BookTranslation)
            .setContentTitle(title)
            .setContentText(contentText)
            .setTicker(title)
            .setOngoing(!paused)
            .setAutoCancel(paused)
            .setSilent(true)
            .setOnlyAlertOnce(true)
            .setGroup(BOOK_TRANSLATION_NOTIFICATION_GROUP)
            .setProgress(
                totalUnits,
                completedUnits.coerceAtMost(totalUnits),
                totalUnits <= 0
            )

        if (paused) {
            builder.addAction(
                android.R.drawable.ic_media_play,
                context.getString(R.string.book_translation_resume),
                BookTranslationActionReceiver.pendingIntent(
                    context = context,
                    action = BookTranslationActionReceiver.ACTION_RESUME,
                    translationId = translationId
                )
            )
        } else {
            builder.addAction(
                android.R.drawable.ic_media_pause,
                context.getString(R.string.book_translation_pause),
                BookTranslationActionReceiver.pendingIntent(
                    context = context,
                    action = BookTranslationActionReceiver.ACTION_PAUSE,
                    translationId = translationId
                )
            )
        }

        builder.addAction(
            android.R.drawable.ic_delete,
            context.getString(R.string.cancel),
            BookTranslationActionReceiver.pendingIntent(
                context = context,
                action = BookTranslationActionReceiver.ACTION_CANCEL,
                translationId = translationId
            )
        )

        return builder.build()
    }

    private suspend fun titleFor(translation: BookTranslation): String {
        val title = getBookById.execute(translation.bookId)
            ?.title
            ?.takeIf { it.isNotBlank() }
        return if (title != null) {
            context.getString(R.string.book_translation_notification_title_with_book, title)
        } else {
            context.getString(R.string.book_translation_notification_title)
        }
    }

    private fun notify(
        notificationId: Int,
        notification: android.app.Notification
    ) {
        appNotifications.notify(notificationId, notification, BOOK_TRANSLATION_LOG)
    }

    companion object {
        // Allocate a unique, stable (per-process) notification id per translationId. The previous
        // `base + (id % range)` scheme collided for ids differing by `range`, which on Android 12+
        // could let one translation's cancel tear down another's foreground-service notification.
        private val notificationIds = ConcurrentHashMap<Long, Int>()
        private val notificationIdAllocator = AtomicInteger(0)

        fun notificationIdFor(translationId: Long): Int =
            notificationIds.computeIfAbsent(translationId) {
                BOOK_TRANSLATION_NOTIFICATION_BASE_ID + notificationIdAllocator.getAndIncrement()
            }
    }
}
