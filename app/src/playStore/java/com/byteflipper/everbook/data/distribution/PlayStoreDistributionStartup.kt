/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.data.distribution

import com.byteflipper.everbook.data.di.ApplicationScope
import com.byteflipper.everbook.domain.config.RemoteFeatureConfig
import com.byteflipper.everbook.domain.distribution.DistributionStartup
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class PlayStoreDistributionStartup @Inject constructor(
    private val remoteFeatureConfig: RemoteFeatureConfig,
    @ApplicationScope private val applicationScope: CoroutineScope
) : DistributionStartup {
    override fun onAppCreate() {
        applicationScope.launch {
            remoteFeatureConfig.refresh()
        }
    }
}
