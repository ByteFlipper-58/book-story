/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.library

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.byteflipper.everbook.domain.library.category.CategoryWithBooks
import com.byteflipper.everbook.domain.library.display.LibraryLayout as LibraryLayoutEnum
import com.byteflipper.everbook.domain.library.display.LibraryTitlePosition
import com.byteflipper.everbook.ui.library.LibraryEvent
import com.byteflipper.everbook.ui.theme.DefaultTransition

@Composable
fun LibraryPager(
    pagerState: PagerState,
    categories: List<CategoryWithBooks>,
    layout: LibraryLayoutEnum,
    gridSize: Int,
    autoGridSize: Boolean,
    hasSelectedItems: Boolean,
    titlePosition: LibraryTitlePosition,
    readButton: Boolean,
    showProgress: Boolean,
    isLoading: Boolean,
    isRefreshing: Boolean,
    selectBook: (LibraryEvent.OnSelectBook) -> Unit,
    navigateToBrowse: () -> Unit,
    navigateToBookInfo: (id: Int) -> Unit,
    navigateToReader: (id: Int) -> Unit,
) {
    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize(),
        // Pre-compose the neighbouring tab while idle so the heavy first-frame work (≈12 grid
        // items + cover image requests) doesn't land inside the swipe gesture. Without this the
        // incoming page is built synchronously as it's dragged into view, which is the main cause
        // of the janky tab swipe.
        beyondViewportPageCount = 1
    ) { index ->
        val category = remember(categories, index) {
            derivedStateOf {
                categories[index]
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            DefaultTransition(visible = !isLoading) {
                LibraryLayout(
                    books = category.value.books,
                    layout = layout,
                    gridSize = gridSize,
                    autoGridSize = autoGridSize
                ) { book ->
                    LibraryItem(
                        book = book,
                        layout = layout,
                        hasSelectedItems = hasSelectedItems,
                        titlePosition = titlePosition,
                        readButton = readButton,
                        showProgress = showProgress,
                        selectBook = { select ->
                            selectBook(
                                LibraryEvent.OnSelectBook(
                                    id = book.data.id,
                                    select = select
                                )
                            )
                        },
                        navigateToBookInfo = { navigateToBookInfo(book.data.id) },
                        navigateToReader = { navigateToReader(book.data.id) },
                    )
                }
            }

            LibraryEmptyPlaceholder(
                isLoading = isLoading,
                isRefreshing = isRefreshing,
                isBooksEmpty = category.value.books.isEmpty(),
                navigateToBrowse = navigateToBrowse
            )
        }
    }
}
