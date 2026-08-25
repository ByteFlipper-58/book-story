/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.data.tts

import android.content.Context
import android.media.AudioAttributes
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import android.util.Log
import com.byteflipper.everbook.domain.reader.tts.TtsEngine
import com.byteflipper.everbook.domain.reader.tts.TtsEngineEvent
import com.byteflipper.everbook.domain.reader.tts.TtsFailure
import com.byteflipper.everbook.domain.reader.tts.TtsLanguageAvailability
import com.byteflipper.everbook.domain.reader.tts.TtsUtterance
import com.byteflipper.everbook.domain.reader.tts.TtsVoice
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

private const val TTS_LOG = "READER, TTS"
private const val FALLBACK_MAX_INPUT_LENGTH = 3900

/**
 * On-device synthesis on top of the platform [TextToSpeech] service. No network involved: the
 * engine is whatever the user has installed (Google, Samsung, RHVoice, eSpeak…).
 */
@Singleton
class AndroidTtsEngine @Inject constructor(
    @ApplicationContext private val context: Context
) : TtsEngine {

    private val _events = MutableSharedFlow<TtsEngineEvent>(
        replay = 0,
        extraBufferCapacity = 64
    )
    override val events: Flow<TtsEngineEvent> = _events.asSharedFlow()

    private var tts: TextToSpeech? = null

    @Volatile
    private var initialized = false

    private val progressListener = object : UtteranceProgressListener() {
        override fun onStart(utteranceId: String?) {
            utteranceId ?: return
            emit(TtsEngineEvent.Started(utteranceId))
        }

        override fun onDone(utteranceId: String?) {
            utteranceId ?: return
            emit(TtsEngineEvent.Done(utteranceId))
        }

        override fun onStop(utteranceId: String?, interrupted: Boolean) {
            utteranceId ?: return
            emit(TtsEngineEvent.Stopped(utteranceId, interrupted))
        }

        override fun onRangeStart(utteranceId: String?, start: Int, end: Int, frame: Int) {
            utteranceId ?: return
            emit(TtsEngineEvent.Range(utteranceId, start, end))
        }

        @Deprecated("Kept for API < 21 parity; the framework calls the int overload.")
        override fun onError(utteranceId: String?) {
            emit(TtsEngineEvent.Failed(utteranceId, TtsFailure.SYNTHESIS_FAILED))
        }

        override fun onError(utteranceId: String?, errorCode: Int) {
            Log.w(TTS_LOG, "Synthesis error: id=$utteranceId code=$errorCode")
            emit(TtsEngineEvent.Failed(utteranceId, TtsFailure.SYNTHESIS_FAILED))
        }
    }

    override suspend fun initialize(): Boolean {
        if (initialized) return true

        val ready = suspendCancellableCoroutine { continuation ->
            val instance = TextToSpeech(context) { status ->
                if (continuation.isActive) {
                    continuation.resume(status == TextToSpeech.SUCCESS)
                }
            }
            tts = instance
            continuation.invokeOnCancellation {
                runCatching { instance.shutdown() }
                tts = null
            }
        }

        if (!ready) {
            Log.w(TTS_LOG, "No usable text-to-speech engine")
            release()
            emit(TtsEngineEvent.Failed(null, TtsFailure.ENGINE_UNAVAILABLE))
            return false
        }

        tts?.apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
            )
            setOnUtteranceProgressListener(progressListener)
        }
        initialized = true
        emit(TtsEngineEvent.Initialized)
        return true
    }

    override suspend fun voices(): List<TtsVoice> {
        val engine = tts ?: return emptyList()
        return runCatching {
            engine.voices
                .orEmpty()
                .filterNotNull()
                .map { it.toDomain() }
                .sortedWith(compareBy({ it.localeTag }, { it.displayName }))
        }.onFailure {
            Log.w(TTS_LOG, "Voice enumeration failed", it)
        }.getOrDefault(emptyList())
    }

    override fun selectVoice(voiceId: String?): Boolean {
        val engine = tts ?: return false
        if (voiceId.isNullOrBlank()) return false
        val voice = runCatching {
            engine.voices.orEmpty().firstOrNull { it?.name == voiceId }
        }.getOrNull() ?: return false
        return engine.setVoice(voice) == TextToSpeech.SUCCESS
    }

    override fun selectLanguage(localeTag: String?): TtsLanguageAvailability {
        val engine = tts ?: return TtsLanguageAvailability.UNKNOWN
        val locale = if (localeTag.isNullOrBlank()) {
            Locale.getDefault()
        } else {
            Locale.forLanguageTag(localeTag)
        }
        return when (engine.setLanguage(locale)) {
            TextToSpeech.LANG_MISSING_DATA -> TtsLanguageAvailability.MISSING_DATA
            TextToSpeech.LANG_NOT_SUPPORTED -> TtsLanguageAvailability.NOT_SUPPORTED
            TextToSpeech.LANG_AVAILABLE,
            TextToSpeech.LANG_COUNTRY_AVAILABLE,
            TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE -> TtsLanguageAvailability.AVAILABLE

            else -> TtsLanguageAvailability.UNKNOWN
        }
    }

    override fun setSpeechRate(rate: Float) {
        tts?.setSpeechRate(rate.coerceIn(0.1f, 4f))
    }

    override fun setPitch(pitch: Float) {
        tts?.setPitch(pitch.coerceIn(0.1f, 3f))
    }

    override fun speak(utterance: TtsUtterance, flush: Boolean) {
        val engine = tts ?: return
        val params = Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utterance.id)
        }
        val queueMode = if (flush) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
        val result = engine.speak(utterance.text, queueMode, params, utterance.id)
        if (result != TextToSpeech.SUCCESS) {
            emit(TtsEngineEvent.Failed(utterance.id, TtsFailure.SYNTHESIS_FAILED))
        }
    }

    override fun stop() {
        runCatching { tts?.stop() }
    }

    override fun maxInputLength(): Int = runCatching {
        TextToSpeech.getMaxSpeechInputLength()
    }.getOrDefault(FALLBACK_MAX_INPUT_LENGTH)
        .coerceAtLeast(64)
        // The platform limit counts UTF-16 units and some engines choke well before it; keeping
        // utterances shorter also makes pause/resume land closer to where the user stopped.
        .coerceAtMost(FALLBACK_MAX_INPUT_LENGTH)

    override fun release() {
        initialized = false
        runCatching {
            tts?.stop()
            tts?.shutdown()
        }.onFailure {
            Log.w(TTS_LOG, "Engine shutdown failed", it)
        }
        tts = null
    }

    private fun emit(event: TtsEngineEvent) {
        _events.tryEmit(event)
    }

    private fun Voice.toDomain(): TtsVoice {
        val tag = locale?.toLanguageTag().orEmpty()
        val displayLocale = locale?.getDisplayName(Locale.getDefault()).orEmpty()
        return TtsVoice(
            id = name.orEmpty(),
            localeTag = tag,
            displayName = displayLocale.ifBlank { name.orEmpty() },
            quality = quality,
            requiresNetwork = isNetworkConnectionRequired
        )
    }
}
