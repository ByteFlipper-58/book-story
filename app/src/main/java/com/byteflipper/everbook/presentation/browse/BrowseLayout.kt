package com.byteflipper.everbook.presentation.browse

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import com.byteflipper.everbook.domain.browse.BrowseLayout
import com.byteflipper.everbook.domain.browse.SelectableFile

@Composable
fun BrowseLayout(
    files: List<SelectableFile>,
    hasSelectedFiles: Boolean,
    layout: BrowseLayout,
    gridSize: Int,
    autoGridSize: Boolean,
    listState: LazyListState,
    gridState: LazyGridState,
    onLongItemClick: (SelectableFile) -> Unit,
    onFavoriteItemClick: (SelectableFile) -> Unit,
    onItemClick: (SelectableFile) -> Unit
) {
    when (layout) {
        BrowseLayout.LIST -> {
            BrowseListLayout(
                files = files,
                hasSelectedFiles = hasSelectedFiles,
                listState = listState,
                onLongItemClick = onLongItemClick,
                onFavoriteItemClick = onFavoriteItemClick,
                onItemClick = onItemClick
            )
        }

        BrowseLayout.GRID -> {
            BrowseGridLayout(
                gridSize = gridSize,
                autoGridSize = autoGridSize,
                files = files,
                hasSelectedFiles = hasSelectedFiles,
                gridState = gridState,
                onLongItemClick = onLongItemClick,
                onFavoriteItemClick = onFavoriteItemClick,
                onItemClick = onItemClick
            )
        }
    }
}