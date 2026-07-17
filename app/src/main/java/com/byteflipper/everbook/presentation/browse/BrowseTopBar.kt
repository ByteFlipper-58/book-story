/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.browse

import androidx.compose.animation.animateColorAsState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.byteflipper.everbook.R
import com.byteflipper.everbook.domain.browse.display.BrowseLayout
import com.byteflipper.everbook.domain.browse.SelectableFile
import com.byteflipper.everbook.presentation.core.components.common.IconButton
import com.byteflipper.everbook.presentation.core.components.common.SearchTextField
import com.byteflipper.everbook.presentation.core.components.common.StyledText
import com.byteflipper.everbook.presentation.core.components.top_bar.TopAppBar
import com.byteflipper.everbook.presentation.core.components.top_bar.TopAppBarData
import com.byteflipper.everbook.presentation.navigator.NavigatorIconButton
import com.byteflipper.everbook.ui.browse.BrowseEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowseTopBar(
    files: List<SelectableFile>,
    layout: BrowseLayout,
    includedFilterItems: List<String>,
    canScrollBackList: Boolean,
    canScrollBackGrid: Boolean,
    hasSelectedItems: Boolean,
    selectedItemsCount: Int,
    showSearch: Boolean,
    searchQuery: String,
    focusRequester: FocusRequester,
    searchVisibility: (BrowseEvent.OnSearchVisibility) -> Unit,
    searchQueryChange: (BrowseEvent.OnSearchQueryChange) -> Unit,
    search: (BrowseEvent.OnSearch) -> Unit,
    requestFocus: (BrowseEvent.OnRequestFocus) -> Unit,
    clearSelectedFiles: (BrowseEvent.OnClearSelectedFiles) -> Unit,
    selectFiles: (BrowseEvent.OnSelectFiles) -> Unit,
    showFilterBottomSheet: (BrowseEvent.OnShowFilterBottomSheet) -> Unit,
    showAddDialog: (BrowseEvent.OnShowAddDialog) -> Unit
) {
    val isScrolled = remember(layout, canScrollBackList, canScrollBackGrid) {
        derivedStateOf {
            when (layout) {
                BrowseLayout.GRID -> canScrollBackGrid
                BrowseLayout.LIST -> canScrollBackList
            }
        }
    }

    val animatedFilterIconColor = animateColorAsState(
        if (includedFilterItems.isNotEmpty()) {
            MaterialTheme.colorScheme.primary
        } else LocalContentColor.current
    )

    TopAppBar(
        scrollBehavior = null,
        isTopBarScrolled = isScrolled.value || hasSelectedItems,

        shownTopBar = when {
            hasSelectedItems -> 2
            showSearch -> 1
            else -> 0
        },
        topBars = listOf(
            TopAppBarData(
                contentID = 0,
                contentNavigationIcon = {},
                contentTitle = {
                    StyledText(
                        text = stringResource(id = R.string.browse_screen),
                        maxLines = 1
                    )
                },
                contentActions = {
                    IconButton(
                        icon = R.drawable.ic_search_rounded_24px,
                        contentDescription = R.string.search_content_desc,
                        disableOnClick = true
                    ) {
                        searchVisibility(BrowseEvent.OnSearchVisibility(true))
                    }
                    IconButton(
                        icon = R.drawable.ic_filter_list_rounded_24px,
                        contentDescription = R.string.filter_content_desc,
                        disableOnClick = false,
                        color = animatedFilterIconColor.value
                    ) {
                        showFilterBottomSheet(BrowseEvent.OnShowFilterBottomSheet)
                    }
                    NavigatorIconButton()
                }
            ),

            TopAppBarData(
                contentID = 1,
                contentNavigationIcon = {
                    IconButton(
                        icon = R.drawable.ic_arrow_back_rounded_24px,
                        contentDescription = R.string.exit_search_content_desc,
                        disableOnClick = true
                    ) {
                        searchVisibility(BrowseEvent.OnSearchVisibility(false))
                    }
                },
                contentTitle = {
                    SearchTextField(
                        modifier = Modifier
                            .focusRequester(focusRequester)
                            .onGloballyPositioned {
                                requestFocus(BrowseEvent.OnRequestFocus(focusRequester))
                            },
                        initialQuery = searchQuery,
                        onQueryChange = {
                            searchQueryChange(BrowseEvent.OnSearchQueryChange(it))
                        },
                        onSearch = {
                            search(BrowseEvent.OnSearch)
                        }
                    )
                },
                contentActions = {
                    NavigatorIconButton()
                }
            ),

            TopAppBarData(
                contentID = 2,
                contentNavigationIcon = {
                    IconButton(
                        icon = R.drawable.ic_close_rounded_24px,
                        contentDescription = R.string.clear_selected_items_content_desc,
                        disableOnClick = true
                    ) {
                        clearSelectedFiles(BrowseEvent.OnClearSelectedFiles)
                    }
                },
                contentTitle = {
                    StyledText(
                        text = pluralStringResource(
                            id = R.plurals.selected_items_count_query,
                            count = selectedItemsCount.coerceAtLeast(1),
                            selectedItemsCount.coerceAtLeast(1)
                        ),
                        maxLines = 1
                    )
                },
                contentActions = {
                    IconButton(
                        icon = R.drawable.ic_select_all_rounded_24px,
                        contentDescription = R.string.select_all_files_content_desc,
                        disableOnClick = false,
                    ) {
                        selectFiles(
                            BrowseEvent.OnSelectFiles(
                                includedFileFormats = includedFilterItems,
                                files = files
                            )
                        )
                    }
                    IconButton(
                        icon = R.drawable.ic_check_rounded_24px,
                        contentDescription = R.string.add_files_content_desc,
                        disableOnClick = false
                    ) {
                        showAddDialog(BrowseEvent.OnShowAddDialog)
                    }
                }
            )
        )
    )
}
