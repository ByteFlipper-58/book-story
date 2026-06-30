/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.statistics

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.byteflipper.everbook.R
import com.byteflipper.everbook.domain.statistics.ActivityBucketScale
import com.byteflipper.everbook.domain.statistics.DayBucket
import com.byteflipper.everbook.presentation.core.components.common.StyledText

@Composable
internal fun ActivitySection(
    buckets: List<DayBucket>,
    navigateToActivity: () -> Unit
) {
    var selectedIndex by remember(buckets) { mutableStateOf<Int?>(null) }
    val selected = selectedIndex?.let { buckets.getOrNull(it) }
    val maxMs = remember(buckets) { buckets.maxOfOrNull { it.timeMs } ?: 0L }
    val totalMs = remember(buckets) { buckets.sumOf { it.timeMs } }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = navigateToActivity)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Outlined.Insights,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(20.dp)
                    .padding(top = 2.dp)
            )
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                StyledText(
                    text = stringResource(id = R.string.statistics_activity),
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                if (maxMs > 0) {
                    StyledText(
                        text = stringResource(id = R.string.activity_week_total, formatDuration(totalMs)),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        maxLines = 1
                    )
                    StyledText(
                        text = stringResource(id = R.string.statistics_max, formatDuration(maxMs)),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        maxLines = 1
                    )
                }
            }
            Crossfade(
                targetState = selected,
                animationSpec = tween(200),
                label = "selectedInfo"
            ) { sel ->
                if (sel != null) {
                    Column(horizontalAlignment = Alignment.End) {
                        StyledText(
                            text = formatActivityBucketLabel(sel, ActivityBucketScale.DAY),
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = MaterialTheme.colorScheme.primary
                            ),
                            maxLines = 1
                        )
                        StyledText(
                            text = formatDuration(sel.timeMs),
                            style = MaterialTheme.typography.titleSmall.copy(
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            ),
                            maxLines = 1
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        WeekActivityChart(
            buckets = buckets,
            selectedIndex = selectedIndex,
            onSelect = { selectedIndex = if (selectedIndex == it) null else it }
        )
    }
}
