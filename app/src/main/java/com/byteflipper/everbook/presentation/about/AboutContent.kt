/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.about

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import com.byteflipper.everbook.data.remote.dto.LatestReleaseInfo
import com.byteflipper.everbook.domain.util.Dialog
import com.byteflipper.everbook.ui.about.AboutEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutContent(
    scrollBehavior: TopAppBarScrollBehavior,
    dialog: Dialog? = null,
    updateLoading: Boolean,
    updateInfo: LatestReleaseInfo?,
    listState: LazyListState,
    checkForUpdate: (AboutEvent.OnCheckForUpdate) -> Unit,
    navigateToBrowserPage: (AboutEvent.OnNavigateToBrowserPage) -> Unit,
    dismissDialog: (AboutEvent.OnDismissDialog) -> Unit,
    navigateToLicenses: () -> Unit,
    navigateToCredits: () -> Unit,
    navigateBack: () -> Unit
) {
    AboutDialog(
        dialog = dialog,
        updateInfo = updateInfo,
        navigateToBrowserPage = navigateToBrowserPage,
        dismissDialog = dismissDialog
    )

    AboutScaffold(
        scrollBehavior = scrollBehavior,
        updateLoading = updateLoading,
        listState = listState,
        checkForUpdate = checkForUpdate,
        navigateToBrowserPage = navigateToBrowserPage,
        navigateToLicenses = navigateToLicenses,
        navigateToCredits = navigateToCredits,
        navigateBack = navigateBack
    )
}