/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.domain.use_case.bookmark

import com.byteflipper.everbook.domain.repository.BookmarkRepository
import javax.inject.Inject

class DeleteBookmark @Inject constructor(
    private val repository: BookmarkRepository
) {
    suspend fun execute(id: Int) {
        repository.deleteBookmark(id)
    }
}
