/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.data.di

import com.byteflipper.everbook.data.ads.PlayStoreAdSessionController
import com.byteflipper.everbook.data.ads.PlayStoreInterstitialAdManager
import com.byteflipper.everbook.data.ads.PlayStoreNativeReaderAdManager
import com.byteflipper.everbook.data.distribution.PlayStoreDistributionStartup
import com.byteflipper.everbook.data.distribution.PlayStoreManualUpdateChecker
import com.byteflipper.everbook.data.distribution.PlayStoreStoreUpdateController
import com.byteflipper.everbook.data.distribution.PlayStoreUpdateNotificationChecker
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
        impl: PlayStoreAdSessionController
    ): AdSessionController

    @Binds
    @Singleton
    abstract fun bindDistributionStartup(
        impl: PlayStoreDistributionStartup
    ): DistributionStartup

    @Binds
    @Singleton
    abstract fun bindReaderEntryActionController(
        impl: PlayStoreInterstitialAdManager
    ): ReaderEntryActionController

    @Binds
    @Singleton
    abstract fun bindReaderInlineContentController(
        impl: PlayStoreNativeReaderAdManager
    ): ReaderInlineContentController

    @Binds
    @Singleton
    abstract fun bindStoreUpdateController(
        impl: PlayStoreStoreUpdateController
    ): StoreUpdateController

    @Binds
    @Singleton
    abstract fun bindManualUpdateChecker(
        impl: PlayStoreManualUpdateChecker
    ): ManualUpdateChecker

    @Binds
    @Singleton
    abstract fun bindUpdateNotificationChecker(
        impl: PlayStoreUpdateNotificationChecker
    ): UpdateNotificationChecker
}
