/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.library

import androidx.compose.foundation.pager.PagerState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.focus.FocusRequester
import com.byteflipper.everbook.domain.library.book.SelectableBook
import com.byteflipper.everbook.domain.library.category.CategoryWithBooks
import com.byteflipper.everbook.domain.library.display.LibraryLayout
import com.byteflipper.everbook.domain.library.display.LibraryTitlePosition
import com.byteflipper.everbook.domain.util.BottomSheet
import com.byteflipper.everbook.domain.util.Dialog
import com.byteflipper.everbook.ui.library.LibraryEvent

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun LibraryContent(
    books: List<SelectableBook>,
    selectedItemsCount: Int,
    hasSelectedItems: Boolean,
    titlePosition: LibraryTitlePosition,
    readButton: Boolean,
    showProgress: Boolean,
    showBookCount: Boolean,
    showCategoryTabs: Boolean,
    showSearch: Boolean,
    searchQuery: String,
    bookCount: Int,
    focusRequester: FocusRequester,
    pagerState: PagerState,
    isLoading: Boolean,
    isRefreshing: Boolean,
    doublePressExit: Boolean,
    categories: List<CategoryWithBooks>,
    layout: LibraryLayout,
    gridSize: Int,
    autoGridSize: Boolean,
    refreshState: PullRefreshState,
    dialog: Dialog?,
    bottomSheet: BottomSheet?,
    filterActive: Boolean,
    selectBook: (LibraryEvent.OnSelectBook) -> Unit,
    searchVisibility: (LibraryEvent.OnSearchVisibility) -> Unit,
    requestFocus: (LibraryEvent.OnRequestFocus) -> Unit,
    searchQueryChange: (LibraryEvent.OnSearchQueryChange) -> Unit,
    search: (LibraryEvent.OnSearch) -> Unit,
    clearSelectedBooks: (LibraryEvent.OnClearSelectedBooks) -> Unit,
    showCategoriesDialog: (LibraryEvent.OnShowCategoriesDialog) -> Unit,
    showDeleteDialog: (LibraryEvent.OnShowDeleteDialog) -> Unit,
    actionSetCategoriesDialog: (LibraryEvent.OnActionSetCategoriesDialog) -> Unit,
    actionDeleteDialog: (LibraryEvent.OnActionDeleteDialog) -> Unit,
    dismissDialog: (LibraryEvent.OnDismissDialog) -> Unit,
    showFilterBottomSheet: (LibraryEvent.OnShowFilterBottomSheet) -> Unit,
    dismissBottomSheet: (LibraryEvent.OnDismissBottomSheet) -> Unit,
    navigateToBrowse: () -> Unit,
    navigateToBookInfo: (id: Int) -> Unit,
    navigateToReader: (id: Int) -> Unit
) {
    LibraryDialog(
        dialog = dialog,
        books = books,
        categories = categories,
        selectedItemsCount = selectedItemsCount,
        actionSetCategoriesDialog = actionSetCategoriesDialog,
        actionDeleteDialog = actionDeleteDialog,
        dismissDialog = dismissDialog
    )

    LibraryBottomSheet(
        bottomSheet = bottomSheet,
        dismissBottomSheet = dismissBottomSheet
    )

    LibraryScaffold(
        selectedItemsCount = selectedItemsCount,
        hasSelectedItems = hasSelectedItems,
        titlePosition = titlePosition,
        readButton = readButton,
        showProgress = showProgress,
        showBookCount = showBookCount,
        showCategoryTabs = showCategoryTabs,
        showSearch = showSearch,
        searchQuery = searchQuery,
        bookCount = bookCount,
        focusRequester = focusRequester,
        pagerState = pagerState,
        isLoading = isLoading,
        isRefreshing = isRefreshing,
        filterActive = filterActive,
        layout = layout,
        gridSize = gridSize,
        autoGridSize = autoGridSize,
        categories = categories,
        searchVisibility = searchVisibility,
        requestFocus = requestFocus,
        searchQueryChange = searchQueryChange,
        search = search,
        selectBook = selectBook,
        clearSelectedBooks = clearSelectedBooks,
        showCategoriesDialog = showCategoriesDialog,
        showDeleteDialog = showDeleteDialog,
        showFilterBottomSheet = showFilterBottomSheet,
        refreshState = refreshState,
        navigateToBrowse = navigateToBrowse,
        navigateToBookInfo = navigateToBookInfo,
        navigateToReader = navigateToReader
    )

    LibraryBackHandler(
        hasSelectedItems = hasSelectedItems,
        showSearch = showSearch,
        pagerState = pagerState,
        doublePressExit = doublePressExit,
        clearSelectedBooks = clearSelectedBooks,
        searchVisibility = searchVisibility,
    )
}
