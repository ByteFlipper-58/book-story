/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.domain.translation

import java.util.Locale

const val AUTO_TRANSLATION_LANGUAGE = "auto"
const val DEFAULT_TRANSLATION_TARGET_LANGUAGE = "en"

fun provideLanguages() = listOf(
    Pair("en", "English"),
    Pair("uk", "Українська"),
    Pair("de", "Deutsch"),
    Pair("ar", "اَلْعَرَبِيَّةُ"),
    Pair("es", "Español"),
    Pair("tr", "Türkçe"),
    Pair("fr", "Français"),
    Pair("pl", "Polski"),
    Pair("ru", "Русский"),
    Pair("it", "Italiano"),
    Pair("zh", "汉语"),
    Pair("hi", "हिन्दी"),
    Pair("pt", "Português (Brasil)")
)

fun provideTranslationLanguages(): List<TranslationLanguage> =
    provideLanguages().map { (code, name) ->
        TranslationLanguage(code = code, name = name)
    }

fun normalizeTranslationLanguageCode(code: String?): String? =
    code
        ?.lowercase()
        ?.substringBefore("-")
        ?.substringBefore("_")
        ?.takeIf { it.isNotBlank() }

fun nativeTranslationLanguageName(code: String): String {
    val languageCode = normalizeTranslationLanguageCode(code) ?: code
    val nativeLocale = Locale.forLanguageTag(languageCode)
    val languageName = nativeLocale
        .getDisplayName(nativeLocale)
        .takeIf { it.isNotBlank() }
        ?: languageCode.uppercase(Locale.ROOT)
    return languageName.replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(nativeLocale) else it.toString()
    }
}
