/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.domain.reader.tts

import androidx.compose.runtime.Immutable

/**
 * A single chunk handed to a [TtsEngine]. One utterance is normally one sentence of a paragraph,
 * further split when it exceeds the engine's input limit.
 *
 * [startChar]/[endChar] address the source paragraph, so the reader can highlight exactly the
 * fragment being spoken.
 */
@Immutable
data class TtsUtterance(
    val id: String,
    val text: String,
    val textIndex: Int,
    val sentenceIndex: Int,
    val startChar: Int,
    val endChar: Int,
    val isChapterTitle: Boolean = false
)

/** Cursor into the book: index in `List<ReaderText>` plus the sentence inside that entry. */
@Immutable
data class TtsPosition(
    val textIndex: Int,
    val sentenceIndex: Int
)

@Immutable
data class TtsVoice(
    val id: String,
    val localeTag: String,
    val displayName: String,
    val quality: Int,
    val requiresNetwork: Boolean
)

enum class TtsPlaybackState {
    IDLE,
    PREPARING,
    SPEAKING,
    PAUSED,
    ERROR;

    val isActive: Boolean
        get() = this != IDLE

    val isPlaying: Boolean
        get() = this == SPEAKING || this == PREPARING
}

enum class TtsLanguageAvailability {
    AVAILABLE,
    MISSING_DATA,
    NOT_SUPPORTED,
    UNKNOWN
}

enum class TtsFailure {
    ENGINE_UNAVAILABLE,
    LANGUAGE_MISSING_DATA,
    LANGUAGE_NOT_SUPPORTED,
    SYNTHESIS_FAILED
}

/** Events emitted by an engine implementation while it synthesizes queued utterances. */
sealed interface TtsEngineEvent {
    data object Initialized : TtsEngineEvent

    data class Started(val utteranceId: String) : TtsEngineEvent

    data class Done(val utteranceId: String) : TtsEngineEvent

    /** Word/range boundary inside the utterance, relative to [TtsUtterance.text]. */
    data class Range(
        val utteranceId: String,
        val start: Int,
        val end: Int
    ) : TtsEngineEvent

    data class Stopped(val utteranceId: String, val interrupted: Boolean) : TtsEngineEvent

    data class Failed(val utteranceId: String?, val failure: TtsFailure) : TtsEngineEvent
}
