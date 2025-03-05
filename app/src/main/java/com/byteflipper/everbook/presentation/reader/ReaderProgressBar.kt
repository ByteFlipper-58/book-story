/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.reader

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import com.byteflipper.everbook.domain.util.HorizontalAlignment
import com.byteflipper.everbook.presentation.core.components.common.StyledText

@Composable
fun ReaderProgressBar(
    progress: String,
    progressBarPadding: Dp,
    progressBarAlignment: HorizontalAlignment,
    progressBarFontSize: TextUnit,
    fontColor: Color,
    sidePadding: Dp
) {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(
                horizontal = sidePadding,
                vertical = progressBarPadding
            ),
        contentAlignment = progressBarAlignment.alignment
    ) {
        DisableSelection {
            StyledText(
                text = progress,
                style = LocalTextStyle.current.copy(
                    color = fontColor,
                    fontSize = progressBarFontSize
                ),
                maxLines = 1
            )
        }
    }
}