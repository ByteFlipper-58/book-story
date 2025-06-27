/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.settings.reader.misc.components

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.byteflipper.everbook.presentation.core.components.settings.SwitchWithTitle
import com.byteflipper.everbook.ui.main.MainEvent
import com.byteflipper.everbook.ui.main.MainModel
import androidx.compose.ui.res.stringResource

@Composable
fun RenderMathOption() {
    val mainModel = hiltViewModel<MainModel>()
    val state = mainModel.state.collectAsStateWithLifecycle()

    SwitchWithTitle(
        selected = state.value.renderMath,
        title = stringResource(id = com.byteflipper.everbook.R.string.math_renderer_option),
        description = stringResource(id = com.byteflipper.everbook.R.string.math_renderer_option_desc),
        onClick = {
            mainModel.onEvent(MainEvent.OnChangeRenderMath(!state.value.renderMath))
        }
    )
} 