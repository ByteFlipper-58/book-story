/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.data.local.dto

import androidx.room.Entity

/**
 * Кросс-таблица many-to-many между книгами и категориями.
 */
@Entity(primaryKeys = ["bookId", "categoryId"])
data class BookCategoryCrossRef(
    val bookId: Int,
    val categoryId: Int
) 