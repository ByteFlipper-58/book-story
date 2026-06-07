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
import com.byteflipper.everbook.domain.translation.TranslationProviderMode
import com.byteflipper.everbook.domain.translation.provideTranslationLanguages
import com.byteflipper.everbook.domain.ui.ButtonItem
import com.byteflipper.everbook.ui.main.MainEvent
import com.byteflipper.everbook.ui.main.MainModel
import com.byteflipper.everbook.ui.settings.TranslatorSettingsModel

@Composable
fun TranslationTargetLanguageOption() {
    val mainModel = hiltViewModel<MainModel>()
    val mainState = mainModel.state.collectAsStateWithLifecycle()
    val languages = if (
        mainState.value.translationProviderMode == TranslationProviderMode.IN_APP
    ) {
        val model = hiltViewModel<TranslatorSettingsModel>()
        val state = model.state.collectAsStateWithLifecycle()
        state.value.models.nativeTranslationLanguages()
    } else {
        provideTranslationLanguages()
    }

    CollapsibleTranslationChipsWithTitle(
        stateKey = "translation_target_language",
        title = stringResource(id = R.string.translation_target_language_option),
        chips = languages
            .distinctBy { it.code }
            .sortedBySelected(mainState.value.translationTargetLanguage)
            .map {
            ButtonItem(
                id = it.code,
                title = it.name,
                textStyle = MaterialTheme.typography.labelLarge,
                selected = it.code == mainState.value.translationTargetLanguage
            )
        },
        onClick = {
            mainModel.onEvent(MainEvent.OnChangeTranslationTargetLanguage(it.id))
        }
    )
}
