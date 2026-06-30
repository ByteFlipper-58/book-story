/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.data.config

import android.util.Log
import com.byteflipper.everbook.BuildConfig
import com.byteflipper.everbook.domain.ads.AdSessionConfig
import com.byteflipper.everbook.domain.config.AdRemoteConfigDefaults
import com.byteflipper.everbook.domain.config.AdRemoteConfigKeys
import com.byteflipper.everbook.domain.config.AdRemoteConfigLimits
import com.byteflipper.everbook.domain.config.ReaderNativeAdConfig
import com.byteflipper.everbook.domain.config.RemoteFeatureConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import ru.rustore.sdk.remoteconfig.RemoteConfig
import ru.rustore.sdk.remoteconfig.RemoteConfigClient
import javax.inject.Inject

private const val TAG = "RemoteFeatureConfig"

/**
 * RuStore Remote Config provider. Initialised by [RemoteConfigClient] (built in
 * [com.byteflipper.everbook.data.distribution.RuStoreDistributionStartup.onAppCreate]).
 *
 * Falls back to [AdRemoteConfigDefaults] when the client is unavailable or a key is missing,
 * matching the Play Store flavour's behaviour on Firebase fetch failure.
 */
class RuStoreRemoteFeatureConfig @Inject constructor() : RemoteFeatureConfig {
    // RuStore has no consent gating (unlike Play Store's UMP), so start from the shared defaults.
    // If RuStore Remote Config is unreachable (404 / unconfigured), these stay in effect.
    private val _adsEnabled = MutableStateFlow(AdRemoteConfigDefaults.ADS_ENABLED)
    private val _readerEntryInterstitialAdsEnabled = MutableStateFlow(
        AdRemoteConfigDefaults.READER_ENTRY_INTERSTITIAL_ADS_ENABLED
    )
    private val _readerEntryInterstitialShowInterval = MutableStateFlow(
        AdRemoteConfigDefaults.READER_ENTRY_INTERSTITIAL_SHOW_INTERVAL
    )
    private val _readerNativeAdConfig = MutableStateFlow(ReaderNativeAdConfig())
    private val _adSessionConfig = MutableStateFlow(AdSessionConfig())

    override val adsEnabled: StateFlow<Boolean> = _adsEnabled
    override val readerEntryInterstitialAdsEnabled: StateFlow<Boolean> =
        _readerEntryInterstitialAdsEnabled
    override val readerEntryInterstitialShowInterval: StateFlow<Int> =
        _readerEntryInterstitialShowInterval
    override val readerNativeAdConfig: StateFlow<ReaderNativeAdConfig> = _readerNativeAdConfig
    override val adSessionConfig: StateFlow<AdSessionConfig> = _adSessionConfig

    override suspend fun refresh() {
        val config = withContext(Dispatchers.IO) {
            runCatching {
                RemoteConfigClient.instance.getRemoteConfig().await()
            }.getOrNull()
        } ?: run {
            Log.w(TAG, "RemoteConfig unavailable, keeping defaults")
            return
        }

        _adsEnabled.value = config.safeGetBoolean(
            AdRemoteConfigKeys.ADS_ENABLED,
            AdRemoteConfigDefaults.ADS_ENABLED
        )
        _readerEntryInterstitialAdsEnabled.value = config.safeGetBoolean(
            AdRemoteConfigKeys.READER_ENTRY_INTERSTITIAL_ADS_ENABLED,
            AdRemoteConfigDefaults.READER_ENTRY_INTERSTITIAL_ADS_ENABLED
        )
        _readerEntryInterstitialShowInterval.value = config.safeGetLong(
            AdRemoteConfigKeys.READER_ENTRY_INTERSTITIAL_SHOW_INTERVAL,
            AdRemoteConfigDefaults.READER_ENTRY_INTERSTITIAL_SHOW_INTERVAL.toLong()
        ).toInt().coerceIn(
            AdRemoteConfigLimits.READER_ENTRY_INTERSTITIAL_SHOW_INTERVAL_MIN,
            AdRemoteConfigLimits.READER_ENTRY_INTERSTITIAL_SHOW_INTERVAL_MAX
        )
        _adSessionConfig.value = AdSessionConfig(
            globalCooldownSeconds = config.safeGetLong(
                AdRemoteConfigKeys.AD_GLOBAL_COOLDOWN_SECONDS,
                AdRemoteConfigDefaults.AD_GLOBAL_COOLDOWN_SECONDS
            ).coerceIn(
                AdRemoteConfigLimits.AD_GLOBAL_COOLDOWN_SECONDS_MIN,
                AdRemoteConfigLimits.AD_GLOBAL_COOLDOWN_SECONDS_MAX
            )
        )
        _readerNativeAdConfig.value = buildReaderNativeAdConfig(config)
    }

