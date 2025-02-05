package com.byteflipper.everbook.presentation.browse

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import com.byteflipper.everbook.domain.browse.BrowseLayout
import com.byteflipper.everbook.domain.browse.SelectableFile

@Composable
fun BrowseLayout(
    files: List<SelectableFile>,
    hasSelectedItems: Boolean,
    layout: BrowseLayout,
    gridSize: Int,
    autoGridSize: Boolean,
    listState: LazyListState,
    gridState: LazyGridState,
    onLongItemClick: (SelectableFile) -> Unit,
    onItemClick: (SelectableFile) -> Unit
) {
    when (layout) {
        BrowseLayout.LIST -> {
            BrowseListLayout(
                files = files,
                hasSelectedItems = hasSelectedItems,
                listState = listState,
                onLongItemClick = onLongItemClick,
                onItemClick = onItemClick
            )
        }

        BrowseLayout.GRID -> {
            BrowseGridLayout(
                gridSize = gridSize,
                autoGridSize = autoGridSize,
                files = files,
                hasSelectedItems = hasSelectedItems,
                gridState = gridState,
                onLongItemClick = onLongItemClick,
                onItemClick = onItemClick
            )
        }
    }
}