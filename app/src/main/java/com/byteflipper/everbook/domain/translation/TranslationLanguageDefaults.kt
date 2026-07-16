/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.domain.translation

import android.content.res.Resources
import java.util.Locale

const val AUTO_TRANSLATION_LANGUAGE = "auto"
const val DEVICE_TRANSLATION_LANGUAGE = "device"
const val FALLBACK_TRANSLATION_TARGET_LANGUAGE = "en"
const val DEFAULT_TRANSLATION_TARGET_LANGUAGE = DEVICE_TRANSLATION_LANGUAGE

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

fun provideGoogleTranslateLanguages(): List<TranslationLanguage> =
    listOf(
        "af", "sq", "am", "ar", "hy", "az", "eu", "be", "bn", "bs", "bg", "ca",
        "ceb", "ny", "zh", "co", "hr", "cs", "da", "nl", "en", "eo", "et", "tl",
        "fi", "fr", "fy", "gl", "ka", "de", "el", "gu", "ht", "ha", "haw", "he",
        "hi", "hmn", "hu", "is", "ig", "id", "ga", "it", "ja", "jv", "kn", "kk",
        "km", "ko", "ku", "ky", "lo", "la", "lv", "lt", "lb", "mk", "mg", "ms",
        "ml", "mt", "mi", "mr", "mn", "my", "ne", "no", "ps", "fa", "pl", "pt",
        "pa", "ro", "ru", "sm", "gd", "sr", "st", "sn", "sd", "si", "sk", "sl",
        "so", "es", "su", "sw", "sv", "tg", "ta", "te", "th", "tr", "uk", "ur",
        "uz", "vi", "cy", "xh", "yi", "yo", "zu"
    ).map { code ->
        TranslationLanguage(code = code, name = nativeTranslationLanguageName(code))
    }.distinctBy { it.code }
        .sortedBy { it.name.lowercase(Locale.ROOT) }

fun normalizeTranslationLanguageCode(code: String?): String? =
    code
        ?.lowercase()
        ?.substringBefore("-")
        ?.substringBefore("_")
        ?.takeIf { it.isNotBlank() && it != DEVICE_TRANSLATION_LANGUAGE }

fun currentDeviceTranslationLanguageCode(): String {
    // Locale.getDefault() follows the app-specific locale after AppCompat applies it. The automatic
    // translation target must instead follow the device language selected in Android settings.
    val systemLocale = Resources.getSystem().configuration.locales[0]
    return normalizeTranslationLanguageCode(systemLocale.toLanguageTag())
        ?: normalizeTranslationLanguageCode(systemLocale.language)
        ?: FALLBACK_TRANSLATION_TARGET_LANGUAGE
}

fun supportedDeviceTranslationLanguageCode(supportedLanguageCodes: Set<String>): String? =
    currentDeviceTranslationLanguageCode().takeIf { it in supportedLanguageCodes }

fun resolveTranslationLanguageCode(
    code: String?,
    supportedLanguageCodes: Set<String>? = null
): String? {
    if (code == DEVICE_TRANSLATION_LANGUAGE) {
        val deviceLanguageCode = currentDeviceTranslationLanguageCode()
        return if (supportedLanguageCodes == null || deviceLanguageCode in supportedLanguageCodes) {
            deviceLanguageCode
        } else {
            null
        }
    }
    return normalizeTranslationLanguageCode(code)
}

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
