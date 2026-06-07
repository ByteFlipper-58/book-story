/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

@file:Suppress("FunctionName")

package com.byteflipper.everbook.presentation.settings.translator

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.byteflipper.everbook.R
import com.byteflipper.everbook.domain.translation.TranslationFeature
import com.byteflipper.everbook.domain.translation.TranslationProviderMode
import com.byteflipper.everbook.presentation.settings.components.SettingsSubcategory
import com.byteflipper.everbook.presentation.settings.translator.components.TranslationOfflineModelsOption
import com.byteflipper.everbook.presentation.settings.translator.components.TranslationProviderDescription
import com.byteflipper.everbook.presentation.settings.translator.components.TranslationProviderOption
import com.byteflipper.everbook.presentation.settings.translator.components.TranslationSourceLanguageOption
import com.byteflipper.everbook.presentation.settings.translator.components.TranslationTargetLanguageOption
import com.byteflipper.everbook.presentation.settings.translator.components.TranslationWifiOnlyOption

fun LazyListScope.TranslatorSettingsCategory(
    titleColor: @Composable () -> Color = { MaterialTheme.colorScheme.primary },
    providerMode: TranslationProviderMode,
    navigateToOfflineModels: () -> Unit
) {
    val mlKitSettingsVisible = providerMode == TranslationProviderMode.IN_APP &&
            TranslationFeature.IN_APP_TRANSLATION_ENABLED
    val languageSettingsVisible = providerMode != TranslationProviderMode.EXTERNAL
    val sourceLanguageVisible = providerMode == TranslationProviderMode.IN_APP

    SettingsSubcategory(
        titleColor = titleColor,
        title = { stringResource(id = R.string.translation_provider_settings) },
        showTitle = true,
        showDivider = languageSettingsVisible || mlKitSettingsVisible
    ) {
        item {
            TranslationProviderOption()
        }
        item {
            TranslationProviderDescription()
        }
    }

    if (languageSettingsVisible) {
        SettingsSubcategory(
            titleColor = titleColor,
            title = { stringResource(id = R.string.translation_language_settings) },
            showTitle = true,
            showDivider = mlKitSettingsVisible
        ) {
            if (sourceLanguageVisible) {
                item(key = "translation_source_language") {
                    TranslationSourceLanguageOption()
                }
            }
            item(key = "translation_target_language") {
                TranslationTargetLanguageOption()
            }
        }
    }

    if (mlKitSettingsVisible) {
        SettingsSubcategory(
            titleColor = titleColor,
            title = { stringResource(id = R.string.translation_ml_kit_settings) },
            showTitle = true,
            showDivider = false
        ) {
            item {
                TranslationWifiOnlyOption()
            }
            item {
                TranslationOfflineModelsOption(
                    navigateToOfflineModels = navigateToOfflineModels
                )
            }
        }
    }
}
