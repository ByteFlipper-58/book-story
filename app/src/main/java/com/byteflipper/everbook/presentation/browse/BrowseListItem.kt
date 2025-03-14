/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.browse

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.byteflipper.everbook.domain.browse.SelectableFile
import com.byteflipper.everbook.presentation.core.components.common.CircularCheckbox
import com.byteflipper.everbook.ui.theme.FadeTransitionPreservingSpace

@Composable
fun BrowseListItem(
    modifier: Modifier,
    file: SelectableFile,
    hasSelectedItems: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (file.selected) MaterialTheme.colorScheme.secondaryContainer
                else Color.Transparent,
                RoundedCornerShape(10.dp)
            )
            .combinedClickable(
                onLongClick = {
                    onLongClick()
                },
                onClick = {
                    onClick()
                }
            )
            .padding(horizontal = 8.dp, vertical = 7.dp)
    ) {
        BrowseListFileItem(file = file)

        FadeTransitionPreservingSpace(visible = hasSelectedItems) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Spacer(modifier = Modifier.width(16.dp))

                CircularCheckbox(
                    selected = file.selected,
                    containerColor = MaterialTheme.colorScheme.surface,
                    size = 18.dp
                )

                Spacer(modifier = Modifier.width(8.dp))
            }
        }
    }
}