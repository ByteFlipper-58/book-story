/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.data.ads

import android.util.Log
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.byteflipper.everbook.R
import com.byteflipper.everbook.domain.ads.AdFormat
import com.byteflipper.everbook.domain.ads.AdSessionController
import com.byteflipper.everbook.domain.config.RemoteFeatureConfig
import com.byteflipper.everbook.domain.distribution.ReaderEntryActionController
import com.byteflipper.everbook.domain.privacy.PrivacyConsentManager
import com.yandex.mobile.ads.common.AdError
import com.yandex.mobile.ads.common.AdRequest
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.ImpressionData
import com.yandex.mobile.ads.interstitial.InterstitialAd
import com.yandex.mobile.ads.interstitial.InterstitialAdEventListener
import com.yandex.mobile.ads.interstitial.InterstitialAdLoadListener
import com.yandex.mobile.ads.interstitial.InterstitialAdLoader
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Reader-entry interstitial backed by the Yandex Mobile Ads SDK. Mirrors the Play Store flavor's
 * interval / cooldown gating, swapping AdMob for Yandex's [InterstitialAdLoader].
 */
class RuStoreInterstitialAdManager @Inject constructor(
    private val adSessionController: AdSessionController,
    private val remoteFeatureConfig: RemoteFeatureConfig,
    private val privacyConsentManager: PrivacyConsentManager
) : ReaderEntryActionController {
    private var adsEnabled = false
    private var showInterval = 4
    private var readerEntriesSinceLastShown = 0
    private var isLoading = false
    private var nextRetryAtMillis = 0L
    private var consecutiveFailures = 0
    private var interstitialAd: InterstitialAd? = null
    private var interstitialAdLoader: InterstitialAdLoader? = null

    override fun configure(activity: ComponentActivity) {
        activity.lifecycleScope.launch {
            combine(
                remoteFeatureConfig.adsEnabled,
                remoteFeatureConfig.readerEntryInterstitialAdsEnabled,
                remoteFeatureConfig.readerEntryInterstitialShowInterval,
                remoteFeatureConfig.adSessionConfig
            ) { adsEnabled, readerEntryInterstitialAdsEnabled, showInterval, adSessionConfig ->
                adSessionController.configure(adSessionConfig)
                Pair(adsEnabled && readerEntryInterstitialAdsEnabled, showInterval)
            }
                .collectLatest { (enabled, showInterval) ->
                    configureAds(
                        activity = activity,
                        enabled = enabled,
                        showInterval = showInterval
                    )
                }
        }
    }

    private fun configureAds(
        activity: ComponentActivity,
        enabled: Boolean,
        showInterval: Int
    ) {
        adsEnabled = enabled
        this.showInterval = showInterval.coerceAtLeast(1)
        readerEntriesSinceLastShown = readerEntriesSinceLastShown.coerceAtMost(this.showInterval)

        if (!enabled) {
            isLoading = false
            interstitialAd = null
            readerEntriesSinceLastShown = 0
            return
        }

        privacyConsentManager.requestConsentIfNeeded(activity) {
            if (privacyConsentManager.canRequestAds(activity)) {
                load(activity)
            } else {
                interstitialAd = null
            }
        }
    }

    override fun onReaderEntered(activity: ComponentActivity) {
        if (!adsEnabled || !privacyConsentManager.canRequestAds(activity)) {
            Log.d(TAG, "Reader entered, skip: adsEnabled=$adsEnabled, consent=${privacyConsentManager.canRequestAds(activity)}")
            return
        }

        readerEntriesSinceLastShown += 1
        Log.d(TAG, "Reader entered ($readerEntriesSinceLastShown/$showInterval)")
        if (readerEntriesSinceLastShown < showInterval) {
            load(activity)
            return
        }

        val ad = interstitialAd
        if (ad == null) {
            Log.d(TAG, "Interval reached but no ad ready; loading")
            load(activity)
            return
        }

        if (!adSessionController.canShow(AdFormat.INTERSTITIAL)) {
            Log.d(TAG, "Interval reached but session cooldown active; loading")
            load(activity)
            return
        }

        readerEntriesSinceLastShown = 0
        interstitialAd = null
        Log.i(TAG, "Interstitial showing")
        ad.setAdEventListener(object : InterstitialAdEventListener {
            override fun onAdShown() {
                Log.d(TAG, "Interstitial onAdShown")
            }

            override fun onAdFailedToShow(adError: AdError) {
                Log.w(TAG, "Interstitial failed to show: ${adError.description}")
                load(activity)
            }

            override fun onAdDismissed() {
                Log.d(TAG, "Interstitial dismissed")
                ad.setAdEventListener(null)
                load(activity)
            }

            override fun onAdClicked() {
                Log.d(TAG, "Interstitial clicked")
            }

            override fun onAdImpression(impressionData: ImpressionData?) {
                Log.d(TAG, "Interstitial impression")
            }
        })
        adSessionController.recordShown(AdFormat.INTERSTITIAL)
        ad.show(activity)
    }

    private fun load(activity: ComponentActivity) {
        if (!adsEnabled || isLoading || interstitialAd != null) return
        if (android.os.SystemClock.elapsedRealtime() < nextRetryAtMillis) return

        val adUnitId = activity.getString(R.string.yandex_interstitial_unit_id)
        if (adUnitId.isBlank()) return

        val loader = interstitialAdLoader ?: InterstitialAdLoader(activity).also {
            interstitialAdLoader = it
        }

        isLoading = true
        Log.d(TAG, "Interstitial requesting (unit=$adUnitId)")
        loader.loadAd(
            AdRequest.Builder(adUnitId).build(),
            object : InterstitialAdLoadListener {
                override fun onAdLoaded(ad: InterstitialAd) {
                    isLoading = false
                    interstitialAd = ad
                    consecutiveFailures = 0
                    nextRetryAtMillis = 0L
                    Log.i(TAG, "Interstitial loaded")
                }

                override fun onAdFailedToLoad(error: AdRequestError) {
                    isLoading = false
                    interstitialAd = null
                    consecutiveFailures += 1
                    val backoff = (BASE_BACKOFF_MS shl (consecutiveFailures - 1))
                        .coerceAtMost(MAX_BACKOFF_MS)
                    nextRetryAtMillis = android.os.SystemClock.elapsedRealtime() + backoff
                    Log.w(
                        TAG, "Interstitial failed to load: ${error.code} ${error.description}; " +
                            "retry in ${backoff}ms"
                    )
                }
            }
        )
    }

    companion object {
        private const val TAG = "InterstitialAds"
        private const val BASE_BACKOFF_MS = 30_000L
        private const val MAX_BACKOFF_MS = 600_000L
    }
}
