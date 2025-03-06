/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val ColorScheme.readerBarsColor: Color
    get() = surfaceContainer.copy(0.9f)

@Composable
fun ColorScheme.dynamicListItemColor(
    index: Int
): Color {
    return when (index % 3) {
        0 -> surfaceContainerLow
        1 -> surfaceContainerHigh
        else -> surfaceContainer
    }
}