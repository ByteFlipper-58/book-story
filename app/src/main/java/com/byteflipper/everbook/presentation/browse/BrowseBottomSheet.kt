/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

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