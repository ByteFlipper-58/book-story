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
import com.byteflipper.everbook.domain.translation.BookTranslationStatus
import javax.inject.Inject

private const val BOOK_TRANSLATION_LOG = "BookTranslation"

/**
 * Re-schedules in-flight translations on process start. A worker that was RUNNING (or sitting
 * QUEUED/PENDING) when the OS killed the process leaves its DB row in a non-terminal state with no
 * live worker — the translation would otherwise be stuck forever. Re-enqueuing with REPLACE cancels
 * any stale WorkManager record and starts a fresh worker; the executor skips already-translated
 * units, so no work is repeated.
 */
class ReconcileBookTranslations @Inject constructor(
    private val repository: BookTranslationRepository,
    private val workScheduler: BookTranslationWorkScheduler
) {
    suspend fun execute() {
        val orphans = repository.getAllTranslations().filter {
            it.status == BookTranslationStatus.RUNNING ||
                    it.status == BookTranslationStatus.QUEUED ||
                    it.status == BookTranslationStatus.PENDING
        }
        if (orphans.isEmpty()) return

        Log.i(
            BOOK_TRANSLATION_LOG,
            "Reconcile: re-enqueuing ${orphans.size} in-flight translation(s) after process start"
        )
        orphans.forEach { translation ->
            runCatching {
                workScheduler.enqueue(translation, replaceExisting = true)
            }.onFailure { throwable ->
                Log.e(
                    BOOK_TRANSLATION_LOG,
                    "Reconcile failed to re-enqueue translationId=${translation.id}",
                    throwable
                )
            }
        }
    }
}
