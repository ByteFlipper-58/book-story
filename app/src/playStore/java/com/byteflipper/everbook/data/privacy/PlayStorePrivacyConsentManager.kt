/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.data.privacy

import android.util.Log
import androidx.activity.ComponentActivity
import com.byteflipper.everbook.domain.privacy.PrivacyConsentManager
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class PlayStorePrivacyConsentManager @Inject constructor() : PrivacyConsentManager {
    private val _privacyOptionsRequired = MutableStateFlow(false)

    override val privacyOptionsRequired: StateFlow<Boolean> = _privacyOptionsRequired

    override fun requestConsentIfNeeded(
        activity: ComponentActivity,
        onConsentUpdated: () -> Unit
    ) {
        val consentInformation = UserMessagingPlatform.getConsentInformation(activity)
        val params = ConsentRequestParameters.Builder()
            .setTagForUnderAgeOfConsent(false)
            .build()

        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                updatePrivacyOptionsRequirement(activity)
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
                    formError?.let {
                        Log.w(TAG, "Consent form failed: ${it.errorCode} ${it.message}")
                    }
                    updatePrivacyOptionsRequirement(activity)
                    onConsentUpdated()
                }
            },
            { requestError ->
                Log.w(TAG, "Consent info update failed: ${requestError.errorCode} ${requestError.message}")
                updatePrivacyOptionsRequirement(activity)
                onConsentUpdated()
            }
        )
    }

    override fun refreshPrivacyOptionsRequirement(
        activity: ComponentActivity,
        onComplete: () -> Unit
    ) {
        val consentInformation = UserMessagingPlatform.getConsentInformation(activity)

        consentInformation.requestConsentInfoUpdate(
            activity,
            consentRequestParameters(),
            {
                updatePrivacyOptionsRequirement(activity)
                onComplete()
            },
            { requestError ->
                Log.w(TAG, "Consent info update failed: ${requestError.errorCode} ${requestError.message}")
                updatePrivacyOptionsRequirement(activity)
                onComplete()
            }
        )
    }

    override fun showPrivacyOptions(
        activity: ComponentActivity,
        onComplete: (errorMessage: String?) -> Unit
    ) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity) { formError ->
            updatePrivacyOptionsRequirement(activity)
            onComplete(formError?.message)
        }
    }

    override fun canRequestAds(activity: ComponentActivity): Boolean {
        return UserMessagingPlatform.getConsentInformation(activity).canRequestAds()
    }

    private fun consentRequestParameters(): ConsentRequestParameters {
        return ConsentRequestParameters.Builder()
            .setTagForUnderAgeOfConsent(false)
            .build()
    }

    private fun updatePrivacyOptionsRequirement(activity: ComponentActivity) {
        _privacyOptionsRequired.value = UserMessagingPlatform
            .getConsentInformation(activity)
            .privacyOptionsRequirementStatus ==
                ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED
    }

    @Suppress("unused")
    private fun debugParams(activity: ComponentActivity): ConsentRequestParameters {
        val debugSettings = ConsentDebugSettings.Builder(activity)
            .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
            .build()

        return ConsentRequestParameters.Builder()
            .setConsentDebugSettings(debugSettings)
            .setTagForUnderAgeOfConsent(false)
            .build()
    }

    companion object {
        private const val TAG = "PrivacyConsent"
    }
}
