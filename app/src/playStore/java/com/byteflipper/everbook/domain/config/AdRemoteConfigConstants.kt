/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.domain.config

object AdRemoteConfigKeys {
    const val ADS_ENABLED = "ads_enabled"
    const val READER_ENTRY_INTERSTITIAL_ADS_ENABLED =
        "reader_entry_interstitial_ads_enabled"
    const val READER_ENTRY_INTERSTITIAL_SHOW_INTERVAL =
        "reader_entry_interstitial_show_interval"
    const val AD_GLOBAL_COOLDOWN_SECONDS = "ad_global_cooldown_seconds"
    const val READER_NATIVE_ADS_ENABLED = "reader_native_ads_enabled"
    const val READER_NATIVE_TEXT_FIRST_MIN_UNITS =
        "reader_native_text_first_min_units"
    const val READER_NATIVE_TEXT_NEXT_MIN_UNITS =
        "reader_native_text_next_min_units"
    const val READER_NATIVE_TEXT_MAX_PER_SESSION =
        "reader_native_text_max_per_session"
    const val READER_NATIVE_TEXT_END_GUARD_UNITS =
        "reader_native_text_end_guard_units"
    const val READER_NATIVE_TEXT_LOOKAHEAD_UNITS =
        "reader_native_text_lookahead_units"
    const val READER_NATIVE_PDF_FIRST_MIN_PAGES =
        "reader_native_pdf_first_min_pages"
    const val READER_NATIVE_PDF_NEXT_MIN_PAGES =
        "reader_native_pdf_next_min_pages"
    const val READER_NATIVE_PDF_MAX_PER_SESSION =
        "reader_native_pdf_max_per_session"
    const val READER_NATIVE_PDF_END_GUARD_PAGES =
        "reader_native_pdf_end_guard_pages"
    const val READER_NATIVE_PDF_LOOKAHEAD_PAGES =
        "reader_native_pdf_lookahead_pages"
}

object AdRemoteConfigDefaults {
    const val ADS_ENABLED = true
    const val READER_ENTRY_INTERSTITIAL_ADS_ENABLED = true
    const val READER_ENTRY_INTERSTITIAL_SHOW_INTERVAL = 4
    const val AD_GLOBAL_COOLDOWN_SECONDS = 120L
    const val READER_NATIVE_ADS_ENABLED = true
    const val READER_NATIVE_TEXT_FIRST_MIN_UNITS = 6
    const val READER_NATIVE_TEXT_NEXT_MIN_UNITS = 12
    const val READER_NATIVE_TEXT_MAX_PER_SESSION = 8
    const val READER_NATIVE_TEXT_END_GUARD_UNITS = 8
    const val READER_NATIVE_TEXT_LOOKAHEAD_UNITS = 2
    const val READER_NATIVE_PDF_FIRST_MIN_PAGES = 6
    const val READER_NATIVE_PDF_NEXT_MIN_PAGES = 10
    const val READER_NATIVE_PDF_MAX_PER_SESSION = 8
    const val READER_NATIVE_PDF_END_GUARD_PAGES = 2
    const val READER_NATIVE_PDF_LOOKAHEAD_PAGES = 1
}

object AdRemoteConfigLimits {
    const val READER_ENTRY_INTERSTITIAL_SHOW_INTERVAL_MIN = 1
    const val READER_ENTRY_INTERSTITIAL_SHOW_INTERVAL_MAX = 20
    const val AD_GLOBAL_COOLDOWN_SECONDS_MIN = 0L
    const val AD_GLOBAL_COOLDOWN_SECONDS_MAX = 86_400L
    const val READER_NATIVE_PROGRESS_MIN = 1
    const val READER_NATIVE_PROGRESS_MAX = 1_000
    const val READER_NATIVE_MAX_PER_SESSION_MIN = 0
    const val READER_NATIVE_MAX_PER_SESSION_MAX = 20
    const val READER_NATIVE_TEXT_END_GUARD_UNITS_MIN = 0
    const val READER_NATIVE_TEXT_END_GUARD_UNITS_MAX = 1_000
    const val READER_NATIVE_PDF_END_GUARD_PAGES_MIN = 0
    const val READER_NATIVE_PDF_END_GUARD_PAGES_MAX = 100
    const val READER_NATIVE_LOOKAHEAD_MIN = 1
    const val READER_NATIVE_LOOKAHEAD_MAX = 100
}

fun provideAdRemoteConfigDefaults() = mapOf(
    AdRemoteConfigKeys.ADS_ENABLED to AdRemoteConfigDefaults.ADS_ENABLED,
    AdRemoteConfigKeys.READER_ENTRY_INTERSTITIAL_ADS_ENABLED to
            AdRemoteConfigDefaults.READER_ENTRY_INTERSTITIAL_ADS_ENABLED,
    AdRemoteConfigKeys.READER_ENTRY_INTERSTITIAL_SHOW_INTERVAL to
            AdRemoteConfigDefaults.READER_ENTRY_INTERSTITIAL_SHOW_INTERVAL.toLong(),
    AdRemoteConfigKeys.AD_GLOBAL_COOLDOWN_SECONDS to
            AdRemoteConfigDefaults.AD_GLOBAL_COOLDOWN_SECONDS,
    AdRemoteConfigKeys.READER_NATIVE_ADS_ENABLED to
            AdRemoteConfigDefaults.READER_NATIVE_ADS_ENABLED,
    AdRemoteConfigKeys.READER_NATIVE_TEXT_FIRST_MIN_UNITS to
            AdRemoteConfigDefaults.READER_NATIVE_TEXT_FIRST_MIN_UNITS.toLong(),
    AdRemoteConfigKeys.READER_NATIVE_TEXT_NEXT_MIN_UNITS to
            AdRemoteConfigDefaults.READER_NATIVE_TEXT_NEXT_MIN_UNITS.toLong(),
    AdRemoteConfigKeys.READER_NATIVE_TEXT_MAX_PER_SESSION to
            AdRemoteConfigDefaults.READER_NATIVE_TEXT_MAX_PER_SESSION.toLong(),
    AdRemoteConfigKeys.READER_NATIVE_TEXT_END_GUARD_UNITS to
            AdRemoteConfigDefaults.READER_NATIVE_TEXT_END_GUARD_UNITS.toLong(),
    AdRemoteConfigKeys.READER_NATIVE_TEXT_LOOKAHEAD_UNITS to
            AdRemoteConfigDefaults.READER_NATIVE_TEXT_LOOKAHEAD_UNITS.toLong(),
    AdRemoteConfigKeys.READER_NATIVE_PDF_FIRST_MIN_PAGES to
            AdRemoteConfigDefaults.READER_NATIVE_PDF_FIRST_MIN_PAGES.toLong(),
    AdRemoteConfigKeys.READER_NATIVE_PDF_NEXT_MIN_PAGES to
            AdRemoteConfigDefaults.READER_NATIVE_PDF_NEXT_MIN_PAGES.toLong(),
    AdRemoteConfigKeys.READER_NATIVE_PDF_MAX_PER_SESSION to
            AdRemoteConfigDefaults.READER_NATIVE_PDF_MAX_PER_SESSION.toLong(),
    AdRemoteConfigKeys.READER_NATIVE_PDF_END_GUARD_PAGES to
            AdRemoteConfigDefaults.READER_NATIVE_PDF_END_GUARD_PAGES.toLong(),
    AdRemoteConfigKeys.READER_NATIVE_PDF_LOOKAHEAD_PAGES to
            AdRemoteConfigDefaults.READER_NATIVE_PDF_LOOKAHEAD_PAGES.toLong()
)
