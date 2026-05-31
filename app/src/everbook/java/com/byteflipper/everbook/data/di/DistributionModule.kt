/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.data.di

import com.byteflipper.everbook.data.distribution.EverbookDistributionStartup
import com.byteflipper.everbook.data.distribution.EverbookReaderEntryActionController
import com.byteflipper.everbook.data.distribution.EverbookReaderInlineContentController
import com.byteflipper.everbook.domain.distribution.DistributionStartup
import com.byteflipper.everbook.domain.distribution.ReaderEntryActionController
import com.byteflipper.everbook.domain.distribution.ReaderInlineContentController
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DistributionModule {
    @Binds
    @Singleton
    abstract fun bindDistributionStartup(
        impl: EverbookDistributionStartup
    ): DistributionStartup

    @Binds
    @Singleton
    abstract fun bindReaderEntryActionController(
        impl: EverbookReaderEntryActionController
    ): ReaderEntryActionController

    @Binds
    @Singleton
    abstract fun bindReaderInlineContentController(
        impl: EverbookReaderInlineContentController
    ): ReaderInlineContentController
}
