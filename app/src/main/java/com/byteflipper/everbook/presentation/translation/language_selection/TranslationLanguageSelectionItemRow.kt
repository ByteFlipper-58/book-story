/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.translation.language_selection
import com.byteflipper.everbook.R
import androidx.compose.ui.res.painterResource
import androidx.annotation.DrawableRes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.byteflipper.everbook.presentation.core.components.common.StyledText

@Composable
internal fun TranslationLanguageSelectionItemRow(
    item: TranslationLanguageSelectionItem,
    onClick: () -> Unit
) {
    val enabled = item.supported && !item.busy
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TranslationLanguageStatusIcon(item = item)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                StyledText(
                    text = item.title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                item.subtitle?.let { subtitle ->
                    StyledText(
                        text = subtitle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
            TranslationLanguageActionIcon(item = item)
        }
    }
    HorizontalDivider(modifier = Modifier.padding(start = 58.dp, end = 18.dp))
}

@Composable
private fun TranslationLanguageStatusIcon(item: TranslationLanguageSelectionItem) {
    if (item.busy) {
        CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            strokeWidth = 2.dp
        )
        return
    }

    TranslationLanguageIcon(
        icon = when {
            !item.supported -> R.drawable.ic_error_rounded_24px
            item.selected -> R.drawable.ic_check_circle_rounded_24px
            item.downloaded == false -> R.drawable.ic_cloud_download_rounded_24px
            else -> R.drawable.ic_language_rounded_24px
        },
        tint = when {
            !item.supported -> MaterialTheme.colorScheme.error
            item.selected -> MaterialTheme.colorScheme.primary
            item.downloaded == false -> MaterialTheme.colorScheme.tertiary
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        }
    )
}

@Composable
private fun TranslationLanguageActionIcon(item: TranslationLanguageSelectionItem) {
    if (!item.supported || item.busy) return
    when {
        item.selected -> TranslationLanguageIcon(
            icon = R.drawable.ic_check_rounded_24px,
            tint = MaterialTheme.colorScheme.primary
        )

        item.downloaded == false -> TranslationLanguageIcon(
            icon = R.drawable.ic_download_for_offline_rounded_24px,
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun TranslationLanguageIcon(
    @androidx.annotation.DrawableRes icon: Int,
    tint: androidx.compose.ui.graphics.Color
) {
    Icon(
        painter = androidx.compose.ui.res.painterResource(id = icon),
        contentDescription = null,
        modifier = Modifier.size(22.dp),
        tint = tint
    )
}
