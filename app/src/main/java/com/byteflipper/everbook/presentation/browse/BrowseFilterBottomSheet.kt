package com.byteflipper.everbook.presentation.browse

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import com.byteflipper.everbook.presentation.core.components.common.LazyColumnWithScrollbar
import com.byteflipper.everbook.presentation.core.components.modal_bottom_sheet.ModalBottomSheet
import com.byteflipper.everbook.presentation.settings.browse.filter.BrowseFilterSubcategory
import com.byteflipper.everbook.presentation.settings.browse.general.BrowseGeneralSubcategory
import com.byteflipper.everbook.presentation.settings.browse.sort.BrowseSortSubcategory
import com.byteflipper.everbook.ui.browse.BrowseEvent

private var initialPage = 0

@Composable
fun BrowseFilterBottomSheet(
    dismissBottomSheet: (BrowseEvent.OnDismissBottomSheet) -> Unit
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
            dismissBottomSheet(BrowseEvent.OnDismissBottomSheet)
        },
        sheetGesturesEnabled = false
    ) {
        BrowseFilterBottomSheetTabRow(
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
                        BrowseGeneralSubcategory(
                            showTitle = false,
                            showDivider = false
                        )
                    }
                }

                1 -> {
                    LazyColumnWithScrollbar(modifier = Modifier.fillMaxSize()) {
                        BrowseFilterSubcategory(
                            showTitle = false,
                            showDivider = false
                        )
                    }
                }

                2 -> {
                    LazyColumnWithScrollbar(modifier = Modifier.fillMaxSize()) {
                        BrowseSortSubcategory(
                            showTitle = false,
                            showDivider = false
                        )
                    }
                }
            }
        }
    }
}