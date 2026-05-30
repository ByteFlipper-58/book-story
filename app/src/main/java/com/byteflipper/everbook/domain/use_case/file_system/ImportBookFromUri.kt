/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.domain.use_case.file_system

import android.net.Uri
import com.byteflipper.everbook.R
import com.byteflipper.everbook.domain.library.book.NullableBook
import com.byteflipper.everbook.domain.library.book.BookWithCover
import com.byteflipper.everbook.domain.repository.FileSystemRepository
import com.byteflipper.everbook.domain.ui.UIText
import com.byteflipper.everbook.domain.use_case.book.InsertBook
import javax.inject.Inject

class ImportBookFromUri @Inject constructor(
    private val repository: FileSystemRepository,
    private val insertBook: InsertBook
) {

    suspend fun execute(uri: Uri): Int? {
        val book = prepare(uri) as? NullableBook.NotNull ?: return null
        return insert(book.bookWithCover ?: return null)
    }

    suspend fun prepare(uri: Uri): NullableBook {
        val cachedFile = repository.copyExternalBookToPrivateStorage(uri)
            ?: return NullableBook.Null(
                uri.lastPathSegment?.substringAfterLast('/') ?: "unknown",
                UIText.StringResource(R.string.error_something_went_wrong)
            )

        return repository.getBookFromFile(cachedFile)
    }

    suspend fun insert(bookWithCover: BookWithCover): Int {
        return insertBook.execute(bookWithCover)
    }
}
