/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ReaderBottomBarSliderIndicator(
    progress: Float,
    bookProgress: Float
) {
    val isOnActivePart = progress < bookProgress
    val markerColor = if (isOnActivePart) {
        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
    } else {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
    }

    Row(Modifier.fillMaxWidth()) {
        Spacer(
            modifier = Modifier.fillMaxWidth(progress)
        )
        Box(
            Modifier
                .width(3.dp)
                .height(36.dp)
                .background(
                    color = markerColor,
                    shape = RoundedCornerShape(1.5.dp)
                )
        )
    }
}