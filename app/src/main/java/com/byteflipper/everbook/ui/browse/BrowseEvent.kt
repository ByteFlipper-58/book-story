/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

@file:OptIn(ExperimentalPermissionsApi::class)

package com.byteflipper.everbook.ui.browse

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Immutable
import androidx.compose.ui.focus.FocusRequester
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.byteflipper.everbook.domain.browse.SelectableFile
import com.byteflipper.everbook.domain.library.book.SelectableNullableBook

@Immutable
sealed class BrowseEvent {
    data class OnRefreshList(
        val loading: Boolean,
        val hideSearch: Boolean
    ) : BrowseEvent()

    data class OnSearchVisibility(
        val show: Boolean
    ) : BrowseEvent()

    data class OnRequestFocus(
        val focusRequester: FocusRequester
    ) : BrowseEvent()

    data class OnSearchQueryChange(
        val query: String
    ) : BrowseEvent()

    data object OnSearch : BrowseEvent()

    data object OnClearSelectedFiles : BrowseEvent()

    data class OnSelectFiles(
        val includedFileFormats: List<String>,
        val files: List<SelectableFile>
    ) : BrowseEvent()

    data class OnSelectFile(
        val includedFileFormats: List<String>,
        val file: SelectableFile
    ) : BrowseEvent()

    data object OnShowFilterBottomSheet : BrowseEvent()

    data object OnDismissBottomSheet : BrowseEvent()

    data class OnPermissionCheck(
        val storagePermissionState: PermissionState
    ) : BrowseEvent()

    data class OnActionPermissionDialog(
        val activity: ComponentActivity,
        val storagePermissionState: PermissionState
    ) : BrowseEvent()

    data class OnDismissPermissionDialog(
        val storagePermissionState: PermissionState
    ) : BrowseEvent()

    data object OnShowAddDialog : BrowseEvent()

    data object OnDismissAddDialog : BrowseEvent()

    data class OnSelectAddDialog(
        val book: SelectableNullableBook
    ) : BrowseEvent()

    data class OnActionAddDialog(
        val context: Context,
        val navigateToLibrary: () -> Unit
    ) : BrowseEvent()

    data object OnDismissDialog : BrowseEvent()
}