/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.domain.use_case.translation

import com.byteflipper.everbook.domain.reader.ReaderText
import com.byteflipper.everbook.domain.repository.BookTranslationRepository
import com.byteflipper.everbook.domain.translation.BookTranslationEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ObserveTranslatedBookText @Inject constructor(
    private val repository: BookTranslationRepository,
    private val planner: BookTranslationPlanner
) {
    fun execute(
        translationId: Long,
        originalText: List<ReaderText>
    ): Flow<TranslatedBookTextSnapshot> =
        repository.observeEntries(translationId)
            .distinctUntilChangedBy { entries ->
                entries.translationEntriesVersion()
            }
            .map { entries ->
                TranslatedBookTextSnapshot(
                    text = planner.applyTranslation(
                        original = originalText,
                        entries = entries
                    ),
                    translatedEntries = entries.size
                )
            }

    private fun List<BookTranslationEntry>.translationEntriesVersion(): String =
        joinToString(separator = "|") { entry ->
            "${entry.readerTextIndex}:${entry.updatedAt}:${entry.translatedText.hashCode()}"
        }
}

data class TranslatedBookTextSnapshot(
    val text: List<ReaderText>,
    val translatedEntries: Int
)
