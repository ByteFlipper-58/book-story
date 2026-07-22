/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.settings.translator.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.byteflipper.everbook.R
import com.byteflipper.everbook.domain.translation.TranslationProviderMode
import com.byteflipper.everbook.presentation.settings.components.SettingsSubcategoryNote
import com.byteflipper.everbook.ui.main.MainModel

@Composable
fun TranslationProviderDescription() {
    val mainModel = hiltViewModel<MainModel>()
    val state = mainModel.state.collectAsStateWithLifecycle()

    SettingsSubcategoryNote(
        text = stringResource(
            id = when (state.value.translationProviderMode) {
                TranslationProviderMode.IN_APP ->
                    R.string.translation_provider_ml_kit_desc

                TranslationProviderMode.GOOGLE_TRANSLATE ->
                    R.string.translation_provider_google_translate_desc

                TranslationProviderMode.EXTERNAL ->
                    R.string.translation_provider_external_desc
            }
        ),
        verticalPadding = 4.dp
    )
}
