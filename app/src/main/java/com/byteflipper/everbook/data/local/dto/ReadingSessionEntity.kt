/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.data.local.dto

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A single reading session: from opening a book to leaving the reader.
 * Source of truth for reading statistics (total time, streaks, per-book time).
 */
@Entity(indices = [Index("bookId")])
data class ReadingSessionEntity(
    @PrimaryKey(true)
    val id: Int = 0,
    val bookId: Int,
    val startTime: Long,
    val endTime: Long,
    val progressStart: Float,
    val progressEnd: Float,
    val pagesRead: Int = 0
)
