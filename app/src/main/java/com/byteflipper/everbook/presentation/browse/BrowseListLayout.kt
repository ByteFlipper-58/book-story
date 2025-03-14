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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.byteflipper.everbook.domain.browse.file.GroupedFiles
import com.byteflipper.everbook.domain.browse.SelectableFile
import com.byteflipper.everbook.presentation.core.components.common.LazyColumnWithScrollbar
import com.byteflipper.everbook.presentation.core.constants.providePrimaryScrollbar

@Composable
fun BrowseListLayout(
    groupedFiles: List<GroupedFiles>,
    listState: LazyListState,
    headerContent: @Composable (header: String, pinned: Boolean) -> Unit,
    itemContent: @Composable (file: SelectableFile, files: List<SelectableFile>) -> Unit
) {
    LazyColumnWithScrollbar(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 8.dp),
        scrollbarSettings = providePrimaryScrollbar(false)
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

        item {
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}