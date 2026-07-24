/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.domain.use_case.bookmark

import com.byteflipper.everbook.domain.reader.Bookmark
import com.byteflipper.everbook.domain.repository.BookmarkRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveBookmarks @Inject constructor(
    private val repository: BookmarkRepository
) {
    fun execute(bookId: Int): Flow<List<Bookmark>> {
        return repository.observeBookmarks(bookId)
    }
}
