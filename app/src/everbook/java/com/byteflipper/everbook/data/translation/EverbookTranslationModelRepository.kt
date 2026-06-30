/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.data.translation

import com.byteflipper.everbook.domain.repository.TranslationModelRepository
import com.byteflipper.everbook.domain.translation.TranslationException
import com.byteflipper.everbook.domain.translation.TranslationModelState
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EverbookTranslationModelRepository @Inject constructor() : TranslationModelRepository {
    override val available = false

    override fun getAvailableModels(): List<TranslationModelState> = emptyList()

    override suspend fun getModels(): List<TranslationModelState> = emptyList()

    override suspend fun downloadModel(languageCode: String, requireWifi: Boolean) {
        throw unavailableException()
    }

    override suspend fun deleteModel(languageCode: String) {
        throw unavailableException()
    }

    private fun unavailableException(): TranslationException =
        TranslationException("Offline translation models are unavailable in this build.")
}
