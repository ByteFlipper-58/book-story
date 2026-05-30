/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.library

import androidx.compose.runtime.Composable
import com.byteflipper.everbook.domain.util.BottomSheet
import com.byteflipper.everbook.ui.library.LibraryEvent
import com.byteflipper.everbook.ui.library.LibraryScreen

@Composable
fun LibraryBottomSheet(
    bottomSheet: BottomSheet?,
    dismissBottomSheet: (LibraryEvent.OnDismissBottomSheet) -> Unit
) {
    when (bottomSheet) {
        LibraryScreen.FILTER_BOTTOM_SHEET -> {
            LibraryFilterBottomSheet(
                dismissBottomSheet = dismissBottomSheet
            )
        }
    }
}

