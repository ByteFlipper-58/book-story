/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.data.tts

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.view.KeyEvent
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.byteflipper.everbook.R
import com.byteflipper.everbook.data.notification.AppNotificationManager
import com.byteflipper.everbook.data.notification.NotificationChannelType
import com.byteflipper.everbook.domain.reader.tts.TtsPlaybackState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val NOTIFICATION_ID = 3071
private const val MEDIA_SESSION_TAG = "EverBookTts"
private const val WAKE_LOCK_TAG = "everbook:tts"
private const val TTS_LOG = "READER, TTS"

// Safety net only: the lock is released as soon as playback stops.
private const val WAKE_LOCK_TIMEOUT_MS = 3 * 60 * 60 * 1000L

/**
 * Keeps read-aloud running with the screen off and exposes transport controls to the
 * notification shade, the lock screen and headset buttons.
 *
 * The service owns no playback state of its own — [TtsSessionManager] is the single source of
 * truth, and the service stops itself as soon as the session goes idle.
 */
@AndroidEntryPoint
class TtsPlaybackService : Service() {

    @Inject
    lateinit var sessionManager: TtsSessionManager

    @Inject
    lateinit var appNotifications: AppNotificationManager

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var mediaSession: MediaSessionCompat? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var isForeground = false

    /** The subset of the session state the shade and the lock screen actually render. */
    private data class TransportSnapshot(
        val isActive: Boolean,
        val isPlaying: Boolean,
        val isError: Boolean,
        val bookTitle: String,
        val chapterTitle: String,
        val keepAwake: Boolean
    )

