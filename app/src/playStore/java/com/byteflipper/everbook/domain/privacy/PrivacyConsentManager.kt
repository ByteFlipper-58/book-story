/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.domain.privacy

import androidx.activity.ComponentActivity
import kotlinx.coroutines.flow.StateFlow

interface PrivacyConsentManager {
    val privacyOptionsRequired: StateFlow<Boolean>

    fun requestConsentIfNeeded(
        activity: ComponentActivity,
        onConsentUpdated: () -> Unit
    )

    fun refreshPrivacyOptionsRequirement(
        activity: ComponentActivity,
        onComplete: () -> Unit
    )

    fun showPrivacyOptions(
        activity: ComponentActivity,
        onComplete: (errorMessage: String?) -> Unit
    )

    fun canRequestAds(activity: ComponentActivity): Boolean
}
