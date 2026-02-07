/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.settings.library.display.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.byteflipper.everbook.R
import com.byteflipper.everbook.domain.library.display.LibraryLayout
import com.byteflipper.everbook.domain.ui.ButtonItem
import com.byteflipper.everbook.presentation.core.components.settings.SegmentedButtonWithTitle
import com.byteflipper.everbook.ui.main.MainEvent
import com.byteflipper.everbook.ui.main.MainModel

@Composable
fun LibraryLayoutOption() {
    val mainModel = hiltViewModel<MainModel>()
    val state = mainModel.state.collectAsStateWithLifecycle()

    SegmentedButtonWithTitle(
        title = stringResource(id = R.string.layout_option),
        buttons = LibraryLayout.entries.map {
            ButtonItem(
                it.name,
                when (it) {
                    LibraryLayout.LIST -> stringResource(id = R.string.layout_list)
                    LibraryLayout.GRID -> stringResource(id = R.string.layout_grid)
                },
                MaterialTheme.typography.labelLarge,
                it == state.value.libraryLayout
            )
        }
    ) {
        mainModel.onEvent(
            MainEvent.OnChangeLibraryLayout(it.id)
        )
    }
}
