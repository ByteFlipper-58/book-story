/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.domain.repository

import com.byteflipper.everbook.domain.translation.TranslationModelState

interface TranslationModelRepository {
    val available: Boolean

    fun getAvailableModels(): List<TranslationModelState>

    suspend fun getModels(): List<TranslationModelState>

    suspend fun downloadModel(languageCode: String, requireWifi: Boolean)

    suspend fun deleteModel(languageCode: String)
}
