/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.reader.controls

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.byteflipper.everbook.domain.reader.tts.TtsFailure
import com.byteflipper.everbook.domain.reader.tts.TtsPlaybackState
import com.byteflipper.everbook.domain.reader.tts.TtsPreferences
import com.byteflipper.everbook.ui.reader.ReaderEvent
import com.byteflipper.everbook.ui.reader.ReaderTtsState
import java.util.Locale

private const val RATE_STEP = 0.1f
private const val RATE_TICK_STEP = 0.5f

/** Floating controls of a running read-aloud session. */
@Composable
fun TtsControlPanel(
    tts: ReaderTtsState,
    chipAlignment: String,
    chipOpacity: Int,
    chipOpacityEnabled: Boolean,
    bookId: Int,
    onTtsEvent: (ReaderEvent) -> Unit,
    onChangeSpeechRate: (Float) -> Unit,
    openTtsSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val collapseState = rememberReaderPanelCollapseState(
        active = tts.isActive,
        resetKey = bookId
    )

    FloatingReaderControlPanel(
        visible = tts.isActive,
        collapseState = collapseState,
        alignment = chipAlignment,
        collapsedWidthFraction = 0.32f,
        expandedHeight = 148.dp,
        collapsedAlpha = if (chipOpacityEnabled) chipOpacity / 100f else 1f,
        modifier = modifier,
        collapsedContent = {
            IconButton(
                onClick = { onTtsEvent(ReaderEvent.OnToggleTtsPlayback) },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    painter = painterResource(id = tts.playPauseIcon()),
                    contentDescription = stringResource(id = tts.playPauseLabel()),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                painter = painterResource(id = R.drawable.ic_volume_up_24px),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = tts.speechRate.formatRate(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        expandedContent = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_volume_up_24px),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(id = tts.headline()),
                        style = if (tts.failure != null) {
                            MaterialTheme.typography.bodySmall
                        } else {
                            MaterialTheme.typography.titleMedium
                        },
                        color = if (tts.failure != null) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        maxLines = 2
                    )
                }
                Box(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = tts.speechRate.formatRate(),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            ReaderControlTrack(
                value = tts.speechRate,
                valueRange = TtsPreferences.MIN_SPEECH_RATE..TtsPreferences.MAX_SPEECH_RATE,
                step = RATE_STEP,
                tickStep = RATE_TICK_STEP,
                onValueChange = onChangeSpeechRate,
                onInteraction = collapseState::touch,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TtsAction(
                    icon = R.drawable.ic_skip_previous_24px,
                    label = R.string.tts_previous_paragraph,
                    onClick = {
                        collapseState.touch()
                        onTtsEvent(ReaderEvent.OnTtsPreviousParagraph)
                    }
                )
                TtsAction(
                    icon = R.drawable.ic_arrow_back_rounded_24px,
                    label = R.string.tts_previous_sentence,
                    onClick = {
                        collapseState.touch()
                        onTtsEvent(ReaderEvent.OnTtsPreviousSentence)
                    }
                )
                TtsAction(
                    icon = tts.playPauseIcon(),
                    label = tts.playPauseLabel(),
                    emphasized = true,
                    onClick = {
                        collapseState.touch()
                        onTtsEvent(ReaderEvent.OnToggleTtsPlayback)
                    }
                )
                TtsAction(
                    icon = R.drawable.ic_arrow_forward_rounded_24px,
                    label = R.string.tts_next_sentence,
                    onClick = {
                        collapseState.touch()
                        onTtsEvent(ReaderEvent.OnTtsNextSentence)
                    }
                )
                TtsAction(
                    icon = R.drawable.ic_skip_next_24px,
                    label = R.string.tts_next_paragraph,
                    onClick = {
                        collapseState.touch()
                        onTtsEvent(ReaderEvent.OnTtsNextParagraph)
                    }
                )
                TtsAction(
                    icon = R.drawable.ic_settings_rounded_24px,
                    label = R.string.tts_open_settings,
                    onClick = {
                        collapseState.touch()
                        openTtsSettings()
                    }
                )
                TtsAction(
                    icon = R.drawable.ic_stop_24px,
                    label = R.string.tts_stop,
                    onClick = { onTtsEvent(ReaderEvent.OnStopTts) }
                )
            }
        }
    )
}

@Composable
private fun TtsAction(
    icon: Int,
    label: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    emphasized: Boolean = false
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.size(if (emphasized) 44.dp else 36.dp)
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = stringResource(id = label),
            tint = if (emphasized) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier.size(if (emphasized) 28.dp else 20.dp)
        )
    }
}

private fun ReaderTtsState.playPauseIcon(): Int = if (isPlaying) {
    R.drawable.ic_pause_rounded_24px
} else {
    R.drawable.ic_play_arrow_rounded_24px
}

private fun ReaderTtsState.playPauseLabel(): Int = if (isPlaying) {
    R.string.tts_pause
} else {
    R.string.tts_play
}

private fun ReaderTtsState.headline(): Int = when (failure) {
    TtsFailure.ENGINE_UNAVAILABLE -> R.string.tts_error_engine_unavailable
    TtsFailure.LANGUAGE_MISSING_DATA -> R.string.tts_error_language_missing_data
    TtsFailure.LANGUAGE_NOT_SUPPORTED -> R.string.tts_error_language_not_supported
    TtsFailure.SYNTHESIS_FAILED -> R.string.tts_error_synthesis_failed
    null -> if (playbackState == TtsPlaybackState.PREPARING) {
        R.string.tts_preparing
    } else {
        R.string.tts_panel_title
    }
}

private fun Float.formatRate(): String = "${String.format(Locale.US, "%.1f", this)}x"
