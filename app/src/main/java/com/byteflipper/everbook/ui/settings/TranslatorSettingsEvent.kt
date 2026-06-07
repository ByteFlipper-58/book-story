/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.ui.settings

import androidx.compose.runtime.Immutable

@Immutable
sealed class TranslatorSettingsEvent {
    data object OnRefreshModels : TranslatorSettingsEvent()

    data class OnChangeModelSearchQuery(
        val value: String
    ) : TranslatorSettingsEvent()

    data class OnChangeModelFilter(
        val filter: TranslationModelFilter
    ) : TranslatorSettingsEvent()

    data class OnDownloadModel(
        val languageCode: String,
        val requireWifi: Boolean
    ) : TranslatorSettingsEvent()

    data class OnDeleteModel(
        val languageCode: String
    ) : TranslatorSettingsEvent()

    data object OnDismissModelError : TranslatorSettingsEvent()
}
