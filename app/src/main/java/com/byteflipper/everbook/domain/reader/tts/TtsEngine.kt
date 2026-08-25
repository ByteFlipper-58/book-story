/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.domain.reader.tts

import kotlinx.coroutines.flow.Flow

/**
 * Everything a reading session needs from a synthesizer. The session, the playback service and
 * the UI talk to this interface only, so a bundled neural engine can later replace the platform
 * one without touching them.
 */
interface TtsEngine {

    val events: Flow<TtsEngineEvent>

    /** Boots the engine. Returns `false` when no usable synthesizer is installed. */
    suspend fun initialize(): Boolean

    suspend fun voices(): List<TtsVoice>

    fun selectVoice(voiceId: String?): Boolean

    fun selectLanguage(localeTag: String?): TtsLanguageAvailability

    fun setSpeechRate(rate: Float)

    fun setPitch(pitch: Float)

    /** Queues [utterance]; [flush] drops anything already queued before speaking. */
    fun speak(utterance: TtsUtterance, flush: Boolean)

    fun stop()

    /** Longest string [speak] accepts; longer utterances must be split by the caller. */
    fun maxInputLength(): Int

    fun release()
}
