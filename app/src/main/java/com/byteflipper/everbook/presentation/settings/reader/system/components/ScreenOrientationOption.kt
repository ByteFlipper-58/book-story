/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.settings.reader.system.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.byteflipper.everbook.R
import com.byteflipper.everbook.domain.reader.ReaderScreenOrientation
import com.byteflipper.everbook.domain.ui.ButtonItem
import com.byteflipper.everbook.presentation.core.components.settings.ChipsWithTitle
import com.byteflipper.everbook.ui.main.MainEvent
import com.byteflipper.everbook.ui.main.MainModel

@Composable
fun ScreenOrientationOption() {
    val mainModel = hiltViewModel<MainModel>()
    val state = mainModel.state.collectAsStateWithLifecycle()

    ChipsWithTitle(
        title = stringResource(id = R.string.screen_orientation_option),
        chips = ReaderScreenOrientation.entries.map {
            ButtonItem(
                id = it.toString(),
                title = when (it.code) {
                    ReaderScreenOrientation.FREE.code -> stringResource(id = R.string.screen_orientation_free)
                    ReaderScreenOrientation.PORTRAIT.code -> stringResource(id = R.string.screen_orientation_portrait)
                    ReaderScreenOrientation.LANDSCAPE.code -> stringResource(id = R.string.screen_orientation_landscape)
                    ReaderScreenOrientation.LOCKED_PORTRAIT.code -> stringResource(id = R.string.screen_orientation_locked_portrait)
                    ReaderScreenOrientation.LOCKED_LANDSCAPE.code -> stringResource(id = R.string.screen_orientation_locked_landscape)
                    else -> stringResource(id = R.string.default_string)
                },
                textStyle = MaterialTheme.typography.labelLarge,
                selected = it == state.value.screenOrientation
            )
        },
        onClick = {
            mainModel.onEvent(
                MainEvent.OnChangeScreenOrientation(
                    it.id
                )
            )
        }
    )
}