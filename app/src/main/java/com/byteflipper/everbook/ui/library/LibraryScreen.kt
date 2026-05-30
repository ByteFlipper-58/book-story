/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.ui.library

import android.os.Parcelable
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.FocusRequester
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import com.byteflipper.everbook.R
import com.byteflipper.everbook.domain.library.book.SelectableBook
import com.byteflipper.everbook.domain.library.category.CategoryWithBooks
import com.byteflipper.everbook.domain.library.display.LibraryLayout
import com.byteflipper.everbook.domain.library.display.LibrarySortOrder
import com.byteflipper.everbook.domain.library.display.LibraryTitlePosition
import com.byteflipper.everbook.domain.navigator.Screen
import com.byteflipper.everbook.domain.ui.UIText
import com.byteflipper.everbook.presentation.library.LibraryContent
import com.byteflipper.everbook.presentation.navigator.LocalNavigator
import com.byteflipper.everbook.ui.book_info.BookInfoScreen
import com.byteflipper.everbook.ui.browse.BrowseScreen
import com.byteflipper.everbook.ui.history.HistoryScreen
import com.byteflipper.everbook.ui.main.MainEvent
import com.byteflipper.everbook.ui.main.MainModel
import com.byteflipper.everbook.ui.reader.ReaderScreen

@Parcelize
object LibraryScreen : Screen, Parcelable {

    @IgnoredOnParcel
    const val MOVE_DIALOG = "move_dialog"

    @IgnoredOnParcel
    const val DELETE_DIALOG = "delete_dialog"

    @IgnoredOnParcel
    const val FILTER_BOTTOM_SHEET = "filter_bottom_sheet"

    @IgnoredOnParcel
    val refreshListChannel: Channel<Long> = Channel(Channel.CONFLATED)

    @IgnoredOnParcel
    val scrollToPageCompositionChannel: Channel<Int> = Channel(Channel.CONFLATED)

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current

        val screenModel = hiltViewModel<LibraryModel>()
        val mainModel = hiltViewModel<MainModel>()

        val state = screenModel.state.collectAsStateWithLifecycle()
        val mainState = mainModel.state.collectAsStateWithLifecycle()

