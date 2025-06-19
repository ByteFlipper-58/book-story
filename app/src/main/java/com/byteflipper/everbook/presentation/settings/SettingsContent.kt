/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.settings

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsContent(
    listState: LazyListState,
    scrollBehavior: TopAppBarScrollBehavior,
    navigateToGeneralSettings: () -> Unit,
    navigateToAppearanceSettings: () -> Unit,
    navigateToReaderSettings: () -> Unit,
    navigateToBrowseSettings: () -> Unit,
    navigateToLibrarySettings: () -> Unit,
    navigateBack: () -> Unit
) {
    SettingsScaffold(
        listState = listState,
        scrollBehavior = scrollBehavior,
        navigateToGeneralSettings = navigateToGeneralSettings,
        navigateToAppearanceSettings = navigateToAppearanceSettings,
        navigateToReaderSettings = navigateToReaderSettings,
        navigateToBrowseSettings = navigateToBrowseSettings,
        navigateToLibrarySettings = navigateToLibrarySettings,
        navigateBack = navigateBack
    )
}