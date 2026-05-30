/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.settings.library.display.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.byteflipper.everbook.R
import com.byteflipper.everbook.domain.library.display.LibraryLayout
import com.byteflipper.everbook.domain.library.display.LibraryTitlePosition
import com.byteflipper.everbook.domain.ui.ButtonItem
import com.byteflipper.everbook.presentation.core.components.settings.ChipsWithTitle
import com.byteflipper.everbook.ui.main.MainEvent
import com.byteflipper.everbook.ui.main.MainModel
import com.byteflipper.everbook.ui.theme.ExpandingTransition

@Composable
fun LibraryTitlePositionOption() {
    val mainModel = hiltViewModel<MainModel>()
    val state = mainModel.state.collectAsStateWithLifecycle()

    ExpandingTransition(visible = state.value.libraryLayout == LibraryLayout.GRID) {
        ChipsWithTitle(
            title = stringResource(id = R.string.title_position_option),
            chips = LibraryTitlePosition.entries.map { item ->
                ButtonItem(
                    id = item.name,
                    title = stringResource(
                        when (item) {
                            LibraryTitlePosition.OFF -> R.string.library_title_position_off
                            LibraryTitlePosition.BELOW -> R.string.library_title_position_below
                            LibraryTitlePosition.INSIDE -> R.string.library_title_position_inside
                        }
                    ),
                    textStyle = androidx.compose.material3.MaterialTheme.typography.labelLarge,
                    selected = item == state.value.libraryTitlePosition
                )
            },
            onClick = { item ->
                mainModel.onEvent(
                    MainEvent.OnChangeLibraryTitlePosition(item.id)
                )
            }
        )
    }
}
