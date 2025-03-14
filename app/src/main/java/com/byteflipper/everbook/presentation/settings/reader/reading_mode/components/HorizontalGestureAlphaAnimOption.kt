/*
 * Book's Story — free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.settings.reader.reading_mode.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.byteflipper.everbook.R
import com.byteflipper.everbook.domain.reader.ReaderHorizontalGesture
import com.byteflipper.everbook.presentation.core.components.settings.SliderWithTitle
import com.byteflipper.everbook.ui.main.MainEvent
import com.byteflipper.everbook.ui.main.MainModel
import com.byteflipper.everbook.ui.theme.ExpandingTransition

@Composable
fun HorizontalGestureAlphaAnimOption() {
    val mainModel = hiltViewModel<MainModel>()
    val state = mainModel.state.collectAsStateWithLifecycle()

    ExpandingTransition(
        visible = when (state.value.horizontalGesture) {
            ReaderHorizontalGesture.OFF -> false
            else -> true
        }
    ) {
        SliderWithTitle(
            value = state.value.horizontalGestureAlphaAnim to "%",
            toValue = 100,
            title = stringResource(id = R.string.horizontal_gesture_alpha_anim_option),
            onValueChange = {
                mainModel.onEvent(
                    MainEvent.OnChangeHorizontalGestureAlphaAnim(it)
                )
            }
        )
    }
}