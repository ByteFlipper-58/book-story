/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.domain.use_case.translation

import com.byteflipper.everbook.domain.repository.BookTranslationRepository
import com.byteflipper.everbook.domain.repository.BookTranslationWorkScheduler
import com.byteflipper.everbook.domain.translation.BookTranslationStatus
import android.util.Log
import javax.inject.Inject

private const val BOOK_TRANSLATION_LOG = "BookTranslation"

class CancelBookTranslation @Inject constructor(
    private val repository: BookTranslationRepository,
    private val workScheduler: BookTranslationWorkScheduler
) {
    suspend fun execute(translationId: Long) {
        Log.i(BOOK_TRANSLATION_LOG, "Cancel requested: translationId=$translationId")
        repository.updateTranslationStatus(
            translationId = translationId,
            status = BookTranslationStatus.CANCELLED,
            errorMessage = null
        )
        workScheduler.cancel(translationId)
    }
}
