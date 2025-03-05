/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.about

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import com.byteflipper.everbook.ui.about.AboutEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScaffold(
    scrollBehavior: TopAppBarScrollBehavior,
    updateLoading: Boolean,
    listState: LazyListState,
    checkForUpdate: (AboutEvent.OnCheckForUpdate) -> Unit,
    navigateToBrowserPage: (AboutEvent.OnNavigateToBrowserPage) -> Unit,
    navigateToLicenses: () -> Unit,
    navigateToCredits: () -> Unit,
    navigateBack: () -> Unit
) {
    Scaffold(
        Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .windowInsetsPadding(WindowInsets.navigationBars),
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            AboutTopBar(
                scrollBehavior = scrollBehavior,
                navigateBack = navigateBack
            )
        }
    ) { paddingValues ->
        AboutLayout(
            updateLoading = updateLoading,
            paddingValues = paddingValues,
            listState = listState,
            checkForUpdate = checkForUpdate,
            navigateToBrowserPage = navigateToBrowserPage,
            navigateToLicenses = navigateToLicenses,
            navigateToCredits = navigateToCredits
        )
    }
}