    override fun onCreate() {
        super.onCreate()
        appNotifications.ensureChannel(NotificationChannelType.TtsPlayback)
        mediaSession = MediaSessionCompat(this, MEDIA_SESSION_TAG).apply {
            setCallback(mediaSessionCallback)
            isActive = true
        }

        scope.launch {
            sessionManager.state
                .map { state ->
                    TransportSnapshot(
                        isActive = state.isActive,
                        isPlaying = state.playbackState.isPlaying,
                        isError = state.playbackState == TtsPlaybackState.ERROR,
                        bookTitle = state.bookTitle,
                        chapterTitle = state.chapterTitle,
                        keepAwake = state.playbackState.isPlaying &&
                                state.preferences.backgroundPlayback
                    )
                }
                .distinctUntilChanged()
                .collect(::render)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Android requires startForeground() promptly after startForegroundService(), even when
        // the session is still preparing.
        val state = sessionManager.state.value
        postNotification(
            buildNotification(
                bookTitle = state.bookTitle,
                chapterTitle = state.chapterTitle,
                isPlaying = state.playbackState.isPlaying,
                isError = state.playbackState == TtsPlaybackState.ERROR
            )
        )

        when (intent?.action) {
            ACTION_PLAY_PAUSE -> sessionManager.togglePlayPause()
            ACTION_NEXT -> sessionManager.nextParagraph()
            ACTION_PREVIOUS -> sessionManager.previousParagraph()
            ACTION_STOP -> sessionManager.stop()
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent): IBinder? = null

    override fun onTaskRemoved(rootIntent: Intent?) {
        sessionManager.stop()
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        scope.cancel()
        updateWakeLock(hold = false)
        mediaSession?.apply {
            isActive = false
            release()
        }
        mediaSession = null
        super.onDestroy()
    }

    private fun render(snapshot: TransportSnapshot) {
        if (!snapshot.isActive) {
            stopPlayback()
            return
        }
        updateMediaSession(
            title = snapshot.chapterTitle.ifBlank { snapshot.bookTitle },
            subtitle = snapshot.bookTitle,
            isPlaying = snapshot.isPlaying
        )
        updateWakeLock(hold = snapshot.keepAwake)
        postNotification(
            buildNotification(
                bookTitle = snapshot.bookTitle,
                chapterTitle = snapshot.chapterTitle,
                isPlaying = snapshot.isPlaying,
                isError = snapshot.isError
            )
        )
    }

    private val mediaSessionCallback = object : MediaSessionCompat.Callback() {
        override fun onPlay() = sessionManager.resume()
        override fun onPause() = sessionManager.pause()
        override fun onStop() = sessionManager.stop()
        override fun onSkipToNext() = sessionManager.nextParagraph()
        override fun onSkipToPrevious() = sessionManager.previousParagraph()

        override fun onMediaButtonEvent(mediaButtonEvent: Intent): Boolean {
            val event = mediaButtonEvent.keyEvent() ?: return false
            if (event.action != KeyEvent.ACTION_DOWN) return false
            return when (event.keyCode) {
                KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE,
                KeyEvent.KEYCODE_HEADSETHOOK -> {
                    sessionManager.togglePlayPause()
                    true
                }

                else -> super.onMediaButtonEvent(mediaButtonEvent)
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun Intent.keyEvent(): KeyEvent? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getParcelableExtra(Intent.EXTRA_KEY_EVENT, KeyEvent::class.java)
        } else {
            getParcelableExtra(Intent.EXTRA_KEY_EVENT)
        }

    private fun updateMediaSession(title: String, subtitle: String, isPlaying: Boolean) {
        val session = mediaSession ?: return
        session.setMetadata(
            MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, subtitle)
                .build()
        )
        session.setPlaybackState(
            PlaybackStateCompat.Builder()
                .setActions(
                    PlaybackStateCompat.ACTION_PLAY or
                            PlaybackStateCompat.ACTION_PAUSE or
                            PlaybackStateCompat.ACTION_PLAY_PAUSE or
                            PlaybackStateCompat.ACTION_STOP or
                            PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                            PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                )
                .setState(
                    if (isPlaying) PlaybackStateCompat.STATE_PLAYING
                    else PlaybackStateCompat.STATE_PAUSED,
                    PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN,
                    1f
                )
                .build()
        )
    }

    private fun buildNotification(
        bookTitle: String,
        chapterTitle: String,
        isPlaying: Boolean,
        isError: Boolean
    ): Notification {
        val contentText = when {
            isError -> getString(R.string.tts_error_engine_unavailable)
            chapterTitle.isNotBlank() -> chapterTitle
            else -> getString(R.string.tts_notification_text)
        }

        val builder = appNotifications.builder(NotificationChannelType.TtsPlayback)
            .setContentTitle(bookTitle.ifBlank { getString(R.string.tts_notification_title) })
            .setContentText(contentText)
            .setOngoing(isPlaying)
            .setSilent(true)
            .setOnlyAlertOnce(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
            .setDeleteIntent(actionIntent(ACTION_STOP))
            .addAction(
                R.drawable.ic_skip_previous_24px,
                getString(R.string.tts_previous_paragraph),
                actionIntent(ACTION_PREVIOUS)
            )
            .addAction(
                if (isPlaying) R.drawable.ic_pause_rounded_24px
                else R.drawable.ic_play_arrow_rounded_24px,
                getString(if (isPlaying) R.string.tts_pause else R.string.tts_play),
                actionIntent(ACTION_PLAY_PAUSE)
            )
            .addAction(
                R.drawable.ic_skip_next_24px,
                getString(R.string.tts_next_paragraph),
                actionIntent(ACTION_NEXT)
            )
            .addAction(
                R.drawable.ic_stop_24px,
                getString(R.string.tts_stop),
                actionIntent(ACTION_STOP)
            )

        openReaderIntent()?.let(builder::setContentIntent)

        mediaSession?.sessionToken?.let { token ->
            builder.setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(token)
                    .setShowActionsInCompactView(0, 1, 2)
                    .setShowCancelButton(true)
                    .setCancelButtonIntent(actionIntent(ACTION_STOP))
            )
        }

        return builder.build()
    }

    private fun postNotification(notification: Notification) {
        if (!isForeground) {
            ServiceCompat.startForeground(
                this,
                NOTIFICATION_ID,
                notification,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                } else {
                    0
                }
            )
            isForeground = true
            return
        }
        appNotifications.notify(NOTIFICATION_ID, notification, TTS_LOG)
    }

    private fun stopPlayback() {
        updateWakeLock(hold = false)
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        isForeground = false
        stopSelf()
    }

    private fun updateWakeLock(hold: Boolean) {
        if (hold) {
            if (wakeLock?.isHeld == true) return
            val powerManager = ContextCompat.getSystemService(this, PowerManager::class.java)
            wakeLock = powerManager
                ?.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKE_LOCK_TAG)
                ?.apply { acquire(WAKE_LOCK_TIMEOUT_MS) }
            return
        }
        wakeLock?.takeIf { it.isHeld }?.release()
        wakeLock = null
    }

    private fun openReaderIntent(): PendingIntent? {
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
            ?.apply { flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP }
            ?: return null
        return PendingIntent.getActivity(
            this,
            0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun actionIntent(action: String): PendingIntent = PendingIntent.getService(
        this,
        action.hashCode(),
        Intent(this, TtsPlaybackService::class.java).setAction(action),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    companion object {
        const val ACTION_PLAY_PAUSE = "com.byteflipper.everbook.tts.PLAY_PAUSE"
        const val ACTION_NEXT = "com.byteflipper.everbook.tts.NEXT"
        const val ACTION_PREVIOUS = "com.byteflipper.everbook.tts.PREVIOUS"
        const val ACTION_STOP = "com.byteflipper.everbook.tts.STOP"

        fun start(context: Context) {
            ContextCompat.startForegroundService(
                context,
                Intent(context, TtsPlaybackService::class.java)
            )
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, TtsPlaybackService::class.java))
        }
    }
}
