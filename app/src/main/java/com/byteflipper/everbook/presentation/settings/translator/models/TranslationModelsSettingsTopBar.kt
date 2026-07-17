/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.settings.translator.models

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.byteflipper.everbook.R
import com.byteflipper.everbook.presentation.core.components.common.IconButton
import com.byteflipper.everbook.presentation.core.components.common.StyledText
import com.byteflipper.everbook.presentation.navigator.NavigatorBackIconButton
import com.byteflipper.everbook.ui.settings.TranslatorSettingsEvent
import com.byteflipper.everbook.ui.settings.TranslatorSettingsModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TranslationModelsSettingsTopBar(
    scrollBehavior: TopAppBarScrollBehavior,
    navigateBack: () -> Unit
) {
    val model = hiltViewModel<TranslatorSettingsModel>()
    val state = model.state.collectAsStateWithLifecycle()

    LargeTopAppBar(
        title = {
            StyledText(stringResource(id = R.string.translation_models_settings))
        },
        navigationIcon = {
            NavigatorBackIconButton(
                navigateBack = navigateBack
            )
        },
        actions = {
            IconButton(
                icon = R.drawable.ic_refresh_rounded_24px,
                contentDescription = R.string.translation_refresh_models_content_desc,
                disableOnClick = false,
                enabled = state.value.modelManagerAvailable && !state.value.isLoadingModels,
                onClick = {
                    model.onEvent(TranslatorSettingsEvent.OnRefreshModels)
                }
            )
        },
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    )
}
