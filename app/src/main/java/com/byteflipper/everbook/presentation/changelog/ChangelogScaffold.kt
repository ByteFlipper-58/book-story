/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.changelog

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
import com.byteflipper.everbook.ui.changelog.ChangelogEvent
import com.byteflipper.everbook.ui.changelog.ChangelogState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangelogScaffold(
    state: ChangelogState,
    versionName: String?,
    listState: LazyListState,
    scrollBehavior: TopAppBarScrollBehavior,
    onEvent: (ChangelogEvent) -> Unit,
    navigateBack: () -> Unit
) {
    Scaffold(
        Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .windowInsetsPadding(WindowInsets.navigationBars),
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            ChangelogTopBar(
                versionName = versionName,
                scrollBehavior = scrollBehavior,
                navigateBack = navigateBack
            )
        }
    ) { paddingValues ->
        ChangelogLayout(
            state = state,
            paddingValues = paddingValues,
            listState = listState,
            onEvent = onEvent,
            navigateBack = navigateBack
        )
    }
}
