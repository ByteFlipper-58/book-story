/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.data.privacy

import androidx.activity.ComponentActivity
import androidx.datastore.preferences.core.booleanPreferencesKey
import com.byteflipper.everbook.data.di.ApplicationScope
import com.byteflipper.everbook.domain.privacy.PrivacyConsentManager
import com.byteflipper.everbook.domain.repository.DataStoreRepository
import com.yandex.mobile.ads.common.YandexAds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Yandex Mobile Ads does not ship a consent form like Google's UMP; the publisher manages consent.
 * This keeps a persisted "personalized ads" preference and forwards it to the SDK. Ads can always
 * be requested (Yandex serves contextual ads without consent); the toggle only affects
 * personalization, surfaced in the RuStore privacy settings screen.
 */
@Singleton
class RuStorePrivacyConsentManager @Inject constructor(
    private val dataStoreRepository: DataStoreRepository,
    @ApplicationScope private val applicationScope: CoroutineScope
) : PrivacyConsentManager {

    private val _personalizedAdsEnabled = MutableStateFlow(true)
    val personalizedAdsEnabled: StateFlow<Boolean> = _personalizedAdsEnabled

    // A privacy options entry point is always available so the user can change ad personalization.
    private val _privacyOptionsRequired = MutableStateFlow(true)
    override val privacyOptionsRequired: StateFlow<Boolean> = _privacyOptionsRequired

    init {
        applicationScope.launch {
            val stored = dataStoreRepository.getNullableDataFromDataStore(CONSENT_KEY) ?: true
            _personalizedAdsEnabled.value = stored
            YandexAds.setUserConsent(stored)
        }
    }

    override fun requestConsentIfNeeded(
        activity: ComponentActivity,
        onConsentUpdated: () -> Unit
    ) {
        YandexAds.setUserConsent(_personalizedAdsEnabled.value)
        onConsentUpdated()
    }

    override fun refreshPrivacyOptionsRequirement(
        activity: ComponentActivity,
        onComplete: () -> Unit
    ) {
        onComplete()
    }

    override fun showPrivacyOptions(
        activity: ComponentActivity,
        onComplete: (errorMessage: String?) -> Unit
    ) {
        // The toggle is handled directly by the RuStore privacy screen; nothing to present here.
        onComplete(null)
    }

    override fun canRequestAds(activity: ComponentActivity): Boolean = true

    fun setPersonalizedAds(enabled: Boolean) {
        _personalizedAdsEnabled.value = enabled
        YandexAds.setUserConsent(enabled)
        applicationScope.launch {
            dataStoreRepository.putDataToDataStore(CONSENT_KEY, enabled)
        }
    }

    private companion object {
        val CONSENT_KEY = booleanPreferencesKey("ru_store_personalized_ads_consent")
    }
}
