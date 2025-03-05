/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.history

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.PullRefreshState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun BoxScope.HistoryRefreshIndicator(
    isRefreshing: Boolean,
    refreshState: PullRefreshState
) {
    PullRefreshIndicator(
        isRefreshing,
        refreshState,
        Modifier.align(Alignment.TopCenter),
        backgroundColor = MaterialTheme.colorScheme.inverseSurface,
        contentColor = MaterialTheme.colorScheme.inverseOnSurface
    )
}