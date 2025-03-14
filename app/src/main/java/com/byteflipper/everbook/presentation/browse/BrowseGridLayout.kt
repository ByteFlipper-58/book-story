/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.browse

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.byteflipper.everbook.domain.browse.file.GroupedFiles
import com.byteflipper.everbook.domain.browse.SelectableFile
import com.byteflipper.everbook.presentation.core.components.common.LazyVerticalGridWithScrollbar
import com.byteflipper.everbook.presentation.core.components.common.header
import com.byteflipper.everbook.presentation.core.constants.Constants
import com.byteflipper.everbook.presentation.core.constants.providePrimaryScrollbar

@Composable
fun BrowseGridLayout(
    groupedFiles: List<GroupedFiles>,
    gridSize: Int,
    autoGridSize: Boolean,
    gridState: LazyGridState,
    headerContent: @Composable (header: String, pinned: Boolean) -> Unit,
    itemContent: @Composable (file: SelectableFile, files: List<SelectableFile>) -> Unit
) {
    LazyVerticalGridWithScrollbar(
        columns = if (autoGridSize) GridCells.Adaptive(170.dp)
        else GridCells.Fixed(gridSize.coerceAtLeast(1)),
        state = gridState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 8.dp),
        scrollbarSettings = Constants.providePrimaryScrollbar(false)
    ) {
        groupedFiles.forEach { group ->
            stickyHeader {
                Box(Modifier.animateItem()) {
                    headerContent(group.header, group.pinned)
                }
            }

            items(
                group.files,
                key = { it.data.path }
            ) { selectableFile ->
                Box(Modifier.animateItem()) {
                    itemContent(selectableFile, group.files)
                }
            }
        }

        header {
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}