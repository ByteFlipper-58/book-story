/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

@file:Suppress("SameReturnValue")

package com.byteflipper.everbook.presentation.core.constants

import androidx.compose.ui.graphics.Color
import com.byteflipper.everbook.domain.library.book.Book
import com.byteflipper.everbook.domain.library.category.Category
import com.byteflipper.everbook.domain.reader.ColorPreset
import com.byteflipper.everbook.domain.ui.UIText

// Main State
fun provideMainState() = "main_state"

// Empty Book
fun provideEmptyBook() = Book(
    id = -1,
    title = "",
    author = UIText.StringValue(""),
    description = null,
    filePath = "",
    coverImage = null,
    scrollIndex = 0,
    scrollOffset = 0,
    progress = 0f,
    lastOpened = null,
    category = Category.READING,
    categoryIds = listOf(0)
)

// Default Color Preset
fun provideDefaultColorPreset() = ColorPreset(
    id = -1,
    name = null,
    backgroundColor = Color(0xFFFAF8FF), // Blue Light Surface (hardcoded)
    fontColor = Color(0xFF44464F), // Blue Light OnSurfaceVariant (hardcoded)
    isSelected = false
)