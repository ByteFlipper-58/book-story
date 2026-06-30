/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.domain.repository

import com.byteflipper.everbook.domain.library.book.Book
import com.byteflipper.everbook.domain.library.book.BookWithCover
import com.byteflipper.everbook.domain.reader.ReaderText
import com.byteflipper.everbook.domain.util.CoverImage

interface BookRepository {

    suspend fun getBooks(
        query: String
    ): List<Book>

    suspend fun getBooksById(
        ids: List<Int>
    ): List<Book>

    suspend fun getBookText(
        bookId: Int,
        onChunk: (suspend (List<ReaderText>) -> Unit)? = null
    ): List<ReaderText>

    suspend fun insertBook(
        bookWithCover: BookWithCover
    ): Int

    suspend fun updateBook(
        book: Book
    )

    suspend fun updateCoverImageOfBook(
        bookWithOldCover: Book,
        newCoverImage: CoverImage?
    )

    suspend fun deleteBooks(
        books: List<Book>
    )

    fun cancelReaderCacheWarmUps()

    suspend fun canResetCover(
        bookId: Int
    ): Boolean

    suspend fun resetCoverImage(
        bookId: Int
    ): Boolean

    suspend fun setCategories(
        bookId: Int,
        categoryIds: List<Int>
    )

    suspend fun addBookToCategory(
        bookId: Int,
        categoryId: Int
    )

    suspend fun removeBookFromCategory(
        bookId: Int,
        categoryId: Int
    )

    /** Latest open timestamps for books whose primary status category equals [categoryId]. */
    suspend fun getLastOpenedByBookInCategory(
        categoryId: Int
    ): Map<Int, Long>

    /** Replaces category refs and primary status category for a batch of books. */
    suspend fun setCategoryForBooks(
        bookIds: List<Int>,
        categoryId: Int
    )
}
