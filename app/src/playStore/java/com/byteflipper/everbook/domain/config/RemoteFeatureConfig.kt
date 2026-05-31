/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.domain.config

import kotlinx.coroutines.flow.StateFlow
import com.byteflipper.everbook.domain.ads.AdSessionConfig

interface RemoteFeatureConfig {
    val adsEnabled: StateFlow<Boolean>
    val readerEntryInterstitialAdsEnabled: StateFlow<Boolean>
    val readerEntryInterstitialShowInterval: StateFlow<Int>
    val readerNativeAdConfig: StateFlow<ReaderNativeAdConfig>
    val adSessionConfig: StateFlow<AdSessionConfig>

    suspend fun refresh()
}

data class ReaderNativeAdConfig(
    val enabled: Boolean = AdRemoteConfigDefaults.READER_NATIVE_ADS_ENABLED,
    val textFirstMinUnits: Int = AdRemoteConfigDefaults.READER_NATIVE_TEXT_FIRST_MIN_UNITS,
    val textNextMinUnits: Int = AdRemoteConfigDefaults.READER_NATIVE_TEXT_NEXT_MIN_UNITS,
    val textMaxPerSession: Int = AdRemoteConfigDefaults.READER_NATIVE_TEXT_MAX_PER_SESSION,
    val textEndGuardUnits: Int = AdRemoteConfigDefaults.READER_NATIVE_TEXT_END_GUARD_UNITS,
    val textLookaheadUnits: Int = AdRemoteConfigDefaults.READER_NATIVE_TEXT_LOOKAHEAD_UNITS,
    val pdfFirstMinPages: Int = AdRemoteConfigDefaults.READER_NATIVE_PDF_FIRST_MIN_PAGES,
    val pdfNextMinPages: Int = AdRemoteConfigDefaults.READER_NATIVE_PDF_NEXT_MIN_PAGES,
    val pdfMaxPerSession: Int = AdRemoteConfigDefaults.READER_NATIVE_PDF_MAX_PER_SESSION,
    val pdfEndGuardPages: Int = AdRemoteConfigDefaults.READER_NATIVE_PDF_END_GUARD_PAGES,
    val pdfLookaheadPages: Int = AdRemoteConfigDefaults.READER_NATIVE_PDF_LOOKAHEAD_PAGES
)
