/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.start

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.byteflipper.everbook.domain.navigator.StackEvent
import com.byteflipper.everbook.ui.theme.Transitions

@Composable
fun StartContentTransition(
    modifier: Modifier = Modifier,
    targetValue: String,
    stackEvent: StackEvent,
    content: @Composable (String) -> Unit
) {
    AnimatedContent(
        modifier = Modifier
            .fillMaxSize()
            .then(modifier),
        targetState = targetValue,
        transitionSpec = {
            when (stackEvent) {
                StackEvent.Default -> {
                    Transitions.SlidingTransitionIn
                        .togetherWith(Transitions.SlidingTransitionOut)
                }

                StackEvent.Pop -> {
                    Transitions.BackSlidingTransitionIn
                        .togetherWith(Transitions.BackSlidingTransitionOut)
                }
            }
        },
        content = { content(it) }
    )
}