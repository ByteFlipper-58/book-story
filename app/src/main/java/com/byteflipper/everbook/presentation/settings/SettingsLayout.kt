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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.byteflipper.everbook.R
import com.byteflipper.everbook.domain.translation.TranslationFeature
import com.byteflipper.everbook.presentation.core.components.common.LazyColumnWithScrollbar
import com.byteflipper.everbook.ui.settings.PrivacySettingsEntry

@Composable
fun SettingsLayout(
    listState: LazyListState,
    paddingValues: PaddingValues,
    navigateToGeneralSettings: () -> Unit,
    navigateToAppearanceSettings: () -> Unit,
    navigateToReaderSettings: () -> Unit,
    navigateToBrowseSettings: () -> Unit,
    navigateToTranslatorSettings: () -> Unit,
    navigateToLibrarySettings: () -> Unit,
    navigateToPrivacySettings: () -> Unit
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
                index = 0,
                icon = R.drawable.ic_display_settings_rounded_24px,
                title = stringResource(id = R.string.general_settings),
                description = stringResource(id = R.string.general_settings_desc)
            ) {
                navigateToGeneralSettings()
            }
        }

        item {
            SettingsLayoutItem(
                index = 1,
                icon = R.drawable.ic_palette_rounded_24px,
                title = stringResource(id = R.string.appearance_settings),
                description = stringResource(id = R.string.appearance_settings_desc)
            ) {
                navigateToAppearanceSettings()
            }
        }

        item {
            SettingsLayoutItem(
                index = 2,
                icon = R.drawable.ic_local_library_rounded_24px,
                title = stringResource(id = R.string.library_settings),
                description = stringResource(id = R.string.library_settings_desc)
            ) {
                navigateToLibrarySettings()
            }
        }

        item {
            SettingsLayoutItem(
                index = 3,
                icon = R.drawable.ic_menu_book_rounded_24px,
                title = stringResource(id = R.string.reader_settings),
                description = stringResource(id = R.string.reader_settings_desc)
            ) {
                navigateToReaderSettings()
            }
        }

        item {
            SettingsLayoutItem(
                index = 4,
                icon = R.drawable.ic_explore_rounded_24px,
                title = stringResource(id = R.string.browse_settings),
                description = stringResource(id = R.string.browse_settings_desc)
            ) {
                navigateToBrowseSettings()
            }
        }

        if (TranslationFeature.INLINE_TRANSLATION_ENABLED) {
            item {
                SettingsLayoutItem(
                    index = 5,
                    icon = R.drawable.ic_language_rounded_24px,
                    title = stringResource(id = R.string.translator_settings),
                    description = stringResource(id = R.string.translator_settings_desc)
                ) {
                    navigateToTranslatorSettings()
                }
            }
        }

        item {
            PrivacySettingsEntry(
                index = if (TranslationFeature.INLINE_TRANSLATION_ENABLED) 6 else 5,
                navigateToPrivacySettings = navigateToPrivacySettings
            )
        }
    }
}
