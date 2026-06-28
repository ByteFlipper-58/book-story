/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.domain.use_case.category

import com.byteflipper.everbook.domain.library.category.CategoryDefaults
import com.byteflipper.everbook.domain.repository.BookRepository
import javax.inject.Inject

/**
 * Moves books that are still in "Reading" but haven't been opened for [STALE_THRESHOLD_MS] into
 * "Dropped". Only "Reading" books are affected — "Already read" / "Planning" / custom categories are
 * left untouched (matches the agreed auto-categorization rules). Books with no open history are
 * skipped. Run periodically from a background worker.
 *
 * @return ids of the books that were moved to "Dropped".
 */
class DropStaleReadingBooks @Inject constructor(
    private val repository: BookRepository
) {
    suspend operator fun invoke(now: Long): List<Int> {
        val dropped = repository.getLastOpenedByBookInCategory(CategoryDefaults.READING)
            .filterValues { lastOpened -> now - lastOpened >= STALE_THRESHOLD_MS }
            .keys
            .toList()

        repository.setCategoryForBooks(dropped, CategoryDefaults.DROPPED)
        return dropped
    }

    companion object {
        /** A "Reading" book untouched for this long is considered dropped. */
        const val STALE_THRESHOLD_MS = 7L * 24 * 60 * 60 * 1000 // 7 days
    }
}
