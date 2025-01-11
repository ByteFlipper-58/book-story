package com.byteflipper.everbook.presentation.browse

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshState
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.byteflipper.everbook.R
import com.byteflipper.everbook.domain.browse.BrowseFilesStructure
import com.byteflipper.everbook.domain.browse.BrowseLayout
import com.byteflipper.everbook.domain.browse.SelectableFile
import com.byteflipper.everbook.presentation.core.components.common.AnimatedVisibility
import com.byteflipper.everbook.presentation.core.util.LocalActivity
import com.byteflipper.everbook.presentation.core.util.showToast
import com.byteflipper.everbook.ui.browse.BrowseEvent
import com.byteflipper.everbook.ui.theme.Transitions
import java.io.File

@OptIn(ExperimentalMaterialApi::class, ExperimentalPermissionsApi::class)
@Composable
fun BrowseScaffold(
    files: List<SelectableFile>,
    refreshState: PullRefreshState,
    storagePermissionState: PermissionState,
    listState: LazyListState,
    gridState: LazyGridState,
    layout: BrowseLayout,
    filesStructure: BrowseFilesStructure,
    gridSize: Int,
    autoGridSize: Boolean,
    includedFilterItems: List<String>,
    canScrollBackList: Boolean,
    canScrollBackGrid: Boolean,
    selectedDirectory: File,
    inNestedDirectory: Boolean,
    hasSelectedItems: Boolean,
    selectedItemsCount: Int,
    isRefreshing: Boolean,
    isLoading: Boolean,
    isError: Boolean,
    dialogHidden: Boolean,
    filesEmpty: Boolean,
    showSearch: Boolean,
    searchQuery: String,
    hasSearched: Boolean,
    focusRequester: FocusRequester,
    searchVisibility: (BrowseEvent.OnSearchVisibility) -> Unit,
    searchQueryChange: (BrowseEvent.OnSearchQueryChange) -> Unit,
    search: (BrowseEvent.OnSearch) -> Unit,
    requestFocus: (BrowseEvent.OnRequestFocus) -> Unit,
    goBackDirectory: (BrowseEvent.OnGoBackDirectory) -> Unit,
    clearSelectedFiles: (BrowseEvent.OnClearSelectedFiles) -> Unit,
    selectFiles: (BrowseEvent.OnSelectFiles) -> Unit,
    selectFile: (BrowseEvent.OnSelectFile) -> Unit,
    permissionCheck: (BrowseEvent.OnPermissionCheck) -> Unit,
    updateFavoriteDirectory: (BrowseEvent.OnUpdateFavoriteDirectory) -> Unit,
    changeDirectory: (BrowseEvent.OnChangeDirectory) -> Unit,
    showFilterBottomSheet: (BrowseEvent.OnShowFilterBottomSheet) -> Unit,
    showAddDialog: (BrowseEvent.OnShowAddDialog) -> Unit,
    navigateToHelp: () -> Unit,
) {
    val context = LocalActivity.current
    Scaffold(
        Modifier
            .fillMaxSize()
            .imePadding()
            .pullRefresh(refreshState)
            .windowInsetsPadding(WindowInsets.navigationBars),
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            BrowseTopBar(
                files = files,
                layout = layout,
                filesStructure = filesStructure,
                includedFilterItems = includedFilterItems,
                canScrollBackList = canScrollBackList,
                canScrollBackGrid = canScrollBackGrid,
                selectedDirectory = selectedDirectory,
                inNestedDirectory = inNestedDirectory,
                hasSelectedItems = hasSelectedItems,
                selectedItemsCount = selectedItemsCount,
                showSearch = showSearch,
                searchQuery = searchQuery,
                hasSearched = hasSearched,
                isError = isError,
                focusRequester = focusRequester,
                searchVisibility = searchVisibility,
                searchQueryChange = searchQueryChange,
                search = search,
                requestFocus = requestFocus,
                goBackDirectory = goBackDirectory,
                clearSelectedFiles = clearSelectedFiles,
                selectFiles = selectFiles,
                changeDirectory = changeDirectory,
                showFilterBottomSheet = showFilterBottomSheet,
                showAddDialog = showAddDialog
            )
        }
    ) { paddingValues ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
        ) {
            AnimatedVisibility(
                visible = !isLoading,
                enter = Transitions.DefaultTransitionIn,
                exit = Transitions.NoExitAnimation
            ) {
                BrowseLayout(
                    files = files,
                    layout = layout,
                    hasSelectedFiles = files.any { it.isSelected },
                    gridSize = gridSize,
                    autoGridSize = autoGridSize,
                    listState = listState,
                    gridState = gridState,
                    onLongItemClick = { file ->
                        when (file.isDirectory) {
                            false -> {
                                context.getString(
                                    R.string.file_path_query,
                                    file.fileOrDirectory.path
                                ).showToast(context = context)
                            }

                            true -> {
                                selectFile(
                                    BrowseEvent.OnSelectFile(
                                        includedFileFormats = includedFilterItems,
                                        file = file
                                    )
                                )
                            }
                        }
                    },
                    onFavoriteItemClick = { file ->
                        updateFavoriteDirectory(
                            BrowseEvent.OnUpdateFavoriteDirectory(
                                file.fileOrDirectory.path
                            )
                        )
                    },
                    onItemClick = { file ->
                        when (file.isDirectory) {
                            false -> {
                                selectFile(
                                    BrowseEvent.OnSelectFile(
                                        includedFileFormats = includedFilterItems,
                                        file = file
                                    )
                                )
                            }
                            true -> {
                                if (!hasSelectedItems) {
                                    changeDirectory(
                                        BrowseEvent.OnChangeDirectory(
                                            file.fileOrDirectory,
                                            savePreviousDirectory = true
                                        )
                                    )
                                } else {
                                    selectFile(
                                        BrowseEvent.OnSelectFile(
                                            includedFileFormats = includedFilterItems,
                                            file = file
                                        )
                                    )
                                }
                            }
                        }
                    }
                )
            }

            BrowseEmptyPlaceholder(
                filesEmpty = filesEmpty,
                dialogHidden = dialogHidden,
                isError = isError,
                isLoading = isLoading,
                isRefreshing = isRefreshing,
                storagePermissionState = storagePermissionState,
                permissionCheck = permissionCheck,
                navigateToHelp = navigateToHelp
            )

            BrowseRefreshIndicator(
                isRefreshing = isRefreshing,
                refreshState = refreshState
            )

            if (isLoading) {
                BrowseLoadingPlaceholder(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}