/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.settings.appearance.theme_preferences.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.byteflipper.everbook.R
import com.byteflipper.everbook.domain.ui.ButtonItem
import com.byteflipper.everbook.presentation.core.components.settings.SegmentedButtonWithTitle
import com.byteflipper.everbook.ui.main.MainEvent
import com.byteflipper.everbook.ui.main.MainModel
import com.byteflipper.everbook.ui.theme.ExpandingTransition
import com.byteflipper.everbook.domain.ui.PureDark
import com.byteflipper.everbook.domain.ui.isDark

@Composable
fun PureDarkOption() {
    val mainModel = hiltViewModel<MainModel>()
    val state = mainModel.state.collectAsStateWithLifecycle()

    ExpandingTransition(visible = state.value.darkTheme.isDark()) {
        SegmentedButtonWithTitle(
            title = stringResource(id = R.string.pure_dark_option),
            buttons = PureDark.entries.map {
                ButtonItem(
                    id = it.toString(),
                    title = when (it) {
                        PureDark.OFF -> stringResource(id = R.string.pure_dark_off)
                        PureDark.ON -> stringResource(id = R.string.pure_dark_on)
                        PureDark.SAVER -> stringResource(id = R.string.pure_dark_power_saver)
                    },
                    textStyle = MaterialTheme.typography.labelLarge,
                    selected = it == state.value.pureDark
                )
            }
        ) {
            mainModel.onEvent(
                MainEvent.OnChangePureDark(
                    it.id
                )
            )
        }
    }
}