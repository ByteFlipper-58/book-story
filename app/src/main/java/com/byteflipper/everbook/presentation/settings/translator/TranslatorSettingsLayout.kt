/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.settings.translator

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.byteflipper.everbook.presentation.core.components.common.LazyColumnWithScrollbar
import com.byteflipper.everbook.ui.main.MainModel

@Composable
fun TranslatorSettingsLayout(
    listState: LazyListState,
    paddingValues: PaddingValues,
    navigateToOfflineModels: () -> Unit
) {
    val mainModel = hiltViewModel<MainModel>()
    val mainState = mainModel.state.collectAsStateWithLifecycle()

    LazyColumnWithScrollbar(
        Modifier
            .fillMaxSize()
            .padding(top = paddingValues.calculateTopPadding()),
        state = listState
    ) {
        TranslatorSettingsCategory(
            providerMode = mainState.value.translationProviderMode,
            navigateToOfflineModels = navigateToOfflineModels
        )
    }
}