    private fun buildReaderNativeAdConfig(config: RemoteConfig): ReaderNativeAdConfig {
        fun safeGetInt(key: String, default: Int): Int {
            return config.safeGetLong(key, default.toLong()).toInt()
        }

        return ReaderNativeAdConfig(
            enabled = config.safeGetBoolean(
                AdRemoteConfigKeys.READER_NATIVE_ADS_ENABLED,
                AdRemoteConfigDefaults.READER_NATIVE_ADS_ENABLED
            ),
            textFirstMinUnits = safeGetInt(
                AdRemoteConfigKeys.READER_NATIVE_TEXT_FIRST_MIN_UNITS,
                AdRemoteConfigDefaults.READER_NATIVE_TEXT_FIRST_MIN_UNITS
            ).coerceIn(
                AdRemoteConfigLimits.READER_NATIVE_PROGRESS_MIN,
                AdRemoteConfigLimits.READER_NATIVE_PROGRESS_MAX
            ),
            textNextMinUnits = safeGetInt(
                AdRemoteConfigKeys.READER_NATIVE_TEXT_NEXT_MIN_UNITS,
                AdRemoteConfigDefaults.READER_NATIVE_TEXT_NEXT_MIN_UNITS
            ).coerceIn(
                AdRemoteConfigLimits.READER_NATIVE_PROGRESS_MIN,
                AdRemoteConfigLimits.READER_NATIVE_PROGRESS_MAX
            ),
            textMaxPerSession = safeGetInt(
                AdRemoteConfigKeys.READER_NATIVE_TEXT_MAX_PER_SESSION,
                AdRemoteConfigDefaults.READER_NATIVE_TEXT_MAX_PER_SESSION
            ).coerceIn(
                AdRemoteConfigLimits.READER_NATIVE_MAX_PER_SESSION_MIN,
                AdRemoteConfigLimits.READER_NATIVE_MAX_PER_SESSION_MAX
            ),
            textEndGuardUnits = safeGetInt(
                AdRemoteConfigKeys.READER_NATIVE_TEXT_END_GUARD_UNITS,
                AdRemoteConfigDefaults.READER_NATIVE_TEXT_END_GUARD_UNITS
            ).coerceIn(
                AdRemoteConfigLimits.READER_NATIVE_TEXT_END_GUARD_UNITS_MIN,
                AdRemoteConfigLimits.READER_NATIVE_TEXT_END_GUARD_UNITS_MAX
            ),
            textLookaheadUnits = safeGetInt(
                AdRemoteConfigKeys.READER_NATIVE_TEXT_LOOKAHEAD_UNITS,
                AdRemoteConfigDefaults.READER_NATIVE_TEXT_LOOKAHEAD_UNITS
            ).coerceIn(
                AdRemoteConfigLimits.READER_NATIVE_LOOKAHEAD_MIN,
                AdRemoteConfigLimits.READER_NATIVE_LOOKAHEAD_MAX
            ),
            pdfFirstMinPages = safeGetInt(
                AdRemoteConfigKeys.READER_NATIVE_PDF_FIRST_MIN_PAGES,
                AdRemoteConfigDefaults.READER_NATIVE_PDF_FIRST_MIN_PAGES
            ).coerceIn(
                AdRemoteConfigLimits.READER_NATIVE_PROGRESS_MIN,
                AdRemoteConfigLimits.READER_NATIVE_PROGRESS_MAX
            ),
            pdfNextMinPages = safeGetInt(
                AdRemoteConfigKeys.READER_NATIVE_PDF_NEXT_MIN_PAGES,
                AdRemoteConfigDefaults.READER_NATIVE_PDF_NEXT_MIN_PAGES
            ).coerceIn(
                AdRemoteConfigLimits.READER_NATIVE_PROGRESS_MIN,
                AdRemoteConfigLimits.READER_NATIVE_PROGRESS_MAX
            ),
            pdfMaxPerSession = safeGetInt(
                AdRemoteConfigKeys.READER_NATIVE_PDF_MAX_PER_SESSION,
                AdRemoteConfigDefaults.READER_NATIVE_PDF_MAX_PER_SESSION
            ).coerceIn(
                AdRemoteConfigLimits.READER_NATIVE_MAX_PER_SESSION_MIN,
                AdRemoteConfigLimits.READER_NATIVE_MAX_PER_SESSION_MAX
            ),
            pdfEndGuardPages = safeGetInt(
                AdRemoteConfigKeys.READER_NATIVE_PDF_END_GUARD_PAGES,
                AdRemoteConfigDefaults.READER_NATIVE_PDF_END_GUARD_PAGES
            ).coerceIn(
                AdRemoteConfigLimits.READER_NATIVE_PDF_END_GUARD_PAGES_MIN,
                AdRemoteConfigLimits.READER_NATIVE_PDF_END_GUARD_PAGES_MAX
            ),
            pdfLookaheadPages = safeGetInt(
                AdRemoteConfigKeys.READER_NATIVE_PDF_LOOKAHEAD_PAGES,
                AdRemoteConfigDefaults.READER_NATIVE_PDF_LOOKAHEAD_PAGES
            ).coerceIn(
                AdRemoteConfigLimits.READER_NATIVE_LOOKAHEAD_MIN,
                AdRemoteConfigLimits.READER_NATIVE_LOOKAHEAD_MAX
            )
        )
    }

    private fun RemoteConfig.safeGetBoolean(key: String, default: Boolean): Boolean {
        return if (containsKey(key)) {
            runCatching { getBoolean(key) }.getOrDefault(default)
        } else default
    }

    private fun RemoteConfig.safeGetLong(key: String, default: Long): Long {
        return if (containsKey(key)) {
            runCatching { getLong(key) }.getOrDefault(default)
        } else default
    }
}
