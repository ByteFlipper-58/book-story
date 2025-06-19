/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.domain.use_case.book

import com.byteflipper.everbook.domain.repository.BookRepository
import javax.inject.Inject

class RemoveBookFromCategory @Inject constructor(
    private val repository: BookRepository
) {
    suspend operator fun invoke(bookId: Int, categoryId: Int) {
        repository.removeBookFromCategory(bookId, categoryId)
    }
} 