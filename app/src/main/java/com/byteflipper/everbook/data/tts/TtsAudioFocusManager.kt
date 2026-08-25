/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.data.tts

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Audio focus and headset handling for read-aloud.
 *
 * The platform synthesizer has no volume control we can duck with, so a transient loss (a
 * navigation prompt, a notification read-out) is treated the same as a full pause and resumed
 * afterwards. Unplugging headphones stops playback like any other media app.
 */
@Singleton
class TtsAudioFocusManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val audioManager: AudioManager? =
        ContextCompat.getSystemService(context, AudioManager::class.java)

    private var focusRequest: AudioFocusRequest? = null
    private var noisyReceiver: BroadcastReceiver? = null

    private var onPause: (() -> Unit)? = null
    private var onResume: (() -> Unit)? = null
    private var onStop: (() -> Unit)? = null
    private var pausedByFocusLoss = false

    private val focusListener = AudioManager.OnAudioFocusChangeListener { change ->
        when (change) {
            AudioManager.AUDIOFOCUS_LOSS -> {
                pausedByFocusLoss = false
                onStop?.invoke()
            }

            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT,
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                pausedByFocusLoss = true
                onPause?.invoke()
            }

            AudioManager.AUDIOFOCUS_GAIN -> {
                if (pausedByFocusLoss) {
                    pausedByFocusLoss = false
                    onResume?.invoke()
                }
            }
        }
    }

    /**
     * Requests focus and starts listening for [AudioManager.ACTION_AUDIO_BECOMING_NOISY].
     * Returns `false` when the system denied focus, in which case nothing was registered.
     */
    fun request(
        onPause: () -> Unit,
        onResume: () -> Unit,
        onStop: () -> Unit
    ): Boolean {
        val manager = audioManager ?: return false
        this.onPause = onPause
        this.onResume = onResume
        this.onStop = onStop

        if (focusRequest == null) {
            focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                .setOnAudioFocusChangeListener(focusListener)
                .setWillPauseWhenDucked(true)
                .build()
        }
        val granted = manager.requestAudioFocus(focusRequest!!) ==
                AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        if (granted) registerNoisyReceiver()
        return granted
    }

    fun abandon() {
        pausedByFocusLoss = false
        unregisterNoisyReceiver()
        focusRequest?.let { audioManager?.abandonAudioFocusRequest(it) }
        onPause = null
        onResume = null
        onStop = null
    }

    private fun registerNoisyReceiver() {
        if (noisyReceiver != null) return
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
                    pausedByFocusLoss = false
                    onPause?.invoke()
                }
            }
        }
        ContextCompat.registerReceiver(
            context,
            receiver,
            IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        noisyReceiver = receiver
    }

    private fun unregisterNoisyReceiver() {
        noisyReceiver?.let { runCatching { context.unregisterReceiver(it) } }
        noisyReceiver = null
    }
}
