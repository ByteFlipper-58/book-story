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
import androidx.compose.ui.unit.sp
import com.byteflipper.everbook.R
import com.byteflipper.everbook.ui.reader.ReaderEvent
import java.util.Locale

private const val MIN_SPEED = 1.0f
private const val MAX_SPEED = 10.0f
private const val SPEED_STEP = 0.5f

/** Floating controls of a running auto-scroll session. */
@Composable
fun AutoScrollControlPanel(
    isAutoScrolling: Boolean,
    autoScrollSpeed: Float,
    isAutoScrollPaused: Boolean,
    chipAlignment: String,
    chipOpacity: Int,
    chipOpacityEnabled: Boolean,
    chipPlayPause: Boolean,
    bookId: Int,
    onSetAutoScrollPaused: (ReaderEvent.OnSetAutoScrollPaused) -> Unit,
    onChangeAutoScrollSpeed: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val collapseState = rememberReaderPanelCollapseState(
        active = isAutoScrolling,
        resetKey = bookId
    )

    FloatingReaderControlPanel(
        visible = isAutoScrolling,
        collapseState = collapseState,
        alignment = chipAlignment,
        collapsedWidthFraction = if (chipPlayPause) 0.32f else 0.22f,
        expandedHeight = 120.dp,
        collapsedAlpha = if (chipOpacityEnabled) chipOpacity / 100f else 1f,
        modifier = modifier,
        collapsedContent = {
            if (chipPlayPause) {
                IconButton(
                    onClick = {
                        onSetAutoScrollPaused(
                            ReaderEvent.OnSetAutoScrollPaused(!isAutoScrollPaused)
                        )
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        painter = painterResource(
                            id = if (isAutoScrollPaused) {
                                R.drawable.ic_play_arrow_rounded_24px
                            } else {
                                R.drawable.ic_pause_rounded_24px
                            }
                        ),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
            }

            Icon(
                painter = painterResource(id = R.drawable.ic_speed),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = autoScrollSpeed.formatSpeed(),
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
                        painter = painterResource(id = R.drawable.ic_speed),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(id = R.string.auto_scroll_reader_settings),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
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
                        text = autoScrollSpeed.formatSpeed(),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            ReaderControlTrack(
                value = autoScrollSpeed,
                valueRange = MIN_SPEED..MAX_SPEED,
                step = SPEED_STEP,
                onValueChange = onChangeAutoScrollSpeed,
                onInteraction = collapseState::touch,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = stringResource(id = R.string.auto_scroll_tap_to_stop),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 9.sp,
                    lineHeight = 11.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    )
}

private fun Float.formatSpeed(): String = "${String.format(Locale.US, "%.1f", this)}x"
