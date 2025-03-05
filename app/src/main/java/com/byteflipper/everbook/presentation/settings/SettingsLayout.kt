/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.settings

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DisplaySettings
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.LocalLibrary
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.byteflipper.everbook.R
import com.byteflipper.everbook.presentation.core.components.common.LazyColumnWithScrollbar

@Composable
fun SettingsLayout(
    listState: LazyListState,
    paddingValues: PaddingValues,
    navigateToGeneralSettings: () -> Unit,
    navigateToAppearanceSettings: () -> Unit,
    navigateToReaderSettings: () -> Unit,
    navigateToBrowseSettings: () -> Unit
) {
    LazyColumnWithScrollbar(
        Modifier
            .fillMaxSize()
            .padding(top = paddingValues.calculateTopPadding()),
        state = listState,
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item {
            SettingsLayoutItem(
                icon = Icons.Outlined.DisplaySettings,
                title = stringResource(id = R.string.general_settings),
                description = stringResource(id = R.string.general_settings_desc)
            ) {
                navigateToGeneralSettings()
            }
        }

        item {
            SettingsLayoutItem(
                icon = Icons.Outlined.Palette,
                title = stringResource(id = R.string.appearance_settings),
                description = stringResource(id = R.string.appearance_settings_desc)
            ) {
                navigateToAppearanceSettings()
            }
        }

        item {
            SettingsLayoutItem(
                icon = Icons.Outlined.LocalLibrary,
                title = stringResource(id = R.string.reader_settings),
                description = stringResource(id = R.string.reader_settings_desc)
            ) {
                navigateToReaderSettings()
            }
        }

        item {
            SettingsLayoutItem(
                icon = Icons.Outlined.Explore,
                title = stringResource(id = R.string.browse_settings),
                description = stringResource(id = R.string.browse_settings_desc)
            ) {
                navigateToBrowseSettings()
            }
        }
    }
}