/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.settings.browse.display.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.byteflipper.everbook.R
import com.byteflipper.everbook.domain.browse.display.BrowseLayout
import com.byteflipper.everbook.presentation.core.components.settings.SliderWithTitle
import com.byteflipper.everbook.ui.main.MainEvent
import com.byteflipper.everbook.ui.main.MainModel
import com.byteflipper.everbook.ui.theme.ExpandingTransition

@Composable
fun BrowseGridSizeOption() {
    val mainModel = hiltViewModel<MainModel>()
    val state = mainModel.state.collectAsStateWithLifecycle()

    ExpandingTransition(visible = state.value.browseLayout == BrowseLayout.GRID) {
        SliderWithTitle(
            value = state.value.browseGridSize
                    to " ${stringResource(R.string.browse_grid_size_per_row)}",
            valuePlaceholder = stringResource(id = R.string.browse_grid_size_auto),
            showPlaceholder = state.value.browseAutoGridSize,
            fromValue = 0,
            toValue = 10,
            title = stringResource(id = R.string.browse_grid_size_option),
            onValueChange = {
                mainModel.onEvent(MainEvent.OnChangeBrowseAutoGridSize(it == 0))
                mainModel.onEvent(
                    MainEvent.OnChangeBrowseGridSize(it)
                )
            }
        )
    }
}