/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.domain.use_case.book

import com.byteflipper.everbook.domain.library.book.Book
import com.byteflipper.everbook.domain.repository.BookRepository
import com.byteflipper.everbook.domain.repository.HistoryRepository
import javax.inject.Inject

class DeleteBooks @Inject constructor(
    private val bookRepository: BookRepository,
    private val historyRepository: HistoryRepository
) {

    suspend fun execute(books: List<Book>) {
        bookRepository.deleteBooks(books)
        books.forEach {
            historyRepository.deleteBookHistory(it.id)
        }
    }
}