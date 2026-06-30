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
import com.byteflipper.everbook.domain.translation.BookTranslation
import com.byteflipper.everbook.domain.translation.BookTranslationStatus
import com.byteflipper.everbook.domain.translation.TranslationException
import javax.inject.Inject

private const val BOOK_TRANSLATION_LOG = "BookTranslation"

class PauseBookTranslation @Inject constructor(
    private val repository: BookTranslationRepository,
    private val workScheduler: BookTranslationWorkScheduler
) {
    suspend fun execute(translationId: Long): BookTranslation {
        val translation = repository.getTranslation(translationId)
            ?: throw TranslationException("Book translation was not found.")
        if (!translation.isBusy) return translation

        val paused = translation.copy(
            status = BookTranslationStatus.PAUSED,
            errorMessage = null,
            updatedAt = System.currentTimeMillis()
        )
        Log.i(
            BOOK_TRANSLATION_LOG,
            "Pause requested: translationId=$translationId " +
                    "completed=${translation.completedUnits}/${translation.totalUnits}"
        )
        repository.updateTranslation(paused)
        workScheduler.cancel(translationId)
        return paused
    }
}
