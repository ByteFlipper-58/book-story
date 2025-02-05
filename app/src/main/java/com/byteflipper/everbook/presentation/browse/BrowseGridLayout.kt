package com.byteflipper.everbook.presentation.browse

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
import com.byteflipper.everbook.domain.browse.BrowseLayout
import com.byteflipper.everbook.domain.browse.SelectableFile
import com.byteflipper.everbook.presentation.core.components.common.LazyVerticalGridWithScrollbar
import com.byteflipper.everbook.presentation.core.components.common.header
import com.byteflipper.everbook.presentation.core.constants.Constants
import com.byteflipper.everbook.presentation.core.constants.providePrimaryScrollbar

@Composable
fun BrowseGridLayout(
    gridSize: Int,
    autoGridSize: Boolean,
    gridState: LazyGridState,
    files: List<SelectableFile>,
    hasSelectedItems: Boolean,
    onLongItemClick: (SelectableFile) -> Unit,
    onItemClick: (SelectableFile) -> Unit,
) {
    LazyVerticalGridWithScrollbar(
        columns = if (autoGridSize) GridCells.Adaptive(170.dp)
        else GridCells.Fixed(gridSize.coerceAtLeast(1)),
        state = gridState,
        modifier = Modifier
            .fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 8.dp),
        scrollbarSettings = Constants.providePrimaryScrollbar(false)
    ) {
        header {
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(
            files,
            key = { it.path }
        ) { selectableFile ->
            BrowseItem(
                file = selectableFile,
                modifier = Modifier.animateItem(),
                layout = BrowseLayout.GRID,
                hasSelectedItems = hasSelectedItems,
                onLongClick = {
                    onLongItemClick(selectableFile)
                },
                onClick = {
                    onItemClick(selectableFile)
                }
            )
        }

        header {
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}