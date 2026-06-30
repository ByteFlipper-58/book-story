/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.translation.language_selection

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.byteflipper.everbook.R
import com.byteflipper.everbook.domain.translation.AUTO_TRANSLATION_LANGUAGE
import com.byteflipper.everbook.domain.translation.DEVICE_TRANSLATION_LANGUAGE
import com.byteflipper.everbook.domain.translation.TranslationProviderMode
import com.byteflipper.everbook.domain.translation.currentDeviceTranslationLanguageCode
import com.byteflipper.everbook.domain.translation.nativeTranslationLanguageName
import com.byteflipper.everbook.domain.translation.provideGoogleTranslateLanguages
import com.byteflipper.everbook.domain.translation.supportedDeviceTranslationLanguageCode
import com.byteflipper.everbook.ui.settings.TranslatorSettingsState
import java.util.Locale

@Composable
internal fun translationLanguageSelectionItems(
    role: TranslationLanguageSelectionRole,
    providerMode: TranslationProviderMode,
    selectedLanguageCode: String,
    requireWifi: Boolean,
    translatorState: TranslatorSettingsState
): List<TranslationLanguageSelectionItem> {
    val items = mutableListOf<TranslationLanguageSelectionItem>()
    val languageItems = mutableListOf<TranslationLanguageSelectionItem>()
    val models = translatorState.models
    val languages = if (providerMode == TranslationProviderMode.IN_APP) {
        models.map { it.language }
    } else {
        provideGoogleTranslateLanguages()
    }.distinctBy { it.code }
        .sortedBy { it.name.lowercase(Locale.ROOT) }
    val supportedCodes = languages.mapTo(mutableSetOf()) { it.code }
    val deviceLanguageCode = supportedDeviceTranslationLanguageCode(supportedCodes)
    val busyLanguageCodes = translatorState.busyLanguageCodes

    if (role == TranslationLanguageSelectionRole.SOURCE) {
        items += TranslationLanguageSelectionItem(
            code = AUTO_TRANSLATION_LANGUAGE,
            title = stringResource(id = R.string.translation_language_auto),
            subtitle = stringResource(id = R.string.translation_language_tap_to_select),
            selected = selectedLanguageCode == AUTO_TRANSLATION_LANGUAGE,
            supported = true,
            busy = false,
            downloaded = true,
            downloadLanguageCode = null
        )
    }

    if (role == TranslationLanguageSelectionRole.TARGET && deviceLanguageCode != null) {
        val deviceModel = models.firstOrNull { it.language.code == deviceLanguageCode }
        items += TranslationLanguageSelectionItem(
            code = DEVICE_TRANSLATION_LANGUAGE,
            title = stringResource(id = R.string.translation_language_device),
            subtitle = nativeTranslationLanguageName(deviceLanguageCode),
            selected = selectedLanguageCode == DEVICE_TRANSLATION_LANGUAGE,
            supported = true,
            busy = deviceLanguageCode in busyLanguageCodes,
            downloaded = if (providerMode == TranslationProviderMode.IN_APP) {
                deviceModel?.downloaded == true
            } else {
                true
            },
            downloadLanguageCode = if (providerMode == TranslationProviderMode.IN_APP) {
                deviceLanguageCode
            } else {
                null
            }
        )
    }

    languages.forEach { language ->
        val model = models.firstOrNull { it.language.code == language.code }
        val downloaded = if (providerMode == TranslationProviderMode.IN_APP) {
            model?.downloaded == true
        } else {
            true
        }
        languageItems += TranslationLanguageSelectionItem(
            code = language.code,
            title = language.name,
            subtitle = languageSubtitle(
                providerMode = providerMode,
                selected = selectedLanguageCode == language.code,
                downloaded = downloaded,
                requireWifi = requireWifi
            ),
            selected = selectedLanguageCode == language.code,
            supported = model?.supported ?: true,
            busy = language.code in busyLanguageCodes,
            downloaded = downloaded,
            downloadLanguageCode = if (providerMode == TranslationProviderMode.IN_APP) {
                language.code
            } else {
                null
            }
        )
    }

    items += languageItems.sortedWith(
        compareByDescending<TranslationLanguageSelectionItem> { item ->
            providerMode == TranslationProviderMode.IN_APP && item.downloaded == true
        }.thenBy { item ->
            item.title.lowercase(Locale.ROOT)
        }
    )

    if (role == TranslationLanguageSelectionRole.TARGET &&
        selectedLanguageCode == DEVICE_TRANSLATION_LANGUAGE &&
        deviceLanguageCode == null
    ) {
        items.add(
            index = 0,
            element = TranslationLanguageSelectionItem(
                code = DEVICE_TRANSLATION_LANGUAGE,
                title = stringResource(id = R.string.translation_language_device),
                subtitle = stringResource(
                    id = R.string.translation_language_device_unsupported,
                    nativeTranslationLanguageName(currentDeviceTranslationLanguageCode())
                ),
                selected = true,
                supported = false,
                busy = false,
                downloaded = true,
                downloadLanguageCode = null
            )
        )
    }

    return items
}

@Composable
private fun languageSubtitle(
    providerMode: TranslationProviderMode,
    selected: Boolean,
    downloaded: Boolean,
    requireWifi: Boolean
): String =
    when {
        selected -> stringResource(id = R.string.translation_language_selected)
        providerMode != TranslationProviderMode.IN_APP ->
            stringResource(id = R.string.translation_language_tap_to_select)

        downloaded -> stringResource(id = R.string.translation_model_downloaded)
        requireWifi -> stringResource(id = R.string.translation_model_not_downloaded_wifi)
        else -> stringResource(id = R.string.translation_model_not_downloaded)
    }
