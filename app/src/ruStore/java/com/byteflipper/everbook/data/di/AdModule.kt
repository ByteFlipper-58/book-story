/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.data.di

import com.byteflipper.everbook.data.ads.AdMobInterstitialAdManager
import com.byteflipper.everbook.data.ads.AdMobNativeReaderAdManager
import com.byteflipper.everbook.data.ads.DefaultAdSessionController
import com.byteflipper.everbook.data.distribution.RuStoreDistributionStartup
import com.byteflipper.everbook.data.distribution.RuStoreManualUpdateChecker
import com.byteflipper.everbook.data.distribution.RuStoreStoreUpdateController
import com.byteflipper.everbook.data.distribution.RuStoreUpdateNotificationChecker
import com.byteflipper.everbook.domain.ads.AdSessionController
import com.byteflipper.everbook.domain.distribution.DistributionStartup
import com.byteflipper.everbook.domain.distribution.ManualUpdateChecker
import com.byteflipper.everbook.domain.distribution.ReaderEntryActionController
import com.byteflipper.everbook.domain.distribution.ReaderInlineContentController
import com.byteflipper.everbook.domain.distribution.StoreUpdateController
import com.byteflipper.everbook.domain.distribution.UpdateNotificationChecker
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AdModule {
    @Binds
    @Singleton
    abstract fun bindAdSessionController(
        impl: DefaultAdSessionController
    ): AdSessionController

    @Binds
    @Singleton
    abstract fun bindDistributionStartup(
        impl: RuStoreDistributionStartup
    ): DistributionStartup

    @Binds
    @Singleton
    abstract fun bindReaderEntryActionController(
        impl: AdMobInterstitialAdManager
    ): ReaderEntryActionController

    @Binds
    @Singleton
    abstract fun bindReaderInlineContentController(
        impl: AdMobNativeReaderAdManager
    ): ReaderInlineContentController

    @Binds
    @Singleton
    abstract fun bindStoreUpdateController(
        impl: RuStoreStoreUpdateController
    ): StoreUpdateController

    @Binds
    @Singleton
    abstract fun bindManualUpdateChecker(
        impl: RuStoreManualUpdateChecker
    ): ManualUpdateChecker

    @Binds
    @Singleton
    abstract fun bindUpdateNotificationChecker(
        impl: RuStoreUpdateNotificationChecker
    ): UpdateNotificationChecker
}
