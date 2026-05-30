/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.domain.use_case.history

import com.byteflipper.everbook.domain.repository.BookRepository
import com.byteflipper.everbook.domain.repository.HistoryRepository
import javax.inject.Inject

class GetLatestAvailableHistoryBook @Inject constructor(
    private val historyRepository: HistoryRepository,
    private val bookRepository: BookRepository
) {

    suspend fun execute(): Int? {
        val history = historyRepository.getHistory().sortedByDescending { it.time }
        if (history.isEmpty()) return null

        val availableBooks = bookRepository.getBooksById(
            history.map { it.bookId }.distinct()
        )
        val availableBookIds = availableBooks.map { it.id }.toSet()

        return history.firstOrNull { it.bookId in availableBookIds }?.bookId
    }
}