        val categoriesModel = hiltViewModel<CategoriesModel>()
        val lifecycleOwner = LocalLifecycleOwner.current
        val categoriesState = categoriesModel.categories.collectAsStateWithLifecycle(
            initialValue = emptyList(),
            lifecycle = lifecycleOwner.lifecycle
        )
        val settings = mainState.value
        val allCategories = remember(categoriesState.value) {
            categoriesState.value
                .sortedBy { it.position }
        }
        val visibleNonDefaultCategories = remember(allCategories) {
            allCategories
                .filter { it.id != 0 && it.isVisible }
                .sortedBy { it.position }
        }
        val visibleCategoryIds = remember(visibleNonDefaultCategories) {
            visibleNonDefaultCategories.map { it.id }.toSet()
        }
        val hiddenNonDefaultCategories = remember(allCategories) {
            allCategories
                .filter { it.id != 0 && !it.isVisible }
        }
        val fallbackAllCategory = remember {
            com.byteflipper.everbook.domain.library.custom_category.Category(
                id = 0,
                name = "All",
                kind = "SYSTEM_MAIN",
                isVisible = true,
                position = -1,
                isDefault = true,
                title = UIText.StringResource(R.string.all_tab)
            )
        }
        val allCategory = remember(allCategories, fallbackAllCategory) {
            allCategories.firstOrNull { it.id == 0 } ?: fallbackAllCategory
        }
        val showDefaultCategory = remember(
            settings.libraryShowDefaultTab,
            visibleNonDefaultCategories,
            hiddenNonDefaultCategories,
            allCategory,
            state.value.books,
            visibleCategoryIds
        ) {
            val allCategoryVisible = allCategory.isVisible || settings.libraryShowDefaultTab
            val hasBooksOutsideVisibleCategories = state.value.books.any { book ->
                book.data.categoryIds.none { it in visibleCategoryIds }
            }
            allCategoryVisible && (
                settings.libraryShowDefaultTab ||
                    visibleNonDefaultCategories.isEmpty() ||
                    hiddenNonDefaultCategories.isNotEmpty() ||
                    hasBooksOutsideVisibleCategories
            )
        }
        val tabCategories = remember(showDefaultCategory, allCategory, visibleNonDefaultCategories) {
            if (showDefaultCategory) {
                listOf(allCategory) + visibleNonDefaultCategories
            } else {
                visibleNonDefaultCategories
            }
        }
        val filterActive = remember(settings) {
            settings.libraryLayout != LibraryLayout.GRID ||
                settings.libraryAutoGridSize != true ||
                settings.libraryGridSize != 0 ||
                settings.libraryTitlePosition != LibraryTitlePosition.BELOW ||
                !settings.libraryShowReadButton ||
                !settings.libraryShowProgress ||
                !settings.libraryShowBookCount ||
                !settings.libraryShowCategoryTabs ||
                !settings.libraryShowDefaultTab ||
                settings.librarySortOrder != LibrarySortOrder.LAST_READ ||
                !settings.librarySortOrderDescending ||
                settings.libraryPerCategorySort
        }
        val categories = remember(
            state.value.books,
            tabCategories,
            settings.librarySortOrder,
            settings.librarySortOrderDescending,
            settings.libraryPerCategorySort
        ) {
            val mapped = tabCategories.map { cat ->
                val booksForCategory = if (cat.id == 0) {
                    state.value.books
                } else {
                    state.value.books.filter { it.data.categoryIds.contains(cat.id) }
                }

                val sortOrder = if (settings.libraryPerCategorySort) {
                    cat.sortOrder
                } else {
                    settings.librarySortOrder
                }
                val sortDescending = if (settings.libraryPerCategorySort) {
                    cat.sortOrderDescending
                } else {
                    settings.librarySortOrderDescending
                }

                CategoryWithBooks(
                    id = cat.id,
                    title = cat.title,
                    books = sortBooks(booksForCategory, sortOrder, sortDescending)
                )
            }

            if (mapped.isEmpty()) {
                val allTitle = UIText.StringResource(R.string.all_tab)
                return@remember listOf(
                    CategoryWithBooks(
                        id = 0,
                        title = allTitle,
                        books = sortBooks(
                            state.value.books,
                            settings.librarySortOrder,
                            settings.librarySortOrderDescending
                        )
                    )
                )
            }

            mapped
        }

        val focusRequester = remember { FocusRequester() }
        val refreshState = rememberPullRefreshState(
            refreshing = state.value.isRefreshing,
            onRefresh = {
                screenModel.onEvent(
                    LibraryEvent.OnRefreshList(
                        loading = false,
                        hideSearch = true
                    )
                )
            }
        )

        val pageCount = categories.size.coerceAtLeast(1)
        val categoryIds = remember(categories) { categories.map { it.id } }
        val resolvedTabId = remember(categoryIds, settings.libraryLastTabId) {
            if (settings.libraryLastTabId in categoryIds) {
                settings.libraryLastTabId
            } else {
                categoryIds.firstOrNull() ?: 0
            }
        }
        val savedPage = categoryIds.indexOf(resolvedTabId)
            .let { if (it >= 0) it else 0 }
        val pagerState = rememberPagerState(
            initialPage = savedPage.coerceIn(0, pageCount - 1)
        ) { pageCount }

