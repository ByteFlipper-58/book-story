/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.domain.ads

import com.byteflipper.everbook.domain.config.AdRemoteConfigDefaults

interface AdSessionController {
    fun configure(config: AdSessionConfig)

    fun canShow(format: AdFormat): Boolean

    fun recordShown(format: AdFormat)
}

data class AdSessionConfig(
    val globalCooldownSeconds: Long = AdRemoteConfigDefaults.AD_GLOBAL_COOLDOWN_SECONDS
)

enum class AdFormat {
    APP_OPEN,
    INTERSTITIAL,
    READER_NATIVE
}
