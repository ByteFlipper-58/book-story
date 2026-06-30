/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.statistics

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.byteflipper.everbook.domain.statistics.ActivityRange
import com.byteflipper.everbook.domain.statistics.DayBucket
import com.byteflipper.everbook.presentation.core.components.common.StyledText
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
internal fun ActivityRangeBarChart(
    buckets: List<DayBucket>,
    range: ActivityRange
) {
    if (buckets.isEmpty()) return

    val maxMs = remember(buckets) { buckets.maxOfOrNull { it.timeMs } ?: 0L }
    val barColorVal = MaterialTheme.colorScheme.primaryContainer
    val todayColorVal = MaterialTheme.colorScheme.primary
    val selectedColorVal = MaterialTheme.colorScheme.tertiary
    val baselineColorVal = MaterialTheme.colorScheme.surfaceContainerHighest
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val labelColorVal = MaterialTheme.colorScheme.onSurfaceVariant

    var selectedIndex by remember(buckets) { mutableStateOf<Int?>(null) }
    var play by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { play = true }
    val grow by animateFloatAsState(if (play) 1f else 0f, tween(700), label = "bars")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(16.dp)
    ) {
        if (selectedIndex != null) {
            val sel = buckets.getOrNull(selectedIndex!!)
            if (sel != null) {
                StyledText(
                    text = formatDuration(sel.timeMs),
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold
                    ),
                    maxLines = 1
                )
                Spacer(Modifier.height(8.dp))
            }
        }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .pointerInput(buckets) {
                    detectTapGestures { offset ->
                        if (buckets.isEmpty()) return@detectTapGestures
                        val slot = size.width / buckets.size
                        val index = (offset.x / slot).toInt().coerceIn(0, buckets.lastIndex)
                        selectedIndex = if (selectedIndex == index) null else index
                    }
                }
        ) {
            if (buckets.isEmpty()) return@Canvas

            drawLine(
                color = gridColor,
                start = Offset(0f, size.height),
                end = Offset(size.width, size.height),
                strokeWidth = 2f
            )

            val slot = size.width / buckets.size
            val barWidth = slot * 0.55f
            val corner = CornerRadius(barWidth / 2, barWidth / 2)
            val minBar = barWidth * 0.4f

            buckets.forEachIndexed { index, bucket ->
                val fraction = if (maxMs > 0) bucket.timeMs.toFloat() / maxMs else 0f
                val barHeight = if (bucket.timeMs > 0L) {
                    (fraction * size.height * grow).coerceAtLeast(minBar)
                } else {
                    minBar
                }
                val x = index * slot + (slot - barWidth) / 2f
                val y = size.height - barHeight
                val isLast = index == buckets.lastIndex
                drawRoundRect(
                    color = when {
                        index == selectedIndex -> selectedColorVal
                        isLast -> todayColorVal
                        bucket.timeMs > 0 -> barColorVal
                        else -> baselineColorVal
                    },
                    topLeft = Offset(x, y),
                    size = Size(barWidth, barHeight),
                    cornerRadius = corner
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val labels = remember(buckets, range) { buildChartAxisLabels(buckets, range) }
            labels.forEach { label ->
                StyledText(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = labelColorVal,
                        textAlign = TextAlign.Center
                    ),
                    maxLines = 1
                )
            }
        }
    }
}

private fun buildChartAxisLabels(buckets: List<DayBucket>, range: ActivityRange): List<String> {
    if (buckets.isEmpty()) return emptyList()
    val locale = Locale.getDefault()
    return when (range) {
        ActivityRange.DAY -> emptyList()
        ActivityRange.WEEK -> {
            val fmt = SimpleDateFormat("EEEEE", locale)
            buckets.map { fmt.format(Date(it.dayStartMillis)) }
        }
        ActivityRange.MONTH, ActivityRange.THREE_MONTHS -> {
            val fmt = SimpleDateFormat("d", locale)
            buckets.map { fmt.format(Date(it.dayStartMillis)) }
        }
        ActivityRange.YEAR -> {
            val fmt = SimpleDateFormat("LLLLL", locale)
            buckets.map { fmt.format(Date(it.dayStartMillis)) }
        }
    }
}
