/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.data.config

import com.byteflipper.everbook.BuildConfig
import com.byteflipper.everbook.domain.ads.AdSessionConfig
import com.byteflipper.everbook.domain.config.AdRemoteConfigKeys
import com.byteflipper.everbook.domain.config.AdRemoteConfigLimits
import com.byteflipper.everbook.domain.config.RemoteFeatureConfig
import com.byteflipper.everbook.domain.config.ReaderNativeAdConfig
import com.byteflipper.everbook.domain.config.provideAdRemoteConfigDefaults
import com.google.android.gms.tasks.Task
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class PlayStoreRemoteFeatureConfig @Inject constructor() : RemoteFeatureConfig {
    private val remoteConfig = FirebaseRemoteConfig.getInstance()
    private val _adsEnabled = MutableStateFlow(false)
    private val _readerEntryInterstitialAdsEnabled = MutableStateFlow(false)
    private val _readerEntryInterstitialShowInterval = MutableStateFlow(4)
    private val _readerNativeAdConfig = MutableStateFlow(ReaderNativeAdConfig())
    private val _adSessionConfig = MutableStateFlow(AdSessionConfig())

    override val adsEnabled: StateFlow<Boolean> = _adsEnabled
    override val readerEntryInterstitialAdsEnabled: StateFlow<Boolean> =
        _readerEntryInterstitialAdsEnabled
    override val readerEntryInterstitialShowInterval: StateFlow<Int> =
        _readerEntryInterstitialShowInterval
    override val readerNativeAdConfig: StateFlow<ReaderNativeAdConfig> = _readerNativeAdConfig
    override val adSessionConfig: StateFlow<AdSessionConfig> = _adSessionConfig

    init {
        remoteConfig.setConfigSettingsAsync(
            FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(if (BuildConfig.DEBUG) 60 else 3_600)
                .build()
        )
        remoteConfig.setDefaultsAsync(provideAdRemoteConfigDefaults())
    }

    override suspend fun refresh() {
        runCatching {
            remoteConfig.fetchAndActivate().await()
        }

        _adsEnabled.value = remoteConfig.getBoolean(AdRemoteConfigKeys.ADS_ENABLED)
        _readerEntryInterstitialAdsEnabled.value =
            remoteConfig.getBoolean(AdRemoteConfigKeys.READER_ENTRY_INTERSTITIAL_ADS_ENABLED)
        _readerEntryInterstitialShowInterval.value = remoteConfig
            .getLong(AdRemoteConfigKeys.READER_ENTRY_INTERSTITIAL_SHOW_INTERVAL)
            .toInt()
            .coerceIn(
                AdRemoteConfigLimits.READER_ENTRY_INTERSTITIAL_SHOW_INTERVAL_MIN,
                AdRemoteConfigLimits.READER_ENTRY_INTERSTITIAL_SHOW_INTERVAL_MAX
            )
        _adSessionConfig.value = AdSessionConfig(
            globalCooldownSeconds = remoteConfig
                .getLong(AdRemoteConfigKeys.AD_GLOBAL_COOLDOWN_SECONDS)
                .coerceIn(
                    AdRemoteConfigLimits.AD_GLOBAL_COOLDOWN_SECONDS_MIN,
                    AdRemoteConfigLimits.AD_GLOBAL_COOLDOWN_SECONDS_MAX
                )
        )
        _readerNativeAdConfig.value = ReaderNativeAdConfig(
            enabled = remoteConfig.getBoolean(AdRemoteConfigKeys.READER_NATIVE_ADS_ENABLED),
            textFirstMinUnits = remoteConfig
                .getLong(AdRemoteConfigKeys.READER_NATIVE_TEXT_FIRST_MIN_UNITS)
                .toInt()
                .coerceReaderNativeProgress(),
            textNextMinUnits = remoteConfig
                .getLong(AdRemoteConfigKeys.READER_NATIVE_TEXT_NEXT_MIN_UNITS)
                .toInt()
                .coerceReaderNativeProgress(),
            textMaxPerSession = remoteConfig
                .getLong(AdRemoteConfigKeys.READER_NATIVE_TEXT_MAX_PER_SESSION)
                .toInt()
                .coerceReaderNativeMaxPerSession(),
            textEndGuardUnits = remoteConfig
                .getLong(AdRemoteConfigKeys.READER_NATIVE_TEXT_END_GUARD_UNITS)
                .toInt()
                .coerceIn(
                    AdRemoteConfigLimits.READER_NATIVE_TEXT_END_GUARD_UNITS_MIN,
                    AdRemoteConfigLimits.READER_NATIVE_TEXT_END_GUARD_UNITS_MAX
                ),
            textLookaheadUnits = remoteConfig
                .getLong(AdRemoteConfigKeys.READER_NATIVE_TEXT_LOOKAHEAD_UNITS)
                .toInt()
                .coerceReaderNativeLookahead(),
            pdfFirstMinPages = remoteConfig
                .getLong(AdRemoteConfigKeys.READER_NATIVE_PDF_FIRST_MIN_PAGES)
                .toInt()
                .coerceReaderNativeProgress(),
            pdfNextMinPages = remoteConfig
                .getLong(AdRemoteConfigKeys.READER_NATIVE_PDF_NEXT_MIN_PAGES)
                .toInt()
                .coerceReaderNativeProgress(),
            pdfMaxPerSession = remoteConfig
                .getLong(AdRemoteConfigKeys.READER_NATIVE_PDF_MAX_PER_SESSION)
                .toInt()
                .coerceReaderNativeMaxPerSession(),
            pdfEndGuardPages = remoteConfig
                .getLong(AdRemoteConfigKeys.READER_NATIVE_PDF_END_GUARD_PAGES)
                .toInt()
                .coerceIn(
                    AdRemoteConfigLimits.READER_NATIVE_PDF_END_GUARD_PAGES_MIN,
                    AdRemoteConfigLimits.READER_NATIVE_PDF_END_GUARD_PAGES_MAX
                ),
            pdfLookaheadPages = remoteConfig
                .getLong(AdRemoteConfigKeys.READER_NATIVE_PDF_LOOKAHEAD_PAGES)
                .toInt()
                .coerceReaderNativeLookahead()
        )
    }

    private fun Int.coerceReaderNativeProgress(): Int {
        return coerceIn(
            AdRemoteConfigLimits.READER_NATIVE_PROGRESS_MIN,
            AdRemoteConfigLimits.READER_NATIVE_PROGRESS_MAX
        )
    }

    private fun Int.coerceReaderNativeMaxPerSession(): Int {
        return coerceIn(
            AdRemoteConfigLimits.READER_NATIVE_MAX_PER_SESSION_MIN,
            AdRemoteConfigLimits.READER_NATIVE_MAX_PER_SESSION_MAX
        )
    }

    private fun Int.coerceReaderNativeLookahead(): Int {
        return coerceIn(
            AdRemoteConfigLimits.READER_NATIVE_LOOKAHEAD_MIN,
            AdRemoteConfigLimits.READER_NATIVE_LOOKAHEAD_MAX
        )
    }

    private suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { continuation ->
        addOnCompleteListener { task ->
            if (task.isSuccessful) {
                continuation.resume(task.result)
            } else {
                continuation.resumeWithException(
                    task.exception ?: IllegalStateException("Firebase Remote Config task failed")
                )
            }
        }
    }
}
