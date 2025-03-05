/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.settings.reader.progress.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.byteflipper.everbook.R
import com.byteflipper.everbook.domain.ui.ButtonItem
import com.byteflipper.everbook.domain.util.HorizontalAlignment
import com.byteflipper.everbook.presentation.core.components.settings.SegmentedButtonWithTitle
import com.byteflipper.everbook.ui.main.MainEvent
import com.byteflipper.everbook.ui.main.MainModel
import com.byteflipper.everbook.ui.theme.ExpandingTransition

@Composable
fun ProgressBarAlignmentOption() {
    val mainModel = hiltViewModel<MainModel>()
    val state = mainModel.state.collectAsStateWithLifecycle()

    ExpandingTransition(visible = state.value.progressBar) {
        SegmentedButtonWithTitle(
            title = stringResource(id = R.string.progress_bar_alignment_option),
            buttons = HorizontalAlignment.entries.map {
                ButtonItem(
                    id = it.toString(),
                    title = when (it) {
                        HorizontalAlignment.START -> stringResource(id = R.string.alignment_start)
                        HorizontalAlignment.CENTER -> stringResource(id = R.string.alignment_center)
                        HorizontalAlignment.END -> stringResource(id = R.string.alignment_end)
                    },
                    textStyle = MaterialTheme.typography.labelLarge,
                    selected = it == state.value.progressBarAlignment
                )
            },
            onClick = {
                mainModel.onEvent(
                    MainEvent.OnChangeProgressBarAlignment(
                        it.id
                    )
                )
            }
        )
    }
}