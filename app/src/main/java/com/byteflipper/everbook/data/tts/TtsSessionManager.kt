/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.data.tts

import android.util.Log
import com.byteflipper.everbook.domain.reader.ReaderText
import com.byteflipper.everbook.domain.reader.tts.TtsBookQueue
import com.byteflipper.everbook.domain.reader.tts.TtsEngine
import com.byteflipper.everbook.domain.reader.tts.TtsEngineEvent
import com.byteflipper.everbook.domain.reader.tts.TtsFailure
import com.byteflipper.everbook.domain.reader.tts.TtsLanguageAvailability
import com.byteflipper.everbook.domain.reader.tts.TtsPlaybackState
import com.byteflipper.everbook.domain.reader.tts.TtsPosition
import com.byteflipper.everbook.domain.reader.tts.TtsPreferences
import com.byteflipper.everbook.domain.reader.tts.TtsSessionState
import com.byteflipper.everbook.domain.reader.tts.TtsUtterance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

private const val TTS_LOG = "READER, TTS"

// Utterances queued behind the spoken one. Keeps the engine from going silent between sentences
// without making a skip re-synthesize a long tail.
private const val LOOKAHEAD = 1

private const val EPOCH_SEPARATOR = '#'

/**
 * App-scoped owner of the read-aloud session: keeps the queue, drives the [TtsEngine] and
 * publishes a single [TtsSessionState] consumed by both the reader UI and the playback service.
 *
 * Every entry point runs on the main dispatcher, so engine callbacks, notification actions and
 * the reader never race for the cursor.
 */
