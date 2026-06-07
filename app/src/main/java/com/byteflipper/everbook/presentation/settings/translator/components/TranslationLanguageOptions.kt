/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.settings.translator.components

import com.byteflipper.everbook.domain.translation.TranslationLanguage
import com.byteflipper.everbook.domain.translation.TranslationModelState
import com.byteflipper.everbook.domain.translation.nativeTranslationLanguageName
import java.util.Locale

internal fun List<TranslationModelState>.nativeTranslationLanguages(): List<TranslationLanguage> =
    map {
        it.language.copy(name = nativeTranslationLanguageName(it.language.code))
    }

internal fun List<TranslationLanguage>.sortedBySelected(
    selectedLanguageCode: String
): List<TranslationLanguage> =
    sortedWith(
        compareByDescending<TranslationLanguage> { it.code == selectedLanguageCode }
            .thenBy { it.name.lowercase(Locale.ROOT) }
            .thenBy { it.code }
    )
