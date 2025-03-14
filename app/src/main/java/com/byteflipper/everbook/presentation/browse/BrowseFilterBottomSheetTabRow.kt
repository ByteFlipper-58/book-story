/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.browse

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.byteflipper.everbook.R
import com.byteflipper.everbook.presentation.core.components.modal_bottom_sheet.ModalBottomSheetTabRow

@Composable
fun BrowseFilterBottomSheetTabRow(
    currentPage: Int,
    scrollToPage: (Int) -> Unit
) {
    val tabItems = listOf(
        stringResource(id = R.string.filter_tab),
        stringResource(id = R.string.sort_tab),
        stringResource(id = R.string.display_tab)
    )

    ModalBottomSheetTabRow(
        selectedTabIndex = currentPage,
        tabs = tabItems
    ) { index ->
        scrollToPage(index)
    }
}