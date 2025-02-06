package com.byteflipper.everbook.presentation.browse

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.byteflipper.everbook.domain.browse.BrowseLayout
import com.byteflipper.everbook.domain.browse.SelectableFile
import com.byteflipper.everbook.presentation.core.components.common.LazyColumnWithScrollbar
import com.byteflipper.everbook.presentation.core.constants.Constants
import com.byteflipper.everbook.presentation.core.constants.providePrimaryScrollbar

@Composable
fun BrowseListLayout(
    files: List<SelectableFile>,
    hasSelectedItems: Boolean,
    listState: LazyListState,
    onLongItemClick: (SelectableFile) -> Unit,
    onItemClick: (SelectableFile) -> Unit,
) {
    LazyColumnWithScrollbar(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        scrollbarSettings = Constants.providePrimaryScrollbar(false)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(
            files,
            key = { it.path }
        ) { selectableFile ->
            BrowseItem(
                file = selectableFile,
                layout = BrowseLayout.LIST,
                modifier = Modifier.animateItem(),
                hasSelectedItems = hasSelectedItems,
                onLongClick = {
                    onLongItemClick(selectableFile)
                },
                onClick = {
                    onItemClick(selectableFile)
                }
            )
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}