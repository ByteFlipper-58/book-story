/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.settings.translator.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.byteflipper.everbook.R
import com.byteflipper.everbook.domain.translation.TranslationFeature
import com.byteflipper.everbook.domain.translation.TranslationProviderMode
import com.byteflipper.everbook.domain.ui.ButtonItem
import com.byteflipper.everbook.presentation.core.components.settings.SegmentedButtonWithTitle
import com.byteflipper.everbook.ui.main.MainEvent
import com.byteflipper.everbook.ui.main.MainModel

@Composable
fun TranslationProviderOption() {
    val mainModel = hiltViewModel<MainModel>()
    val state = mainModel.state.collectAsStateWithLifecycle()

    SegmentedButtonWithTitle(
        title = stringResource(id = R.string.translation_provider_option),
        buttons = TranslationFeature.AVAILABLE_PROVIDER_MODES.map {
            ButtonItem(
                id = it.name,
                title = when (it) {
                    TranslationProviderMode.IN_APP ->
                        stringResource(id = R.string.translation_provider_in_app)

                    TranslationProviderMode.GOOGLE_TRANSLATE ->
                        stringResource(id = R.string.translation_provider_google_translate)

                    TranslationProviderMode.EXTERNAL ->
                        stringResource(id = R.string.translation_provider_external)
                },
                textStyle = MaterialTheme.typography.labelLarge,
                selected = it == state.value.translationProviderMode
            )
        },
        onClick = {
            mainModel.onEvent(MainEvent.OnChangeTranslationProviderMode(it.id))
        }
    )
}
