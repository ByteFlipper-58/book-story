/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.data.ads

import android.os.SystemClock
import com.byteflipper.everbook.domain.ads.AdFormat
import com.byteflipper.everbook.domain.ads.AdSessionConfig
import com.byteflipper.everbook.domain.ads.AdSessionController
import javax.inject.Inject

class PlayStoreAdSessionController @Inject constructor() : AdSessionController {
    private var config = AdSessionConfig()
    private var lastAdShownElapsed: Long? = null

    override fun configure(config: AdSessionConfig) {
        this.config = config
    }

    override fun canShow(format: AdFormat): Boolean {
        val now = SystemClock.elapsedRealtime()

        return isCooldownElapsed(now, lastAdShownElapsed, config.globalCooldownSeconds)
    }

    override fun recordShown(format: AdFormat) {
        lastAdShownElapsed = SystemClock.elapsedRealtime()
    }

    private fun isCooldownElapsed(
        now: Long,
        lastShownElapsed: Long?,
        cooldownSeconds: Long
    ): Boolean {
        if (lastShownElapsed == null) return true

        return now - lastShownElapsed >= cooldownSeconds.coerceAtLeast(0L) * 1_000L
    }
}
