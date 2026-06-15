/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.domain.use_case.translation

import android.util.Log
import com.byteflipper.everbook.domain.repository.BookTranslationRepository
import com.byteflipper.everbook.domain.repository.BookTranslationWorkScheduler
import javax.inject.Inject

private const val BOOK_TRANSLATION_LOG = "BookTranslation"

/**
 * Removes a book translation and its entries from the database, cancelling any in-flight worker
 * first so the executor doesn't write progress back into a row that's about to be deleted.
 */
class DeleteBookTranslation @Inject constructor(
    private val repository: BookTranslationRepository,
    private val workScheduler: BookTranslationWorkScheduler
) {
    suspend fun execute(translationId: Long) {
        Log.i(BOOK_TRANSLATION_LOG, "Delete requested: translationId=$translationId")
        workScheduler.cancel(translationId)
        repository.deleteTranslation(translationId)
    }
}
