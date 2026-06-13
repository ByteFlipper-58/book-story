/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.domain.use_case.translation

import com.byteflipper.everbook.domain.reader.ReaderText
import com.byteflipper.everbook.domain.repository.BookTranslationRepository
import com.byteflipper.everbook.domain.translation.BookTranslation
import com.byteflipper.everbook.domain.translation.BookTranslationStatus
import com.byteflipper.everbook.domain.translation.BOOK_TRANSLATION_TEXT_CHANGED_MESSAGE
import javax.inject.Inject

class GetBookTranslations @Inject constructor(
    private val repository: BookTranslationRepository,
    private val planner: BookTranslationPlanner
) {
    suspend fun execute(
        bookId: Int,
        originalText: List<ReaderText> = emptyList()
    ): List<BookTranslation> {
        val translations = repository.getTranslations(bookId)
        if (originalText.isEmpty()) return translations

        val sourceFingerprint = planner.fingerprint(originalText)
        return translations.map { translation ->
            if (!translation.canRead || translation.sourceFingerprint == sourceFingerprint) {
                return@map translation
            }

            val staleTranslation = translation.copy(
                status = BookTranslationStatus.STALE,
                errorMessage = BOOK_TRANSLATION_TEXT_CHANGED_MESSAGE,
                updatedAt = System.currentTimeMillis()
            )
            repository.updateTranslation(staleTranslation)
            staleTranslation
        }.sortedByDescending { it.updatedAt }
    }
}
