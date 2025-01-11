package com.byteflipper.everbook.presentation.browse

import androidx.compose.runtime.Composable
import com.byteflipper.everbook.domain.util.BottomSheet
import com.byteflipper.everbook.ui.browse.BrowseEvent
import com.byteflipper.everbook.ui.browse.BrowseScreen

@Composable
fun BrowseBottomSheet(
    bottomSheet: BottomSheet?,
    dismissBottomSheet: (BrowseEvent.OnDismissBottomSheet) -> Unit
) {
    when (bottomSheet) {
        BrowseScreen.FILTER_BOTTOM_SHEET -> {
            BrowseFilterBottomSheet(
                dismissBottomSheet = dismissBottomSheet
            )
        }
    }
}