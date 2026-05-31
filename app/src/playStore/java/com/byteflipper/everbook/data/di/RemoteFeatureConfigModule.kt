/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.data.di

import com.byteflipper.everbook.data.config.PlayStoreRemoteFeatureConfig
import com.byteflipper.everbook.domain.config.RemoteFeatureConfig
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RemoteFeatureConfigModule {
    @Binds
    @Singleton
    abstract fun bindRemoteFeatureConfig(
        impl: PlayStoreRemoteFeatureConfig
    ): RemoteFeatureConfig
}
