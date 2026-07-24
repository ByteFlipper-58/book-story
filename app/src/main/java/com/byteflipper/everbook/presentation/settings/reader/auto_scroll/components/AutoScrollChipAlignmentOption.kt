/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.settings.reader.auto_scroll.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.byteflipper.everbook.R
import com.byteflipper.everbook.domain.ui.ButtonItem
import com.byteflipper.everbook.presentation.core.components.settings.SegmentedButtonWithTitle
import com.byteflipper.everbook.ui.main.MainEvent
import com.byteflipper.everbook.ui.main.MainModel

@Composable
fun AutoScrollChipAlignmentOption() {
    val mainModel = hiltViewModel<MainModel>()
    val state = mainModel.state.collectAsStateWithLifecycle()

    SegmentedButtonWithTitle(
        title = stringResource(id = R.string.auto_scroll_chip_alignment_option),
        buttons = listOf("BOTTOM_LEFT", "BOTTOM_CENTER", "BOTTOM_RIGHT").map {
            ButtonItem(
                id = it,
                title = when (it) {
                    "BOTTOM_LEFT" -> stringResource(id = R.string.alignment_bottom_left)
                    "BOTTOM_CENTER" -> stringResource(id = R.string.alignment_bottom_center)
                    else -> stringResource(id = R.string.alignment_bottom_right)
                },
                textStyle = MaterialTheme.typography.labelLarge,
                selected = it == state.value.autoScrollChipAlignment
            )
        },
        onClick = {
            mainModel.onEvent(
                MainEvent.OnChangeAutoScrollChipAlignment(
                    it.id
                )
            )
        }
    )
}
