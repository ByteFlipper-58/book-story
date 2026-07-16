/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.domain.repository

import com.byteflipper.everbook.domain.reader.Bookmark
import kotlinx.coroutines.flow.Flow

interface BookmarkRepository {

    fun observeBookmarks(bookId: Int): Flow<List<Bookmark>>

    /** Inserts or replaces a bookmark, returning its (possibly new) id. */
    suspend fun upsertBookmark(bookmark: Bookmark): Int

    suspend fun getBookmark(id: Int): Bookmark?

    suspend fun deleteBookmark(id: Int)

    suspend fun deleteBookmarksForBook(bookId: Int)
}
