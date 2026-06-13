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

class ResumeBookTranslation @Inject constructor(
    private val repository: BookTranslationRepository,
    private val workScheduler: BookTranslationWorkScheduler
) {
    suspend fun execute(translationId: Long): BookTranslation {
        val translation = repository.getTranslation(translationId)
            ?: throw TranslationException("Book translation was not found.")
        if (!translation.canResume) return translation

        val queued = translation.copy(
            status = BookTranslationStatus.QUEUED,
            errorMessage = null,
            queuedAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            completedAt = null
        )
        Log.i(
            BOOK_TRANSLATION_LOG,
            "Resume requested: translationId=$translationId " +
                    "completed=${translation.completedUnits}/${translation.totalUnits}"
        )
        repository.updateTranslation(queued)
        workScheduler.enqueue(queued, replaceExisting = true)
        return queued
    }
}
