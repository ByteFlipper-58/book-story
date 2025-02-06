package com.byteflipper.everbook.presentation.browse

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.focus.FocusRequester
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.byteflipper.everbook.domain.browse.BrowseLayout
import com.byteflipper.everbook.domain.browse.SelectableFile
import com.byteflipper.everbook.domain.library.book.SelectableNullableBook
import com.byteflipper.everbook.domain.util.BottomSheet
import com.byteflipper.everbook.domain.util.Dialog
import com.byteflipper.everbook.ui.browse.BrowseEvent
import com.byteflipper.everbook.ui.main.MainEvent

@OptIn(ExperimentalMaterialApi::class, ExperimentalPermissionsApi::class)
@Composable
fun BrowseContent(
    files: List<SelectableFile>,
    selectedBooksAddDialog: List<SelectableNullableBook>,
    refreshState: PullRefreshState,
    storagePermissionState: PermissionState,
    loadingAddDialog: Boolean,
    dialog: Dialog?,
    bottomSheet: BottomSheet?,
    listState: LazyListState,
    gridState: LazyGridState,
    layout: BrowseLayout,
    gridSize: Int,
    autoGridSize: Boolean,
    includedFilterItems: List<String>,
    pinnedPaths: List<String>,
    canScrollBackList: Boolean,
    canScrollBackGrid: Boolean,
    hasSelectedItems: Boolean,
    selectedItemsCount: Int,
    isRefreshing: Boolean,
    isLoading: Boolean,
    isError: Boolean,
    dialogHidden: Boolean,
    filesEmpty: Boolean,
    showSearch: Boolean,
    searchQuery: String,
    focusRequester: FocusRequester,
    searchVisibility: (BrowseEvent.OnSearchVisibility) -> Unit,
    searchQueryChange: (BrowseEvent.OnSearchQueryChange) -> Unit,
    search: (BrowseEvent.OnSearch) -> Unit,
    requestFocus: (BrowseEvent.OnRequestFocus) -> Unit,
    clearSelectedFiles: (BrowseEvent.OnClearSelectedFiles) -> Unit,
    selectFiles: (BrowseEvent.OnSelectFiles) -> Unit,
    selectFile: (BrowseEvent.OnSelectFile) -> Unit,
    permissionCheck: (BrowseEvent.OnPermissionCheck) -> Unit,
    dismissBottomSheet: (BrowseEvent.OnDismissBottomSheet) -> Unit,
    showFilterBottomSheet: (BrowseEvent.OnShowFilterBottomSheet) -> Unit,
    actionPermissionDialog: (BrowseEvent.OnActionPermissionDialog) -> Unit,
    dismissPermissionDialog: (BrowseEvent.OnDismissPermissionDialog) -> Unit,
    showAddDialog: (BrowseEvent.OnShowAddDialog) -> Unit,
    dismissAddDialog: (BrowseEvent.OnDismissAddDialog) -> Unit,
    actionAddDialog: (BrowseEvent.OnActionAddDialog) -> Unit,
    selectAddDialog: (BrowseEvent.OnSelectAddDialog) -> Unit,
    changePinnedPaths: (MainEvent.OnChangeBrowsePinnedPaths) -> Unit,
    navigateToLibrary: () -> Unit,
    navigateToHelp: () -> Unit,
) {
    BrowseDialog(
        dialog = dialog,
        storagePermissionState = storagePermissionState,
        loadingAddDialog = loadingAddDialog,
        actionPermissionDialog = actionPermissionDialog,
        dismissPermissionDialog = dismissPermissionDialog,
        actionAddDialog = actionAddDialog,
        dismissAddDialog = dismissAddDialog,
        selectedBooksAddDialog = selectedBooksAddDialog,
        selectAddDialog = selectAddDialog,
        navigateToLibrary = navigateToLibrary
    )

    BrowseBottomSheet(
        bottomSheet = bottomSheet,
        dismissBottomSheet = dismissBottomSheet
    )

    BrowseScaffold(
        files = files,
        refreshState = refreshState,
        listState = listState,
        gridState = gridState,
        layout = layout,
        gridSize = gridSize,
        autoGridSize = autoGridSize,
        includedFilterItems = includedFilterItems,
        pinnedPaths = pinnedPaths,
        canScrollBackList = canScrollBackList,
        canScrollBackGrid = canScrollBackGrid,
        hasSelectedItems = hasSelectedItems,
        selectedItemsCount = selectedItemsCount,
        isRefreshing = isRefreshing,
        dialogHidden = dialogHidden,
        showSearch = showSearch,
        searchQuery = searchQuery,
        focusRequester = focusRequester,
        searchVisibility = searchVisibility,
        searchQueryChange = searchQueryChange,
        search = search,
        requestFocus = requestFocus,
        clearSelectedFiles = clearSelectedFiles,
        selectFiles = selectFiles,
        storagePermissionState = storagePermissionState,
        isLoading = isLoading,
        isError = isError,
        filesEmpty = filesEmpty,
        permissionCheck = permissionCheck,
        selectFile = selectFile,
        showFilterBottomSheet = showFilterBottomSheet,
        showAddDialog = showAddDialog,
        changePinnedPaths = changePinnedPaths,
        navigateToHelp = navigateToHelp
    )

    BrowseBackHandler(
        hasSelectedItems = hasSelectedItems,
        showSearch = showSearch,
        searchVisibility = searchVisibility,
        clearSelectedFiles = clearSelectedFiles,
        navigateToLibrary = navigateToLibrary
    )
}