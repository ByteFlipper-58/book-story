/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.domain.translation

import androidx.compose.runtime.Immutable

@Immutable
data class TranslationCapability(
    val inAppAvailable: Boolean,
    val googleTranslateAvailable: Boolean = true
) {
    fun isAvailable(providerMode: TranslationProviderMode): Boolean =
        when (providerMode) {
            TranslationProviderMode.IN_APP -> inAppAvailable
            TranslationProviderMode.GOOGLE_TRANSLATE -> googleTranslateAvailable
            TranslationProviderMode.EXTERNAL -> true
        }
}
