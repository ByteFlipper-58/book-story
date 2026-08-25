/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.domain.reader.tts

import androidx.compose.runtime.Immutable

/** User-facing synthesis and playback options, mirrored from the app's settings. */
@Immutable
data class TtsPreferences(
    val speechRate: Float = DEFAULT_SPEECH_RATE,
    val pitch: Float = DEFAULT_PITCH,
    /** Engine voice id; empty means "let the engine pick for [languageTag]". */
    val voiceId: String = "",
    /** BCP-47 tag; empty means "follow the app/system locale". */
    val languageTag: String = "",
    val backgroundPlayback: Boolean = true,
    val stopAtChapterEnd: Boolean = false,
    val paragraphDelayMs: Int = 0,
    val autoScroll: Boolean = true,
    val highlightSentence: Boolean = true,
    val speakChapterTitles: Boolean = true
) {
    companion object {
        const val DEFAULT_SPEECH_RATE = 1.0f
        const val DEFAULT_PITCH = 1.0f
        const val MIN_SPEECH_RATE = 0.5f
        const val MAX_SPEECH_RATE = 3.0f
        const val MIN_PITCH = 0.5f
        const val MAX_PITCH = 2.0f
        const val MAX_PARAGRAPH_DELAY_MS = 3000
    }
}
