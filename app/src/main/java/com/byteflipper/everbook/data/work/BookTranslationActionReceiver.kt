/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.data.work

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.app.PendingIntent
import android.os.Build
import android.util.Log
import com.byteflipper.everbook.data.di.ApplicationScope
import com.byteflipper.everbook.domain.use_case.translation.CancelBookTranslation
import com.byteflipper.everbook.domain.use_case.translation.PauseBookTranslation
import com.byteflipper.everbook.domain.use_case.translation.ResumeBookTranslation
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.absoluteValue

private const val BOOK_TRANSLATION_LOG = "BookTranslation"

@AndroidEntryPoint
class BookTranslationActionReceiver : BroadcastReceiver() {
    @Inject
    lateinit var pauseBookTranslation: PauseBookTranslation

    @Inject
    lateinit var resumeBookTranslation: ResumeBookTranslation

    @Inject
    lateinit var cancelBookTranslation: CancelBookTranslation

    @Inject
    lateinit var notificationController: BookTranslationNotificationController

    @Inject
    @ApplicationScope
    lateinit var applicationScope: CoroutineScope

    override fun onReceive(context: Context, intent: Intent) {
        val translationId = intent.getLongExtra(EXTRA_TRANSLATION_ID, -1L)
            .takeIf { it > 0L }
        val action = intent.action
        if (translationId == null || action == null) {
            Log.w(
                BOOK_TRANSLATION_LOG,
                "Notification action ignored: action=$action translationId=$translationId"
            )
            return
        }

        val pendingResult = goAsync()
        applicationScope.launch {
            try {
                Log.i(
                    BOOK_TRANSLATION_LOG,
                    "Notification action received: action=$action translationId=$translationId"
                )
                when (action) {
                    ACTION_PAUSE -> {
                        pauseBookTranslation.execute(translationId)
                        delay(300)
                        notificationController.showPaused(translationId)
                    }

                    ACTION_RESUME -> {
                        notificationController.cancelTranslationNotification(translationId)
                        resumeBookTranslation.execute(translationId)
                    }

                    ACTION_CANCEL -> {
                        cancelBookTranslation.execute(translationId)
                        notificationController.cancelTranslationNotification(translationId)
                    }
                }
                notificationController.refreshGroupSummary()
            } catch (throwable: Throwable) {
                Log.e(
                    BOOK_TRANSLATION_LOG,
                    "Notification action failed: action=$action translationId=$translationId",
                    throwable
                )
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val ACTION_PAUSE = "com.byteflipper.everbook.action.PAUSE_BOOK_TRANSLATION"
        const val ACTION_RESUME = "com.byteflipper.everbook.action.RESUME_BOOK_TRANSLATION"
        const val ACTION_CANCEL = "com.byteflipper.everbook.action.CANCEL_BOOK_TRANSLATION"

        private const val EXTRA_TRANSLATION_ID = "translation_id"

        fun pendingIntent(
            context: Context,
            action: String,
            translationId: Long
        ): PendingIntent {
            val intent = Intent(context, BookTranslationActionReceiver::class.java)
                .setAction(action)
                .putExtra(EXTRA_TRANSLATION_ID, translationId)
            val flags = PendingIntent.FLAG_UPDATE_CURRENT or
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        PendingIntent.FLAG_IMMUTABLE
                    } else {
                        0
                    }
            return PendingIntent.getBroadcast(
                context,
                requestCodeFor(action, translationId),
                intent,
                flags
            )
        }

        private fun requestCodeFor(action: String, translationId: Long): Int =
            (translationId.hashCode() * 31 + action.hashCode()).absoluteValue
    }
}
