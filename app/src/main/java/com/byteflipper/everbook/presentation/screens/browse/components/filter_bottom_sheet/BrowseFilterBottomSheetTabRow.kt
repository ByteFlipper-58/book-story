package com.byteflipper.everbook.presentation.screens.browse.components.filter_bottom_sheet

import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.byteflipper.everbook.R
import com.byteflipper.everbook.presentation.core.components.modal_bottom_sheet.ModalBottomSheetTabRow
import com.byteflipper.everbook.presentation.screens.browse.data.BrowseEvent

/**
 * Browse Filter Bottom Sheet Tab Row.
 * Lets user switch tabs in bottom sheet.
 *
 * @param onEvent [BrowseEvent] callback.
 * @param pagerState [PagerState].
 */
@Composable
fun BrowseFilterBottomSheetTabRow(
    onEvent: (BrowseEvent) -> Unit,
    pagerState: PagerState
) {
    val tabItems = listOf(
        stringResource(id = R.string.general_tab),
        stringResource(id = R.string.filter_tab),
        stringResource(id = R.string.sort_tab)
    )

    ModalBottomSheetTabRow(
        selectedTabIndex = pagerState.currentPage,
        tabs = tabItems
    ) { index ->
        onEvent(BrowseEvent.OnScrollToFilterPage(index, pagerState))
    }
}