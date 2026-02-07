/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.library

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.byteflipper.everbook.presentation.core.components.common.LazyColumnWithScrollbar
import com.byteflipper.everbook.presentation.core.components.modal_bottom_sheet.ModalBottomSheet
import com.byteflipper.everbook.presentation.settings.library.display.LibraryDisplaySubcategory
import com.byteflipper.everbook.presentation.settings.library.sort.LibrarySortSubcategory
import com.byteflipper.everbook.presentation.settings.library.tabs.LibraryTabsSubcategory
import com.byteflipper.everbook.ui.library.LibraryEvent
import kotlinx.coroutines.launch

private var initialPage = 0

@Composable
fun LibraryFilterBottomSheet(
    dismissBottomSheet: (LibraryEvent.OnDismissBottomSheet) -> Unit
) {
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(initialPage) { 3 }
    DisposableEffect(Unit) { onDispose { initialPage = pagerState.currentPage } }

    ModalBottomSheet(
        hasFixedHeight = true,
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.5f),
        dragHandle = {},
        onDismissRequest = {
            dismissBottomSheet(LibraryEvent.OnDismissBottomSheet)
        },
        sheetGesturesEnabled = false
    ) {
        LibraryFilterBottomSheetTabRow(
            currentPage = pagerState.currentPage,
            scrollToPage = {
                scope.launch {
                    pagerState.animateScrollToPage(it)
                }
            }
        )

        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
            when (page) {
                0 -> {
                    LazyColumnWithScrollbar(modifier = Modifier.fillMaxSize()) {
                        LibrarySortSubcategory(
                            showTitle = false,
                            showDivider = false
                        )
                    }
                }

                1 -> {
                    LazyColumnWithScrollbar(modifier = Modifier.fillMaxSize()) {
                        LibraryDisplaySubcategory(
                            showTitle = false,
                            showDivider = false
                        )
                    }
                }

                2 -> {
                    LazyColumnWithScrollbar(modifier = Modifier.fillMaxSize()) {
                        LibraryTabsSubcategory(
                            showTitle = false,
                            showDivider = false
                        )
                    }
                }
            }
        }
    }
}

