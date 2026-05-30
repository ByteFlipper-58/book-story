/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.changelog

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import com.byteflipper.everbook.ui.changelog.ChangelogEvent
import com.byteflipper.everbook.ui.changelog.ChangelogState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangelogContent(
    state: ChangelogState,
    listState: LazyListState,
    scrollBehavior: TopAppBarScrollBehavior,
    onEvent: (ChangelogEvent) -> Unit,
    navigateBack: () -> Unit
) {
    ChangelogScaffold(
        state = state,
        versionName = state.selectedRelease?.versionName,
        listState = listState,
        scrollBehavior = scrollBehavior,
        onEvent = onEvent,
        navigateBack = navigateBack
    )
}
