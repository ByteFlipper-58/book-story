/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.data.repository

import com.byteflipper.everbook.data.local.dto.BookmarkEntity
import com.byteflipper.everbook.data.local.room.BookDao
import com.byteflipper.everbook.domain.reader.Bookmark
import com.byteflipper.everbook.domain.reader.BookmarkKind
import com.byteflipper.everbook.domain.repository.BookmarkRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Reader annotations repository (bookmarks, highlights, notes). Wraps [BookDao] and maps between
 * [BookmarkEntity] and [Bookmark] inline — the shapes are 1:1, so no dedicated mapper is needed.
 */
@Singleton
class BookmarkRepositoryImpl @Inject constructor(
    private val database: BookDao
) : BookmarkRepository {

    override fun observeBookmarks(bookId: Int): Flow<List<Bookmark>> {
        return database.observeBookmarks(bookId).map { entities ->
            entities.map { it.toBookmark() }
        }
    }

    override suspend fun upsertBookmark(bookmark: Bookmark): Int {
        return database.insertBookmark(bookmark.toEntity()).toInt()
    }

    override suspend fun getBookmark(id: Int): Bookmark? {
        return database.getBookmarkById(id)?.toBookmark()
    }

    override suspend fun deleteBookmark(id: Int) {
        database.deleteBookmark(id)
    }

    override suspend fun deleteBookmarksForBook(bookId: Int) {
        database.deleteBookmarksForBook(bookId)
    }

    private fun BookmarkEntity.toBookmark() = Bookmark(
        id = id,
        bookId = bookId,
        kind = runCatching { BookmarkKind.valueOf(kind) }.getOrDefault(BookmarkKind.BOOKMARK),
        chapterIndex = chapterIndex,
        chapterTitle = chapterTitle,
        paragraphIndex = paragraphIndex,
        charStart = charStart,
        charEnd = charEnd,
        quotedText = quotedText,
        paragraphHash = paragraphHash,
        prefix = prefix,
        suffix = suffix,
        note = note,
        colorArgb = colorArgb,
        progress = progress,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private fun Bookmark.toEntity() = BookmarkEntity(
        id = id,
        bookId = bookId,
        kind = kind.name,
        chapterIndex = chapterIndex,
        chapterTitle = chapterTitle,
        paragraphIndex = paragraphIndex,
        charStart = charStart,
        charEnd = charEnd,
        quotedText = quotedText,
        paragraphHash = paragraphHash,
        prefix = prefix,
        suffix = suffix,
        note = note,
        colorArgb = colorArgb,
        progress = progress,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
