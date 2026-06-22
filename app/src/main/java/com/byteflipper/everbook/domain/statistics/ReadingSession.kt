/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.domain.statistics

import androidx.compose.runtime.Immutable

/**
 * A single reading session, from opening a book to leaving the reader.
 */
@Immutable
data class ReadingSession(
    val id: Int = 0,
    val bookId: Int,
    val startTime: Long,
    val endTime: Long,
    val progressStart: Float,
    val progressEnd: Float,
    val pagesRead: Int = 0
) {
    val durationMs: Long get() = (endTime - startTime).coerceAtLeast(0)
}
