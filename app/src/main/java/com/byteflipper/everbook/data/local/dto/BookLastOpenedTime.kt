/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.data.local.dto

/**
 * Projection for the latest open timestamp of a book, used to batch-load `lastOpened` for many books
 * in a single query instead of one [HistoryEntity] lookup per book (see BookDao.getLatestHistoryTimes).
 */
data class BookLastOpenedTime(
    val bookId: Int,
    val time: Long
)
