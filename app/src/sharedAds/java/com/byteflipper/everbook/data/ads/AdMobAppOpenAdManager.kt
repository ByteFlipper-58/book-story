/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.data.ads

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.byteflipper.everbook.R
import com.byteflipper.everbook.data.di.ApplicationScope
import com.byteflipper.everbook.data.local.data_store.DataStore
import com.byteflipper.everbook.domain.ads.AdFormat
import com.byteflipper.everbook.domain.ads.AdSessionController
import com.byteflipper.everbook.domain.config.RemoteFeatureConfig
import com.byteflipper.everbook.domain.privacy.PrivacyConsentManager
import com.byteflipper.everbook.presentation.core.constants.DataStoreConstants
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.appopen.AppOpenAd
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Loads an App Open ad while the app is running and shows it only after the
 * user explicitly enables this optional format in settings.
 */
@Singleton
class AdMobAppOpenAdManager @Inject constructor(
    private val application: Application,
    private val dataStore: DataStore,
    private val remoteFeatureConfig: RemoteFeatureConfig,
    private val adSessionController: AdSessionController,
    private val privacyConsentManager: PrivacyConsentManager,
    @ApplicationScope private val applicationScope: CoroutineScope
) : Application.ActivityLifecycleCallbacks, DefaultLifecycleObserver {

    private val _isEnabled = MutableStateFlow(false)
    val isEnabled: StateFlow<Boolean> = _isEnabled.asStateFlow()

    private var remoteAdsEnabled = false
    private var currentActivity: ComponentActivity? = null
    private var appOpenAd: AppOpenAd? = null
    private var adLoadTime = 0L
    private var isLoading = false
    private var isShowing = false
    private var isMobileAdsInitialized = false
    private var isStarted = false

    fun start() {
        if (isStarted) return
        isStarted = true

        application.registerActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        applicationScope.launch {
            val enabled = dataStore.getNullableData(DataStoreConstants.APP_OPEN_ADS_ENABLED) ?: false
            withContext(Dispatchers.Main.immediate) {
                _isEnabled.value = enabled
                loadAdIfNeeded()
            }
        }
        applicationScope.launch {
            remoteFeatureConfig.adsEnabled.collectLatest { enabled ->
                withContext(Dispatchers.Main.immediate) {
                    remoteAdsEnabled = enabled
                    if (canServeAds()) loadAdIfNeeded() else clearAd()
                }
            }
        }
        applicationScope.launch {
            remoteFeatureConfig.adSessionConfig.collectLatest { config ->
                withContext(Dispatchers.Main.immediate) {
                    adSessionController.configure(config)
                }
            }
        }
    }

    fun setEnabled(enabled: Boolean) {
        _isEnabled.value = enabled
        applicationScope.launch {
            dataStore.putData(DataStoreConstants.APP_OPEN_ADS_ENABLED, enabled)
        }

        if (enabled) {
            loadAdIfNeeded()
        } else {
            clearAd()
        }
    }

    override fun onStart(owner: LifecycleOwner) {
        val activity = currentActivity ?: return
        if (!canServeAds() || isShowing) return

        privacyConsentManager.requestConsentIfNeeded(activity) {
            if (!canServeAds() || !privacyConsentManager.canRequestAds(activity)) return@requestConsentIfNeeded

            initializeAndLoad(activity)
            showAdIfAvailable(activity)
        }
    }

    override fun onActivityStarted(activity: Activity) {
        if (!isShowing && activity is ComponentActivity) {
            currentActivity = activity
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
    override fun onActivityResumed(activity: Activity) = Unit
    override fun onActivityPaused(activity: Activity) = Unit
    override fun onActivityStopped(activity: Activity) = Unit
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
    override fun onActivityDestroyed(activity: Activity) {
        if (currentActivity === activity) currentActivity = null
    }

    private fun initializeAndLoad(activity: ComponentActivity) {
        if (isMobileAdsInitialized) {
            loadAdIfNeeded()
            return
        }

        isMobileAdsInitialized = true
        MobileAds.initialize(activity) { loadAdIfNeeded() }
    }

    private fun loadAdIfNeeded() {
        if (!isMobileAdsInitialized || !canServeAds() || isLoading || isAdAvailable()) return

        isLoading = true
        AppOpenAd.load(
            application,
            application.getString(R.string.admob_app_open_unit_id),
            AdRequest.Builder().build(),
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    isLoading = false
                    appOpenAd = ad
                    adLoadTime = Date().time
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    isLoading = false
                    appOpenAd = null
                    Log.w(TAG, "App Open ad failed to load: ${error.code} ${error.message}")
                }
            }
        )
    }

    private fun showAdIfAvailable(activity: ComponentActivity) {
        if (isShowing || !isAdAvailable() || !adSessionController.canShow(AdFormat.APP_OPEN)) {
            return
        }

        val ad = appOpenAd ?: return
        isShowing = true
        appOpenAd = null
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                adSessionController.recordShown(AdFormat.APP_OPEN)
            }

            override fun onAdDismissedFullScreenContent() {
                isShowing = false
                loadAdIfNeeded()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                isShowing = false
                Log.w(TAG, "App Open ad failed to show: ${adError.code} ${adError.message}")
                loadAdIfNeeded()
            }
        }
        ad.show(activity)
    }

    private fun canServeAds(): Boolean = _isEnabled.value && remoteAdsEnabled

    private fun isAdAvailable(): Boolean {
        return appOpenAd != null && Date().time - adLoadTime < MAX_AD_AGE_MILLIS
    }

    private fun clearAd() {
        appOpenAd = null
        adLoadTime = 0L
        isLoading = false
    }

    private companion object {
        const val TAG = "AppOpenAds"
        const val MAX_AD_AGE_MILLIS = 4 * 60 * 60 * 1_000L
    }
}
