package com.byteflipper.everbook.presentation.browse

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.byteflipper.everbook.domain.browse.BrowseLayout
import com.byteflipper.everbook.domain.browse.SelectableFile

@Composable
fun BrowseItem(
    layout: BrowseLayout,
    file: SelectableFile,
    hasSelectedFiles: Boolean,
    modifier: Modifier,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onLongClick: () -> Unit
) {
    when (layout) {
        BrowseLayout.LIST -> {
            BrowseListItem(
                modifier = modifier,
                file = file,
                hasSelectedFiles = hasSelectedFiles,
                onClick = onClick,
                onFavoriteClick = onFavoriteClick,
                onLongClick = onLongClick
            )
        }

        BrowseLayout.GRID -> {
            BrowseGridItem(
                modifier = modifier,
                file = file,
                hasSelectedFiles = hasSelectedFiles,
                onClick = onClick,
                onFavoriteClick = onFavoriteClick,
                onLongClick = onLongClick
            )
        }
    }
}