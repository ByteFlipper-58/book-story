/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.about

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.byteflipper.everbook.domain.about.Badge
import com.byteflipper.everbook.presentation.core.components.common.IconButton

@Composable
fun AboutBadgeItem(
    badge: Badge,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
        contentAlignment = Alignment.Center
    ) {
        if (badge.imageVector == null && badge.drawable != null) {
            IconButton(
                modifier = Modifier.size(22.dp),
                icon = badge.drawable,
                contentDescription = badge.contentDescription,
                disableOnClick = false,
                color = MaterialTheme.colorScheme.tertiary
            ) {
                onClick()
            }
        } else if (badge.imageVector != null && badge.drawable == null) {
            IconButton(
                modifier = Modifier.size(22.dp),
                icon = badge.imageVector,
                contentDescription = badge.contentDescription,
                disableOnClick = false,
                color = MaterialTheme.colorScheme.tertiary
            ) {
                onClick()
            }
        }
    }
}