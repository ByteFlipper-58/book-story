/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.browse

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.byteflipper.everbook.domain.browse.display.BrowseLayout
import com.byteflipper.everbook.domain.browse.SelectableFile

@Composable
fun BrowseItem(
    modifier: Modifier = Modifier,
    layout: BrowseLayout,
    file: SelectableFile,
    hasSelectedItems: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    when (layout) {
        BrowseLayout.LIST -> {
            BrowseListItem(
                modifier = modifier,
                file = file,
                hasSelectedItems = hasSelectedItems,
                onClick = onClick,
                onLongClick = onLongClick
            )
        }

        BrowseLayout.GRID -> {
            BrowseGridItem(
                modifier = modifier,
                file = file,
                hasSelectedItems = hasSelectedItems,
                onClick = onClick,
                onLongClick = onLongClick
            )
        }
    }
}