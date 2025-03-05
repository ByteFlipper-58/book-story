/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.library

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.byteflipper.everbook.presentation.core.components.common.LazyVerticalGridWithScrollbar
import com.byteflipper.everbook.presentation.core.constants.Constants
import com.byteflipper.everbook.presentation.core.constants.providePrimaryScrollbar

@Composable
fun LibraryLayout(
    items: LazyGridScope.() -> Unit
) {
    LazyVerticalGridWithScrollbar(
        columns = GridCells.Adaptive(120.dp),
        modifier = Modifier.fillMaxSize(),
        scrollbarSettings = Constants.providePrimaryScrollbar(false),
        contentPadding = PaddingValues(8.dp)
    ) {
        items()
    }
}