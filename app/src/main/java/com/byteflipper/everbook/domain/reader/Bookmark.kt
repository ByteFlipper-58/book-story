/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.domain.reader

import androidx.compose.runtime.Immutable

@Immutable
data class Bookmark(
    val id: Int = 0,
    val bookId: Int,
    val kind: BookmarkKind,
    val chapterIndex: Int,
    val chapterTitle: String,
    val paragraphIndex: Int,
    val charStart: Int,
    val charEnd: Int,
    val quotedText: String = "",
    val paragraphHash: Int,
    val prefix: String = "",
    val suffix: String = "",
    val note: String? = null,
    val colorArgb: Int? = null,
    val progress: Float,
    val createdAt: Long,
    val updatedAt: Long
) {
    val isPositionBookmark: Boolean get() = kind == BookmarkKind.BOOKMARK

    val isHighlight: Boolean get() = kind == BookmarkKind.HIGHLIGHT

    val hasNote: Boolean get() = !note.isNullOrBlank()
}

enum class BookmarkKind {
    BOOKMARK,
    HIGHLIGHT,
    NOTE
}
