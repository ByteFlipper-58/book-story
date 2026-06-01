/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook

import android.app.Application
import com.byteflipper.everbook.domain.distribution.DistributionStartup
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class Application : Application() {
    @Inject
    lateinit var distributionStartup: DistributionStartup

    override fun onCreate() {
        super.onCreate()

        distributionStartup.onAppCreate()
    }
}






