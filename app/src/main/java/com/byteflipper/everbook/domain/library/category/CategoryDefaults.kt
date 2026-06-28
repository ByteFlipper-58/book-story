/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.domain.library.category

/**
 * Ids of the seeded system categories (see BookDatabase.PREPOPULATE_CATEGORIES / MIGRATION_9_10).
 * These ids are stable: the defaults are non-deletable and user categories get ids >= 5 via
 * AUTOINCREMENT, so [isDefaultStatus] reliably distinguishes them from custom categories.
 */
object CategoryDefaults {
    const val ALL = 0
    const val READING = 1
    const val ALREADY_READ = 2
    const val PLANNING = 3
    const val DROPPED = 4

    /** A book is considered fully read at this progress (ReaderModel returns exactly 1f at the end). */
    const val FINISHED_PROGRESS = 1f

    /** True when [categoryId] is one of the built-in status buckets (incl. 0 = no status / All-only). */
    fun isDefaultStatus(categoryId: Int): Boolean = categoryId in ALL..DROPPED

    /**
     * Computes the auto-assigned status category for a book based on reading behaviour.
     *
     * Rules (custom categories are never overwritten):
     * 1. custom category (`current` not a default status) → unchanged.
     * 2. finished (`progress >= 1f`) → [ALREADY_READ].
     * 3. opening the reader → [READING], unless the book is already [ALREADY_READ] (re-reading a
     *    finished book keeps its "read" status).
     * 4. otherwise → unchanged (avoids redundant writes while reading).
     */
    fun computeAutoStatus(current: Int, progress: Float, isOpening: Boolean): Int = when {
        !isDefaultStatus(current) -> current
        progress >= FINISHED_PROGRESS -> ALREADY_READ
        isOpening -> if (current == ALREADY_READ) ALREADY_READ else READING
        else -> current
    }
}
