/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.ui.settings

import android.os.Parcelable
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import kotlinx.parcelize.Parcelize
import com.byteflipper.everbook.domain.navigator.Screen
import com.byteflipper.everbook.presentation.core.components.top_bar.collapsibleTopAppBarScrollBehavior
import com.byteflipper.everbook.presentation.navigator.LocalNavigator
import com.byteflipper.everbook.presentation.settings.SettingsContent
import com.byteflipper.everbook.ui.settings.LibrarySettingsScreen

@Parcelize
object SettingsScreen : Screen, Parcelable {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val (scrollBehavior, listState) = TopAppBarDefaults.collapsibleTopAppBarScrollBehavior()

        SettingsContent(
            listState = listState,
            scrollBehavior = scrollBehavior,
            navigateToGeneralSettings = {
                navigator.push(GeneralSettingsScreen)
            },
            navigateToAppearanceSettings = {
                navigator.push(AppearanceSettingsScreen)
            },
            navigateToReaderSettings = {
                navigator.push(ReaderSettingsScreen)
            },
            navigateToBrowseSettings = {
                navigator.push(BrowseSettingsScreen)
            },
            navigateToLibrarySettings = {
                navigator.push(LibrarySettingsScreen)
            },
            navigateBack = {
                navigator.pop()
            }
        )
    }
}