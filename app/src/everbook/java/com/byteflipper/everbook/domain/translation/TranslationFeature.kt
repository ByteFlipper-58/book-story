/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.domain.translation

object TranslationFeature {
    const val IN_APP_TRANSLATION_ENABLED = false
    const val INLINE_TRANSLATION_ENABLED = true
    const val FULL_BOOK_TRANSLATION_ENABLED = true

    val AVAILABLE_PROVIDER_MODES = listOf(
        TranslationProviderMode.GOOGLE_TRANSLATE,
        TranslationProviderMode.EXTERNAL
    )
    val DEFAULT_PROVIDER_MODE = TranslationProviderMode.GOOGLE_TRANSLATE

    val AVAILABLE_FULL_BOOK_PROVIDER_MODES = listOf(
        TranslationProviderMode.GOOGLE_TRANSLATE
    )
    val DEFAULT_FULL_BOOK_PROVIDER_MODE = TranslationProviderMode.GOOGLE_TRANSLATE

    fun coerceProviderMode(providerMode: TranslationProviderMode): TranslationProviderMode =
        providerMode.takeIf { it in AVAILABLE_PROVIDER_MODES } ?: DEFAULT_PROVIDER_MODE

    fun coerceFullBookProviderMode(providerMode: TranslationProviderMode): TranslationProviderMode =
        providerMode.takeIf { it in AVAILABLE_FULL_BOOK_PROVIDER_MODES }
            ?: DEFAULT_FULL_BOOK_PROVIDER_MODE
}