@Singleton
class TtsSessionManager @Inject constructor(
    private val engine: TtsEngine,
    private val audioFocus: TtsAudioFocusManager
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val _state = MutableStateFlow(TtsSessionState())
    val state: StateFlow<TtsSessionState> = _state.asStateFlow()

    private var queue: TtsBookQueue? = null
    private var chapterTitles: List<Pair<Int, String>> = emptyList()
    private var cursor: TtsPosition? = null
    private var lastEnqueued: TtsPosition? = null
    private var pendingAhead = 0
    private var reachedEnd = false
    private var engineReady = false
    private var eventsJob: Job? = null
    private var continuationJob: Job? = null

    // Utterance ids carry this counter so callbacks from a flushed queue can be discarded:
    // positions repeat across flushes, epochs do not.
    private var epoch = 0

    /**
     * Starts (or restarts) reading [entries] of [bookId] at [startTextIndex].
     * Returns `false` when no on-device engine is available or audio focus was denied.
     */
    suspend fun start(
        bookId: Int,
        bookTitle: String,
        entries: List<ReaderText>,
        startTextIndex: Int,
        preferences: TtsPreferences
    ): Boolean {
        stopInternal(resetState = false)
        _state.value = TtsSessionState(
            bookId = bookId,
            bookTitle = bookTitle,
            playbackState = TtsPlaybackState.PREPARING,
            preferences = preferences
        )

        if (!ensureEngine()) {
            fail(TtsFailure.ENGINE_UNAVAILABLE)
            return false
        }

        val bookQueue = TtsBookQueue(
            entries = entries,
            maxUtteranceLength = engine.maxInputLength(),
            speakChapterTitles = preferences.speakChapterTitles
        )
        queue = bookQueue
        chapterTitles = entries.mapIndexedNotNull { index, entry ->
            (entry as? ReaderText.Chapter)?.let { index to it.title }
        }

        applyPreferences(preferences)
        val voices = engine.voices()
        _state.value = _state.value.copy(voices = voices)

        val start = bookQueue.firstFrom(startTextIndex) ?: bookQueue.firstFrom(0)
        if (start == null) {
            stopInternal(resetState = true)
            return false
        }

        if (!requestFocus()) {
            Log.w(TTS_LOG, "Audio focus denied")
            fail(TtsFailure.SYNTHESIS_FAILED)
            return false
        }

        speakFrom(start)
        return true
    }

    fun pause() {
        if (!_state.value.playbackState.isPlaying) return
        continuationJob?.cancel()
        // TextToSpeech has no pause: the queue is flushed and resume re-speaks the sentence the
        // cursor stopped on.
        flushEngine()
        _state.value = _state.value.copy(playbackState = TtsPlaybackState.PAUSED)
    }

    fun resume() {
        val playbackState = _state.value.playbackState
        if (playbackState != TtsPlaybackState.PAUSED && playbackState != TtsPlaybackState.ERROR) {
            return
        }
        val position = cursor ?: queue?.firstFrom(0) ?: return
        if (!requestFocus()) return
        speakFrom(position)
    }

    fun togglePlayPause() {
        if (_state.value.playbackState.isPlaying) pause() else resume()
    }

    fun stop() {
        stopInternal(resetState = true)
    }

    fun nextSentence() = seek { bookQueue, position -> bookQueue.next(position) }

    fun previousSentence() = seek { bookQueue, position -> bookQueue.previous(position) }

    fun nextParagraph() = seek { bookQueue, position -> bookQueue.nextParagraph(position) }

    fun previousParagraph() = seek { bookQueue, position -> bookQueue.previousParagraph(position) }

    /** Moves playback to the paragraph the user scrolled to or tapped. */
    fun seekToTextIndex(textIndex: Int) = seek { bookQueue, _ -> bookQueue.firstFrom(textIndex) }

    /**
     * Pushes new synthesis options. Rate/pitch/voice apply to utterances queued from now on, so
     * a change is audible after at most the buffered look-ahead.
     */
    fun applyPreferences(preferences: TtsPreferences) {
        _state.value = _state.value.copy(preferences = preferences)
        if (!engineReady) return

        engine.setSpeechRate(preferences.speechRate)
        engine.setPitch(preferences.pitch)
        val availability = engine.selectLanguage(preferences.languageTag.takeIf { it.isNotBlank() })
        if (availability == TtsLanguageAvailability.MISSING_DATA ||
            availability == TtsLanguageAvailability.NOT_SUPPORTED
        ) {
            Log.w(TTS_LOG, "Language unavailable: ${preferences.languageTag} ($availability)")
        }
        engine.selectVoice(preferences.voiceId.takeIf { it.isNotBlank() })
    }

    fun release() {
        stopInternal(resetState = true)
        eventsJob?.cancel()
        eventsJob = null
        engineReady = false
        engine.release()
    }

    private inline fun seek(select: (TtsBookQueue, TtsPosition) -> TtsPosition?) {
        val bookQueue = queue ?: return
        val position = cursor ?: return
        val target = select(bookQueue, position) ?: return
        if (_state.value.playbackState == TtsPlaybackState.PAUSED) {
            flushEngine()
            cursor = target
            publishPosition(target)
        } else {
            speakFrom(target)
        }
    }

    private fun requestFocus(): Boolean = audioFocus.request(
        onPause = ::pause,
        onResume = ::resume,
        onStop = ::stop
    )

    private suspend fun ensureEngine(): Boolean {
        if (engineReady) return true
        if (!engine.initialize()) return false
        engineReady = true
        if (eventsJob == null) {
            eventsJob = scope.launch {
                engine.events.collect(::onEngineEvent)
            }
        }
        return true
    }

    private fun speakFrom(position: TtsPosition) {
        val bookQueue = queue ?: return
        val utterance = bookQueue.utteranceAt(position) ?: return
        continuationJob?.cancel()
        epoch++
        cursor = position
        lastEnqueued = position
        pendingAhead = 1
        reachedEnd = false
        _state.value = _state.value.copy(
            playbackState = TtsPlaybackState.PREPARING,
            failure = null
        )
        publishPosition(position)
        engine.speak(utterance.tagged(), flush = true)
        topUp()
    }

    private fun topUp() {
        val bookQueue = queue ?: return
        val preferences = _state.value.preferences
        while (pendingAhead <= LOOKAHEAD) {
            val from = lastEnqueued ?: return
            val next = bookQueue.next(from)
            if (next == null) {
                reachedEnd = true
                return
            }
            if (preferences.stopAtChapterEnd && bookQueue.crossesChapter(from, next)) {
                reachedEnd = true
                return
            }
            // An inter-paragraph gap only exists if nothing is queued behind the current
            // paragraph; it is inserted once that paragraph's last utterance completes.
            if (preferences.paragraphDelayMs > 0 && next.textIndex != from.textIndex) return

            val utterance = bookQueue.utteranceAt(next) ?: return
            lastEnqueued = next
            pendingAhead++
            engine.speak(utterance.tagged(), flush = false)
        }
    }

    private fun onEngineEvent(event: TtsEngineEvent) {
        when (event) {
            is TtsEngineEvent.Started -> {
                val position = event.utteranceId.toPosition() ?: return
                pendingAhead = (pendingAhead - 1).coerceAtLeast(0)
                cursor = position
                _state.value = _state.value.copy(
                    playbackState = TtsPlaybackState.SPEAKING,
                    failure = null
                )
                publishPosition(position)
                topUp()
            }

            is TtsEngineEvent.Done -> {
                val position = event.utteranceId.toPosition() ?: return
                if (position != lastEnqueued) return
                if (reachedEnd) {
                    stopInternal(resetState = true)
                } else {
                    scheduleContinuation(position)
                }
            }

            is TtsEngineEvent.Failed -> {
                val utteranceId = event.utteranceId
                if (utteranceId != null && utteranceId.toPosition() == null) return
                fail(event.failure)
            }

            is TtsEngineEvent.Range,
            is TtsEngineEvent.Stopped,
            TtsEngineEvent.Initialized -> Unit
        }
    }

    /** Continues after the queue drained: the inter-paragraph gap, or a buffer underrun. */
    private fun scheduleContinuation(from: TtsPosition) {
        val bookQueue = queue ?: return
        val next = bookQueue.next(from)
        if (next == null) {
            stopInternal(resetState = true)
            return
        }
        val delayMs = _state.value.preferences.paragraphDelayMs
            .takeIf { it > 0 && next.textIndex != from.textIndex }
            ?: 0
        continuationJob?.cancel()
        continuationJob = scope.launch {
            if (delayMs > 0) delay(delayMs.toLong())
            if (_state.value.playbackState == TtsPlaybackState.IDLE) return@launch
            speakFrom(next)
        }
    }

    private fun publishPosition(position: TtsPosition) {
        val utterance = queue?.utteranceAt(position)
        _state.value = _state.value.copy(
            textIndex = position.textIndex,
            sentenceIndex = position.sentenceIndex,
            sentenceStart = utterance?.startChar ?: -1,
            sentenceEnd = utterance?.endChar ?: -1,
            spokenText = utterance?.text.orEmpty(),
            chapterTitle = chapterTitleFor(position.textIndex)
        )
    }

    private fun chapterTitleFor(textIndex: Int): String =
        chapterTitles.lastOrNull { it.first <= textIndex }?.second.orEmpty()

    private fun fail(failure: TtsFailure) {
        flushEngine()
        audioFocus.abandon()
        _state.value = _state.value.copy(
            playbackState = TtsPlaybackState.ERROR,
            failure = failure
        )
    }

    private fun stopInternal(resetState: Boolean) {
        continuationJob?.cancel()
        continuationJob = null
        flushEngine()
        audioFocus.abandon()
        queue = null
        chapterTitles = emptyList()
        cursor = null
        reachedEnd = false
        if (resetState) {
            _state.value = TtsSessionState(preferences = _state.value.preferences)
        }
    }

    private fun flushEngine() {
        epoch++
        pendingAhead = 0
        lastEnqueued = null
        if (engineReady) engine.stop()
    }

    private fun TtsUtterance.tagged(): TtsUtterance = copy(id = "$epoch$EPOCH_SEPARATOR$id")

    private fun String.toPosition(): TtsPosition? {
        val separator = indexOf(EPOCH_SEPARATOR)
        if (separator <= 0) return null
        if (substring(0, separator).toIntOrNull() != epoch) return null
        return TtsBookQueue.parsePosition(substring(separator + 1))
    }
}
