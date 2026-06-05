/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.ui.reader

import androidx.compose.runtime.Immutable
import com.byteflipper.everbook.domain.translation.AUTO_TRANSLATION_LANGUAGE
import com.byteflipper.everbook.domain.translation.DEFAULT_TRANSLATION_TARGET_LANGUAGE
import com.byteflipper.everbook.domain.translation.TranslationCapability
import com.byteflipper.everbook.domain.translation.TranslationProviderMode

@Immutable
data class ReaderTranslationState(
    val text: String = "",
    val readerTextIndex: Int? = null,
    val showOriginal: Boolean = false,
    val sourceLanguageCode: String = AUTO_TRANSLATION_LANGUAGE,
    val detectedSourceLanguageCode: String? = null,
    val targetLanguageCode: String = DEFAULT_TRANSLATION_TARGET_LANGUAGE,
    val providerMode: TranslationProviderMode = TranslationProviderMode.EXTERNAL,
    val requireWifi: Boolean = true,
    val capability: TranslationCapability = TranslationCapability(
        inAppAvailable = false
    ),
    val translatedText: String? = null,
    val isTranslating: Boolean = false,
    val errorMessage: String? = null
) {
    val resolvedSourceLanguageCode: String?
        get() = detectedSourceLanguageCode ?: sourceLanguageCode.takeIf {
            it != AUTO_TRANSLATION_LANGUAGE
        }

    val inAppRequested: Boolean
        get() = providerMode == TranslationProviderMode.IN_APP
}
