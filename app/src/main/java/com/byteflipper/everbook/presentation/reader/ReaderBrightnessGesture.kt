/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.reader

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/**
 * Brightness controls placed over the narrow edges of the reader. Keeping the gesture hit areas
 * separate from the reading surface lets regular vertical scrolling continue to work unchanged.
 */
@Composable
fun BoxScope.ReaderBrightnessGesture(
    enabled: Boolean,
    brightness: Float,
    onBrightnessChange: (Float) -> Unit
) {
    if (!enabled) return

    val currentBrightness by rememberUpdatedState(brightness)
    val currentOnBrightnessChange by rememberUpdatedState(onBrightnessChange)
    var indicatorVisible by remember { mutableStateOf(false) }
    var indicatorBrightness by remember { mutableFloatStateOf(brightness) }

    @Composable
    fun EdgeControl(alignment: Alignment) {
        Box(
            modifier = Modifier
                .align(alignment)
                .fillMaxHeight()
                .width(32.dp)
                .pointerInput(Unit) {
                    var dragStartBrightness = currentBrightness
                    var accumulatedDrag = 0f
                    detectVerticalDragGestures(
                        onDragStart = {
                            dragStartBrightness = currentBrightness
                            accumulatedDrag = 0f
                            indicatorBrightness = dragStartBrightness
                            indicatorVisible = true
                        },
                        onVerticalDrag = { change, dragAmount ->
                            change.consume()
                            accumulatedDrag += dragAmount
                            val updatedBrightness = (dragStartBrightness -
                                    accumulatedDrag / (size.height * 0.8f))
                                .coerceIn(MIN_BRIGHTNESS, MAX_BRIGHTNESS)
                            indicatorBrightness = updatedBrightness
                            currentOnBrightnessChange(updatedBrightness)
                        },
                        onDragCancel = { indicatorVisible = false },
                        onDragEnd = { indicatorVisible = false }
                    )
                }
        )
    }

    EdgeControl(Alignment.CenterStart)
    EdgeControl(Alignment.CenterEnd)

    AnimatedVisibility(
        visible = indicatorVisible,
        modifier = Modifier.align(Alignment.Center),
        enter = fadeIn() + scaleIn(initialScale = 0.8f),
        exit = fadeOut() + scaleOut(targetScale = 0.8f)
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.inverseSurface,
            contentColor = MaterialTheme.colorScheme.inverseOnSurface,
            shadowElevation = 8.dp,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 32.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = when {
                        indicatorBrightness < 0.33f -> Icons.Filled.Brightness4
                        indicatorBrightness < 0.66f -> Icons.Filled.Brightness6
                        else -> Icons.Filled.Brightness7
                    },
                    contentDescription = null,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${(indicatorBrightness * 100).roundToInt()}%",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { indicatorBrightness },
                    modifier = Modifier
                        .width(100.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    trackColor = MaterialTheme.colorScheme.inverseOnSurface.copy(alpha = 0.3f)
                )
            }
        }
    }
}

private const val MIN_BRIGHTNESS = 0.01f
private const val MAX_BRIGHTNESS = 1f
