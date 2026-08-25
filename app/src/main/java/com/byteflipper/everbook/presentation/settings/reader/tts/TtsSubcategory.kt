/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

@file:Suppress("FunctionName")

package com.byteflipper.everbook.presentation.settings.reader.tts

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.byteflipper.everbook.R
import com.byteflipper.everbook.presentation.settings.components.SettingsSubcategory
import com.byteflipper.everbook.presentation.settings.reader.tts.components.TtsAutoScrollOption
import com.byteflipper.everbook.presentation.settings.reader.tts.components.TtsBackgroundPlaybackOption
import com.byteflipper.everbook.presentation.settings.reader.tts.components.TtsHighlightSentenceOption
import com.byteflipper.everbook.presentation.settings.reader.tts.components.TtsParagraphDelayOption
import com.byteflipper.everbook.presentation.settings.reader.tts.components.TtsPitchOption
import com.byteflipper.everbook.presentation.settings.reader.tts.components.TtsSpeakChapterTitlesOption
import com.byteflipper.everbook.presentation.settings.reader.tts.components.TtsSpeechRateOption
import com.byteflipper.everbook.presentation.settings.reader.tts.components.TtsStopAtChapterEndOption
import com.byteflipper.everbook.presentation.settings.reader.tts.components.TtsVoiceOption

fun LazyListScope.TtsSubcategory(
    titleColor: @Composable () -> Color = { MaterialTheme.colorScheme.primary },
    title: @Composable () -> String = { stringResource(id = R.string.tts_reader_settings) },
    showTitle: Boolean = true,
    showDivider: Boolean = true
) {
    SettingsSubcategory(
        titleColor = titleColor,
        title = title,
        showTitle = showTitle,
        showDivider = showDivider
    ) {
        item {
            TtsSpeechRateOption()
        }

        item {
            TtsPitchOption()
        }

        item {
            TtsVoiceOption()
        }

        item {
            TtsParagraphDelayOption()
        }

        item {
            TtsBackgroundPlaybackOption()
        }

        item {
            TtsStopAtChapterEndOption()
        }

        item {
            TtsAutoScrollOption()
        }

        item {
            TtsHighlightSentenceOption()
        }

        item {
            TtsSpeakChapterTitlesOption()
        }
    }
}
