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
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

private const val TAG = "RemoteFeatureConfig"

class PlayStoreRemoteFeatureConfig @Inject constructor() : RemoteFeatureConfig {
    private val remoteConfig: FirebaseRemoteConfig? = try {
        FirebaseApp.getInstance()
        FirebaseRemoteConfig.getInstance()
    } catch (e: IllegalStateException) {
        Log.w(TAG, "Firebase not initialized, remote config disabled", e)
        null
    }

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
        remoteConfig?.let { config ->
            config.setConfigSettingsAsync(
                FirebaseRemoteConfigSettings.Builder()
                    .setMinimumFetchIntervalInSeconds(if (BuildConfig.DEBUG) 60 else 3_600)
                    .build()
            )
            config.setDefaultsAsync(provideAdRemoteConfigDefaults())
        }
    }

    override suspend fun refresh() {
        val config = remoteConfig ?: return

        runCatching {
            config.fetchAndActivate().await()
        }

        _adsEnabled.value = config.getBoolean(AdRemoteConfigKeys.ADS_ENABLED)
        _readerEntryInterstitialAdsEnabled.value =
            config.getBoolean(AdRemoteConfigKeys.READER_ENTRY_INTERSTITIAL_ADS_ENABLED)
        _readerEntryInterstitialShowInterval.value = config
            .getLong(AdRemoteConfigKeys.READER_ENTRY_INTERSTITIAL_SHOW_INTERVAL)
            .toInt()
            .coerceIn(
                AdRemoteConfigLimits.READER_ENTRY_INTERSTITIAL_SHOW_INTERVAL_MIN,
                AdRemoteConfigLimits.READER_ENTRY_INTERSTITIAL_SHOW_INTERVAL_MAX
            )
        _adSessionConfig.value = AdSessionConfig(
            globalCooldownSeconds = config
                .getLong(AdRemoteConfigKeys.AD_GLOBAL_COOLDOWN_SECONDS)
                .coerceIn(
                    AdRemoteConfigLimits.AD_GLOBAL_COOLDOWN_SECONDS_MIN,
                    AdRemoteConfigLimits.AD_GLOBAL_COOLDOWN_SECONDS_MAX
                )
        )
        _readerNativeAdConfig.value = ReaderNativeAdConfig(
            enabled = config.getBoolean(AdRemoteConfigKeys.READER_NATIVE_ADS_ENABLED),
            textFirstMinUnits = config
                .getLong(AdRemoteConfigKeys.READER_NATIVE_TEXT_FIRST_MIN_UNITS)
                .toInt()
                .coerceReaderNativeProgress(),
            textNextMinUnits = config
                .getLong(AdRemoteConfigKeys.READER_NATIVE_TEXT_NEXT_MIN_UNITS)
                .toInt()
                .coerceReaderNativeProgress(),
            textMaxPerSession = config
                .getLong(AdRemoteConfigKeys.READER_NATIVE_TEXT_MAX_PER_SESSION)
                .toInt()
                .coerceReaderNativeMaxPerSession(),
            textEndGuardUnits = config
                .getLong(AdRemoteConfigKeys.READER_NATIVE_TEXT_END_GUARD_UNITS)
                .toInt()
                .coerceIn(
                    AdRemoteConfigLimits.READER_NATIVE_TEXT_END_GUARD_UNITS_MIN,
                    AdRemoteConfigLimits.READER_NATIVE_TEXT_END_GUARD_UNITS_MAX
                ),
            textLookaheadUnits = config
                .getLong(AdRemoteConfigKeys.READER_NATIVE_TEXT_LOOKAHEAD_UNITS)
                .toInt()
                .coerceReaderNativeLookahead(),
            pdfFirstMinPages = config
                .getLong(AdRemoteConfigKeys.READER_NATIVE_PDF_FIRST_MIN_PAGES)
                .toInt()
                .coerceReaderNativeProgress(),
            pdfNextMinPages = config
                .getLong(AdRemoteConfigKeys.READER_NATIVE_PDF_NEXT_MIN_PAGES)
                .toInt()
                .coerceReaderNativeProgress(),
            pdfMaxPerSession = config
                .getLong(AdRemoteConfigKeys.READER_NATIVE_PDF_MAX_PER_SESSION)
                .toInt()
                .coerceReaderNativeMaxPerSession(),
            pdfEndGuardPages = config
                .getLong(AdRemoteConfigKeys.READER_NATIVE_PDF_END_GUARD_PAGES)
                .toInt()
                .coerceIn(
                    AdRemoteConfigLimits.READER_NATIVE_PDF_END_GUARD_PAGES_MIN,
                    AdRemoteConfigLimits.READER_NATIVE_PDF_END_GUARD_PAGES_MAX
                ),
            pdfLookaheadPages = config
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
