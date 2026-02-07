/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.library

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.byteflipper.everbook.domain.library.book.SelectableBook
import com.byteflipper.everbook.presentation.core.components.common.LazyVerticalGridWithScrollbar
import com.byteflipper.everbook.presentation.core.constants.providePrimaryScrollbar

@Composable
fun LibraryGridLayout(
    books: List<SelectableBook>,
    gridSize: Int,
    autoGridSize: Boolean,
    itemContent: @Composable (book: SelectableBook) -> Unit
) {
    LazyVerticalGridWithScrollbar(
        columns = if (autoGridSize) GridCells.Adaptive(120.dp)
        else GridCells.Fixed(gridSize.coerceAtLeast(1)),
        modifier = Modifier.fillMaxSize(),
        scrollbarSettings = providePrimaryScrollbar(false),
        contentPadding = PaddingValues(8.dp)
    ) {
        items(
            books,
            key = { it.data.id }
        ) { book ->
            Box(modifier = Modifier.animateItem()) {
                itemContent(book)
            }
        }
    }
}
