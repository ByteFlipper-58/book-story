/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.library

import androidx.compose.runtime.Composable
import com.byteflipper.everbook.domain.library.book.SelectableBook
import com.byteflipper.everbook.domain.library.display.LibraryLayout as LibraryLayoutEnum

@Composable
fun LibraryLayout(
    books: List<SelectableBook>,
    layout: LibraryLayoutEnum,
    gridSize: Int,
    autoGridSize: Boolean,
    itemContent: @Composable (book: SelectableBook) -> Unit
) {
    when (layout) {
        LibraryLayoutEnum.LIST -> {
            LibraryListLayout(
                books = books,
                itemContent = itemContent
            )
        }

        LibraryLayoutEnum.GRID -> {
            LibraryGridLayout(
                books = books,
                gridSize = gridSize,
                autoGridSize = autoGridSize,
                itemContent = itemContent
            )
        }
    }
}
