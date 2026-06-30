/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.data.di

import com.byteflipper.everbook.data.privacy.RuStorePrivacyConsentManager
import com.byteflipper.everbook.domain.privacy.PrivacyConsentManager
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PrivacyConsentModule {
    @Binds
    @Singleton
    abstract fun bindPrivacyConsentManager(
        impl: RuStorePrivacyConsentManager
    ): PrivacyConsentManager
}
