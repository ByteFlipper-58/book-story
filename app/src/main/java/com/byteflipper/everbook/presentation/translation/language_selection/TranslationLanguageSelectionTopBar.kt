/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.byteflipper.everbook.presentation.translation.language_selection

import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.byteflipper.everbook.R
import com.byteflipper.everbook.domain.translation.TranslationProviderMode
import com.byteflipper.everbook.presentation.core.components.common.IconButton
import com.byteflipper.everbook.presentation.core.components.common.StyledText
import com.byteflipper.everbook.presentation.navigator.NavigatorBackIconButton

@Composable
internal fun TranslationLanguageSelectionTopBar(
    role: TranslationLanguageSelectionRole,
    providerMode: TranslationProviderMode,
    modelManagerAvailable: Boolean,
    isLoadingModels: Boolean,
    scrollBehavior: TopAppBarScrollBehavior,
    navigateBack: () -> Unit,
    refreshModels: () -> Unit
) {
    LargeTopAppBar(
        title = {
            StyledText(
                text = stringResource(
                    id = when (role) {
                        TranslationLanguageSelectionRole.SOURCE ->
                            R.string.translation_select_source_language

                        TranslationLanguageSelectionRole.TARGET ->
                            R.string.translation_select_target_language
                    }
                )
            )
        },
        navigationIcon = {
            NavigatorBackIconButton(navigateBack = navigateBack)
        },
        actions = {
            if (providerMode == TranslationProviderMode.IN_APP) {
                IconButton(
                    icon = R.drawable.ic_refresh_rounded_24px,
                    contentDescription = R.string.translation_refresh_models_content_desc,
                    disableOnClick = false,
                    enabled = modelManagerAvailable && !isLoadingModels,
                    onClick = refreshModels
                )
            }
        },
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    )
}
