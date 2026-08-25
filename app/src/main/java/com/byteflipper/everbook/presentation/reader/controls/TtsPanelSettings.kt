/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.reader.controls

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.byteflipper.everbook.R
import com.byteflipper.everbook.presentation.settings.reader.tts.components.TtsAutoScrollOption
import com.byteflipper.everbook.presentation.settings.reader.tts.components.TtsBackgroundPlaybackOption
import com.byteflipper.everbook.presentation.settings.reader.tts.components.TtsHighlightSentenceOption
import com.byteflipper.everbook.presentation.settings.reader.tts.components.TtsParagraphDelayOption
import com.byteflipper.everbook.presentation.settings.reader.tts.components.TtsPitchOption
import com.byteflipper.everbook.presentation.settings.reader.tts.components.TtsSpeakChapterTitlesOption
import com.byteflipper.everbook.presentation.settings.reader.tts.components.TtsSpeechRateOption
import com.byteflipper.everbook.presentation.settings.reader.tts.components.TtsStopAtChapterEndOption
import com.byteflipper.everbook.presentation.settings.reader.tts.components.TtsVoiceOption

/**
 * Read-aloud settings shown inside the floating panel, reusing the very same options as the reader
 * settings screen. The header stays put while the options scroll, because the panel is far shorter
 * than the list.
 */
@Composable
fun ColumnScope.TtsPanelSettings(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, end = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_back_rounded_24px),
                contentDescription = stringResource(id = R.string.tts_close_settings),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(id = R.string.tts_open_settings),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1
        )
    }

    Spacer(modifier = Modifier.height(8.dp))
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        color = MaterialTheme.colorScheme.outlineVariant
    )

    Column(
        modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 4.dp)
    ) {
        TtsSpeechRateOption()
        TtsPitchOption()
        TtsVoiceOption()
        TtsParagraphDelayOption()
        TtsBackgroundPlaybackOption()
        TtsStopAtChapterEndOption()
        TtsAutoScrollOption()
        TtsHighlightSentenceOption()
        TtsSpeakChapterTitlesOption()
    }
}
