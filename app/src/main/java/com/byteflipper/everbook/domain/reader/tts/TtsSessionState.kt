/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.domain.reader.tts

import androidx.compose.runtime.Immutable

/**
 * Everything both the reader UI and the playback service need to know about the running
 * read-aloud session. Owned by a single app-scoped session holder.
 */
@Immutable
data class TtsSessionState(
    val bookId: Int = NO_BOOK,
    val bookTitle: String = "",
    val chapterTitle: String = "",
    val playbackState: TtsPlaybackState = TtsPlaybackState.IDLE,
    val textIndex: Int = -1,
    val sentenceIndex: Int = -1,
    val sentenceStart: Int = -1,
    val sentenceEnd: Int = -1,
    val spokenText: String = "",
    val failure: TtsFailure? = null,
    val preferences: TtsPreferences = TtsPreferences(),
    val voices: List<TtsVoice> = emptyList()
) {
    val isActive: Boolean get() = playbackState.isActive

    /** Char range of the sentence inside paragraph [textIndex], or `null` when nothing is spoken. */
    val sentenceRange: IntRange?
        get() = if (sentenceStart in 0 until sentenceEnd) sentenceStart until sentenceEnd else null

    companion object {
        const val NO_BOOK = -1
    }
}
