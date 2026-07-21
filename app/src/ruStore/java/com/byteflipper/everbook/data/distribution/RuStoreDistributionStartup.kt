/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.data.distribution

import android.content.Context
import android.util.Log
import com.byteflipper.everbook.data.ads.AdMobAppOpenAdManager
import com.byteflipper.everbook.BuildConfig
import com.byteflipper.everbook.data.di.ApplicationScope
import com.byteflipper.everbook.domain.config.RemoteFeatureConfig
import com.byteflipper.everbook.domain.config.provideAdRemoteConfigDefaults
import com.byteflipper.everbook.domain.distribution.DistributionStartup
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import ru.rustore.sdk.remoteconfig.RemoteConfigClient
import ru.rustore.sdk.remoteconfig.RemoteConfigClientBuilder
import ru.rustore.sdk.remoteconfig.AppId
import ru.rustore.sdk.remoteconfig.UpdateBehaviour
import kotlin.time.Duration.Companion.minutes
import javax.inject.Inject

private const val TAG = "RuStoreDistStartup"

class RuStoreDistributionStartup @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appOpenAdManager: AdMobAppOpenAdManager,
    private val remoteFeatureConfig: RemoteFeatureConfig,
    @ApplicationScope private val applicationScope: CoroutineScope
) : DistributionStartup {
    override fun onAppCreate() {
        appOpenAdManager.start()
        // RuStore Remote Config
        initRemoteConfig()

        applicationScope.launch {
            remoteFeatureConfig.refresh()
        }
    }

    private fun initRemoteConfig() {
        runCatching {
            RemoteConfigClientBuilder(
                appId = AppId(BuildConfig.RU_STORE_APP_ID),
                context = context
            )
                .setInternalConfig(provideAdRemoteConfigDefaults())
                .setUpdateBehaviour(UpdateBehaviour.Default(15.minutes))
                .build()
                .init()
        }.onSuccess {
            Log.i(TAG, "RemoteConfig client initialised")
        }.onFailure { error ->
            Log.w(TAG, "RemoteConfig init failed, using local defaults", error)
        }
    }
}
