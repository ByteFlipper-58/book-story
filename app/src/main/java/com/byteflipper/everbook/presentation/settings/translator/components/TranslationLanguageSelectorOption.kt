/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.settings.translator.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.byteflipper.everbook.domain.translation.AUTO_TRANSLATION_LANGUAGE
import com.byteflipper.everbook.domain.translation.DEVICE_TRANSLATION_LANGUAGE
import com.byteflipper.everbook.domain.translation.TranslationProviderMode
import com.byteflipper.everbook.domain.translation.provideGoogleTranslateLanguages
import com.byteflipper.everbook.presentation.navigator.LocalNavigator
import com.byteflipper.everbook.presentation.translation.TranslationLanguageSelector
import com.byteflipper.everbook.ui.main.MainEvent
import com.byteflipper.everbook.ui.main.MainModel
import com.byteflipper.everbook.ui.settings.TranslatorSettingsModel
import com.byteflipper.everbook.presentation.translation.language_selection.TranslationLanguageSelectionRole
import com.byteflipper.everbook.ui.translation.TranslationLanguageSelectionScreen
import com.byteflipper.everbook.ui.translation.TranslationLanguageSelectionTarget

@Composable
fun TranslationLanguageSelectorOption() {
    val navigator = LocalNavigator.current
    val mainModel = hiltViewModel<MainModel>()
    val mainState = mainModel.state.collectAsStateWithLifecycle()
    val model = hiltViewModel<TranslatorSettingsModel>()
    val modelState = model.state.collectAsStateWithLifecycle()
    val providerMode = mainState.value.translationProviderMode
    val languages = if (providerMode == TranslationProviderMode.IN_APP) {
        modelState.value.models.nativeTranslationLanguages()
    } else {
        provideGoogleTranslateLanguages()
    }
    val sourceLanguageCode = mainState.value.translationSourceLanguage
    val targetLanguageCode = mainState.value.translationTargetLanguage

    LaunchedEffect(providerMode, languages, sourceLanguageCode, targetLanguageCode) {
        if (languages.isEmpty()) return@LaunchedEffect
        val supportedCodes = languages.mapTo(mutableSetOf()) { it.code }
        if (sourceLanguageCode != AUTO_TRANSLATION_LANGUAGE && sourceLanguageCode !in supportedCodes) {
            mainModel.onEvent(
                MainEvent.OnChangeTranslationSourceLanguage(AUTO_TRANSLATION_LANGUAGE)
            )
        }
        if (targetLanguageCode != DEVICE_TRANSLATION_LANGUAGE && targetLanguageCode !in supportedCodes) {
            mainModel.onEvent(
                MainEvent.OnChangeTranslationTargetLanguage(DEVICE_TRANSLATION_LANGUAGE)
            )
        }
    }

    TranslationLanguageSelector(
        sourceLanguageCode = sourceLanguageCode,
        targetLanguageCode = targetLanguageCode,
        languages = languages,
        allowAutoSource = true,
        allowDeviceTarget = true,
        onSourceLanguageClick = {
            navigator.push(
                TranslationLanguageSelectionScreen(
                    target = TranslationLanguageSelectionTarget.SETTINGS,
                    role = TranslationLanguageSelectionRole.SOURCE
                )
            )
        },
        onTargetLanguageClick = {
            navigator.push(
                TranslationLanguageSelectionScreen(
                    target = TranslationLanguageSelectionTarget.SETTINGS,
                    role = TranslationLanguageSelectionRole.TARGET
                )
            )
        },
        onSwapLanguages = {
            mainModel.onEvent(MainEvent.OnSwapTranslationLanguages)
        }
    )
}
