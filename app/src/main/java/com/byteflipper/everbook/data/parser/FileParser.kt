/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.data.parser

import com.byteflipper.everbook.domain.file.CachedFile
import com.byteflipper.everbook.domain.library.book.BookWithCover


interface FileParser {
    suspend fun parse(cachedFile: CachedFile): BookWithCover?
}