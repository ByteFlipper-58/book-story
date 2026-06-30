/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.ui.settings

import androidx.compose.runtime.Immutable
import com.byteflipper.everbook.domain.translation.TranslationModelState

@Immutable
data class TranslatorSettingsState(
    val modelManagerAvailable: Boolean = false,
    val models: List<TranslationModelState> = emptyList(),
    val busyLanguageCodes: Set<String> = emptySet(),
    val query: String = "",
    val filter: TranslationModelFilter = TranslationModelFilter.ALL,
    val isLoadingModels: Boolean = false,
    val errorMessage: String? = null
) {
    val downloadedModelsCount: Int
        get() = models.count { it.downloaded }

    val filteredModels: List<TranslationModelState>
        get() {
            val normalizedQuery = query.trim().lowercase()
            return models.filter { model ->
                val matchesFilter = when (filter) {
                    TranslationModelFilter.ALL -> true
                    TranslationModelFilter.DOWNLOADED -> model.downloaded
                    TranslationModelFilter.NOT_DOWNLOADED -> !model.downloaded
                }
                val matchesQuery = normalizedQuery.isBlank() ||
                        model.language.name.lowercase().contains(normalizedQuery) ||
                        model.language.code.lowercase().contains(normalizedQuery)
                matchesFilter && matchesQuery
            }.sortedWith(
                compareByDescending<TranslationModelState> { it.downloaded }
                    .thenBy { it.language.name.lowercase() }
                    .thenBy { it.language.code }
            )
        }
}

enum class TranslationModelFilter {
    ALL,
    DOWNLOADED,
    NOT_DOWNLOADED
}
