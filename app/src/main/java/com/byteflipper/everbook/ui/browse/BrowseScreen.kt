/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.ui.browse

import android.Manifest
import android.os.Parcelable
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.focus.FocusRequester
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import com.byteflipper.everbook.domain.browse.BrowseLayout
import com.byteflipper.everbook.domain.navigator.Screen
import com.byteflipper.everbook.presentation.browse.BrowseContent
import com.byteflipper.everbook.presentation.navigator.LocalNavigator
import com.byteflipper.everbook.ui.help.HelpScreen
import com.byteflipper.everbook.ui.library.LibraryScreen
import com.byteflipper.everbook.ui.main.MainModel

@Parcelize
object BrowseScreen : Screen, Parcelable {

    @IgnoredOnParcel
    const val PERMISSION_DIALOG = "permission_dialog"

    @IgnoredOnParcel
    const val ADD_DIALOG = "add_dialog"

    @IgnoredOnParcel
    const val FILTER_BOTTOM_SHEET = "filter_bottom_sheet"

    @IgnoredOnParcel
    val refreshListChannel: Channel<Unit> = Channel(Channel.CONFLATED)

    @IgnoredOnParcel
    val resetScrollPositionCompositionChannel: Channel<Unit> = Channel(Channel.CONFLATED)

    @IgnoredOnParcel
    private var initialListIndex = 0

    @IgnoredOnParcel
    private var initialListOffset = 0

    @IgnoredOnParcel
    private var initialGridIndex = 0

    @IgnoredOnParcel
    private var initialGridOffset = 0

    @OptIn(ExperimentalMaterialApi::class, ExperimentalPermissionsApi::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val screenModel = hiltViewModel<BrowseModel>()
        val mainModel = hiltViewModel<MainModel>()

        val state = screenModel.state.collectAsStateWithLifecycle()
        val mainState = mainModel.state.collectAsStateWithLifecycle()

        val listState = rememberLazyListState(initialListIndex, initialListOffset)
        val gridState = rememberLazyGridState(initialGridIndex, initialGridOffset)

        val storagePermissionState = rememberPermissionState(
            permission = Manifest.permission.READ_EXTERNAL_STORAGE
        )
        val focusRequester = remember { FocusRequester() }
        val refreshState = rememberPullRefreshState(
            refreshing = state.value.isRefreshing,
            onRefresh = {
                screenModel.onEvent(
                    BrowseEvent.OnRefreshList(
                        loading = false,
                        hideSearch = true
                    )
                )
            }
        )

        val files = remember {
            derivedStateOf {
                screenModel.filterList(
                    files = state.value.files,
                    sortOrderDescending = mainState.value.browseSortOrderDescending,
                    includedFilterItems = mainState.value.browseIncludedFilterItems,
                    sortOrder = mainState.value.browseSortOrder
                )
            }
        }

        LaunchedEffect(Unit) {
            screenModel.onEvent(
                BrowseEvent.OnPermissionCheck(
                    storagePermissionState = storagePermissionState
                )
            )
        }

        LaunchedEffect(Unit) {
            resetScrollPositionCompositionChannel.receiveAsFlow().collectLatest {
                initialListIndex = 0
                initialListOffset = 0
                initialGridIndex = 0
                initialGridOffset = 0
            }
        }

        DisposableEffect(Unit) {
            onDispose {
                screenModel.resetScreen()

                initialListIndex = 0
                initialListOffset = 0
                initialGridIndex = 0
                initialGridOffset = 0

                when (mainState.value.browseLayout) {
                    BrowseLayout.LIST -> {
                        initialListIndex = listState.firstVisibleItemIndex
                        initialListOffset = listState.firstVisibleItemScrollOffset
                    }

                    BrowseLayout.GRID -> {
                        initialGridIndex = gridState.firstVisibleItemIndex
                        initialGridOffset = gridState.firstVisibleItemScrollOffset
                    }
                }
            }
        }

        BrowseContent(
            files = files.value,
            selectedBooksAddDialog = state.value.selectedBooksAddDialog,
            refreshState = refreshState,
            storagePermissionState = storagePermissionState,
            loadingAddDialog = state.value.loadingAddDialog,
            dialog = state.value.dialog,
            bottomSheet = state.value.bottomSheet,
            listState = listState,
            gridState = gridState,
            layout = mainState.value.browseLayout,
            gridSize = mainState.value.browseGridSize,
            autoGridSize = mainState.value.browseAutoGridSize,
            includedFilterItems = mainState.value.browseIncludedFilterItems,
            pinnedPaths = mainState.value.browsePinnedPaths,
            canScrollBackList = listState.canScrollBackward,
            canScrollBackGrid = gridState.canScrollBackward,
            hasSelectedItems = state.value.hasSelectedItems,
            selectedItemsCount = state.value.selectedItemsCount,
            isRefreshing = state.value.isRefreshing,
            isLoading = state.value.isLoading,
            isError = state.value.isError,
            dialogHidden = state.value.dialog == null,
            filesEmpty = files.value.isEmpty(),
            showSearch = state.value.showSearch,
            searchQuery = state.value.searchQuery,
            focusRequester = focusRequester,
            searchVisibility = screenModel::onEvent,
            searchQueryChange = screenModel::onEvent,
            search = screenModel::onEvent,
            requestFocus = screenModel::onEvent,
            clearSelectedFiles = screenModel::onEvent,
            selectFiles = screenModel::onEvent,
            selectFile = screenModel::onEvent,
            permissionCheck = screenModel::onEvent,
            showFilterBottomSheet = screenModel::onEvent,
            dismissBottomSheet = screenModel::onEvent,
            actionPermissionDialog = screenModel::onEvent,
            dismissPermissionDialog = screenModel::onEvent,
            showAddDialog = screenModel::onEvent,
            dismissAddDialog = screenModel::onEvent,
            selectAddDialog = screenModel::onEvent,
            actionAddDialog = screenModel::onEvent,
            changePinnedPaths = mainModel::onEvent,
            navigateToLibrary = {
                navigator.push(LibraryScreen, saveInBackStack = false)
            },
            navigateToHelp = {
                navigator.push(HelpScreen(fromStart = false))
            },
        )
    }
}