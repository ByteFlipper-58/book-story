/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.domain.repository

import com.byteflipper.everbook.domain.history.History

interface HistoryRepository {

    suspend fun insertHistory(
        history: History
    )

    suspend fun getHistory(): List<History>

    suspend fun getLatestBookHistory(
        bookId: Int
    ): History?

    suspend fun deleteWholeHistory()

    suspend fun deleteBookHistory(
        bookId: Int
    )

    suspend fun deleteHistory(
        history: History
    )
}