        var suppressTabSync by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            scrollToPageCompositionChannel.receiveAsFlow().collectLatest {
                pagerState.animateScrollToPage(it)
            }
        }

        LaunchedEffect(resolvedTabId, categoryIds, pageCount) {
            if (categoryIds.isEmpty()) return@LaunchedEffect
            val targetPage = categoryIds.indexOf(resolvedTabId)
                .let { if (it >= 0) it else 0 }
                .coerceIn(0, pageCount - 1)
            suppressTabSync = true
            if (pagerState.currentPage != targetPage) {
                pagerState.scrollToPage(targetPage)
            }
            suppressTabSync = false
        }

        LaunchedEffect(pagerState, categoryIds, settings.libraryLastTabId) {
            if (categoryIds.isEmpty()) return@LaunchedEffect
            snapshotFlow { pagerState.currentPage }
                .distinctUntilChanged()
                .collect { page ->
                    if (suppressTabSync) return@collect
                    val categoryId = categoryIds.getOrNull(page) ?: return@collect
                    if (categoryId != settings.libraryLastTabId) {
                        mainModel.onEvent(
                            MainEvent.OnChangeLibraryLastTabId(categoryId)
                        )
                    }
                }
        }

        LibraryContent(
            books = state.value.books,
            selectedItemsCount = state.value.selectedItemsCount,
            hasSelectedItems = state.value.hasSelectedItems,
            titlePosition = settings.libraryTitlePosition,
            readButton = settings.libraryShowReadButton,
            showProgress = settings.libraryShowProgress,
            showBookCount = settings.libraryShowBookCount,
            showCategoryTabs = settings.libraryShowCategoryTabs,
            showSearch = state.value.showSearch,
            searchQuery = state.value.searchQuery,
            bookCount = state.value.books.count(),
            focusRequester = focusRequester,
            pagerState = pagerState,
            isLoading = state.value.isLoading,
            isRefreshing = state.value.isRefreshing,
            doublePressExit = mainState.value.doublePressExit,
            categories = categories,
            layout = settings.libraryLayout,
            gridSize = settings.libraryGridSize,
            autoGridSize = settings.libraryAutoGridSize,
            refreshState = refreshState,
            dialog = state.value.dialog,
            bottomSheet = state.value.bottomSheet,
            filterActive = filterActive,
            selectBook = screenModel::onEvent,
            searchVisibility = screenModel::onEvent,
            requestFocus = screenModel::onEvent,
            searchQueryChange = screenModel::onEvent,
            search = screenModel::onEvent,
            clearSelectedBooks = screenModel::onEvent,
            showCategoriesDialog = screenModel::onEvent,
            actionSetCategoriesDialog = screenModel::onEvent,
            actionDeleteDialog = screenModel::onEvent,
            showDeleteDialog = screenModel::onEvent,
            dismissDialog = screenModel::onEvent,
            showFilterBottomSheet = screenModel::onEvent,
            dismissBottomSheet = screenModel::onEvent,
            navigateToBrowse = {
                navigator.push(BrowseScreen)
            },
            navigateToReader = {
                HistoryScreen.insertHistoryChannel.trySend(it)
                navigator.push(ReaderScreen(it))
            },
            navigateToBookInfo = {
                navigator.push(BookInfoScreen(bookId = it))
            }
        )
    }
}

private fun sortBooks(
    books: List<SelectableBook>,
    sortOrder: LibrarySortOrder,
    descending: Boolean
): List<SelectableBook> {
    if (books.isEmpty()) return books

    fun key(book: SelectableBook): Comparable<*> {
        return when (sortOrder) {
            LibrarySortOrder.NAME -> book.data.title.trim().lowercase()
            LibrarySortOrder.LAST_READ -> book.data.lastOpened ?: 0L
            LibrarySortOrder.PROGRESS -> book.data.progress
            LibrarySortOrder.AUTHOR -> book.data.author.getAsString()?.lowercase() ?: ""
        }
    }

    val comparator = if (descending) {
        compareByDescending<SelectableBook> { key(it) }
    } else {
        compareBy { key(it) }
    }.thenBy { it.data.title.trim().lowercase() }

    return books.sortedWith(comparator)
}
