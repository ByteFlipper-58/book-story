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
import com.byteflipper.everbook.domain.translation.AUTO_TRANSLATION_LANGUAGE
import com.byteflipper.everbook.domain.translation.TranslationLanguage
import com.byteflipper.everbook.domain.translation.provideTranslationLanguages
import com.byteflipper.everbook.domain.ui.ButtonItem
import com.byteflipper.everbook.presentation.core.components.settings.ChipsWithTitle
import com.byteflipper.everbook.ui.main.MainEvent
import com.byteflipper.everbook.ui.main.MainModel

@Composable
fun TranslationSourceLanguageOption() {
    val mainModel = hiltViewModel<MainModel>()
    val state = mainModel.state.collectAsStateWithLifecycle()
    val languages = listOf(
        TranslationLanguage(
            code = AUTO_TRANSLATION_LANGUAGE,
            name = stringResource(id = R.string.translation_language_auto)
        )
    ) + provideTranslationLanguages()

    ChipsWithTitle(
        title = stringResource(id = R.string.translation_source_language_option),
        chips = languages.map {
            ButtonItem(
                id = it.code,
                title = it.name,
                textStyle = MaterialTheme.typography.labelLarge,
                selected = it.code == state.value.translationSourceLanguage
            )
        },
        onClick = {
            mainModel.onEvent(MainEvent.OnChangeTranslationSourceLanguage(it.id))
        }
    )
}
