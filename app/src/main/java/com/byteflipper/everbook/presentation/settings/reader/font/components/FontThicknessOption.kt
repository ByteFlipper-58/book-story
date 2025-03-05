/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.settings.reader.font.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.byteflipper.everbook.R
import com.byteflipper.everbook.domain.reader.ReaderFontThickness
import com.byteflipper.everbook.domain.ui.ButtonItem
import com.byteflipper.everbook.presentation.core.components.settings.ChipsWithTitle
import com.byteflipper.everbook.presentation.core.constants.Constants
import com.byteflipper.everbook.presentation.core.constants.provideFonts
import com.byteflipper.everbook.ui.main.MainEvent
import com.byteflipper.everbook.ui.main.MainModel

@Composable
fun FontThicknessOption() {
    val mainModel = hiltViewModel<MainModel>()
    val state = mainModel.state.collectAsStateWithLifecycle()

    val fontFamily = remember(state.value.fontFamily) {
        Constants.provideFonts().run {
            find {
                it.id == state.value.fontFamily
            } ?: get(0)
        }
    }

    ChipsWithTitle(
        title = stringResource(id = R.string.font_thickness_option),
        chips = ReaderFontThickness.entries.map {
            ButtonItem(
                id = it.toString(),
                title = when (it) {
                    ReaderFontThickness.THIN -> stringResource(id = R.string.font_thickness_thin)
                    ReaderFontThickness.EXTRA_LIGHT -> stringResource(id = R.string.font_thickness_extra_light)
                    ReaderFontThickness.LIGHT -> stringResource(id = R.string.font_thickness_light)
                    ReaderFontThickness.NORMAL -> stringResource(id = R.string.font_thickness_normal)
                    ReaderFontThickness.MEDIUM -> stringResource(id = R.string.font_thickness_medium)
                },
                textStyle = MaterialTheme.typography.labelLarge.copy(
                    fontFamily = fontFamily.font,
                    fontWeight = it.thickness
                ),
                selected = it == state.value.fontThickness
            )
        },
        onClick = {
            mainModel.onEvent(
                MainEvent.OnChangeFontThickness(
                    it.id
                )
            )
        }
    )
}