/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.data.local.dto

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(indices = [Index("bookId")])
data class BookmarkEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val bookId: Int,
    val kind: String,
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
)
