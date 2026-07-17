/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.statistics
import androidx.compose.ui.res.painterResource

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import com.byteflipper.everbook.R
import com.byteflipper.everbook.domain.statistics.ActivityBucketScale
import com.byteflipper.everbook.domain.statistics.BookReadTime
import com.byteflipper.everbook.domain.statistics.ReadingStatistics
import com.byteflipper.everbook.domain.statistics.ReadingPeriodComparison
import com.byteflipper.everbook.domain.statistics.StatMetric
import com.byteflipper.everbook.domain.statistics.StatMetricPref
import com.byteflipper.everbook.domain.statistics.DayBucket
import com.byteflipper.everbook.domain.statistics.StatSection
import com.byteflipper.everbook.domain.statistics.StatisticsRange
import com.byteflipper.everbook.domain.statistics.defaultStatMetrics
import com.byteflipper.everbook.domain.statistics.defaultStatSections
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyGridState
import sh.calvin.reorderable.rememberReorderableLazyListState
import com.byteflipper.everbook.presentation.core.components.common.AsyncCoverImage
import com.byteflipper.everbook.presentation.core.components.common.StyledText
import com.byteflipper.everbook.presentation.navigator.NavigatorBackIconButton
import com.byteflipper.everbook.ui.statistics.StatisticsEvent
import com.byteflipper.everbook.ui.statistics.StatisticsState
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.ceil
import kotlinx.coroutines.delay

@Composable
internal fun HeatmapSection(heatmap: List<DayBucket>) {
    if (heatmap.isEmpty()) return

    val emptyColor = MaterialTheme.colorScheme.surfaceContainerHighest
    val base = MaterialTheme.colorScheme.primary
    val levels = remember(base) {
        listOf(
            base.copy(alpha = 0.30f),
            base.copy(alpha = 0.52f),
            base.copy(alpha = 0.76f),
            base.copy(alpha = 1f)
        )
    }
    val columns = remember(heatmap) { ceil(heatmap.size / 7f).toInt() }

    val monthFormat = remember { SimpleDateFormat("LLL", Locale.getDefault()) }
    val monthLabels = remember(heatmap) {
        // Label the column that contains the 1st of each month (GitHub style).
        val result = mutableListOf<Pair<Int, String>>()
        val cal = Calendar.getInstance()
        heatmap.forEachIndexed { index, bucket ->
            cal.timeInMillis = bucket.dayStartMillis
            if (cal.get(Calendar.DAY_OF_MONTH) == 1) {
                val col = index / 7
                if (result.none { it.first == col }) {
                    result.add(col to monthFormat.format(Date(bucket.dayStartMillis)))
                }
            }
        }
        result
    }
    val weekdayFormat = remember { SimpleDateFormat("EEE", Locale.getDefault()) }
    // GitHub shows Mon/Wed/Fri only. Rows are Mon..Sun (row 0 = Monday).
    val weekdayLabels = remember(heatmap) {
        listOf(0, 2, 4).mapNotNull { row ->
            heatmap.getOrNull(row)?.let { row to weekdayFormat.format(Date(it.dayStartMillis)) }
        }
    }
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant.toArgb()
    val labelPaint = remember(labelColor) {
        android.graphics.Paint().apply {
            color = labelColor
            isAntiAlias = true
        }
    }

    var selectedIndex by remember(heatmap) { mutableStateOf<Int?>(null) }
    val selected = selectedIndex?.let { heatmap.getOrNull(it) }
    val selectedDateFormat = remember { SimpleDateFormat("d MMM", Locale.getDefault()) }
    val outlineColor = MaterialTheme.colorScheme.onSurface

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(R.drawable.ic_schedule_rounded_24px),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                StyledText(
                    text = stringResource(id = R.string.statistics_calendar),
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
            if (selected != null) {
                StyledText(
                    text = "${selectedDateFormat.format(Date(selected.dayStartMillis))}  •  " +
                            formatDuration(selected.timeMs),
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = MaterialTheme.colorScheme.primary
                    ),
                    maxLines = 1
                )
            }
        }
        Spacer(Modifier.height(12.dp))

        // Fixed cell size + horizontal scroll: 18 weeks don't fit the screen width.
        val cellDp = 15.dp
        val gapDp = 4.dp
        val labelStripDp = 16.dp
        val weekdayWidthDp = 30.dp
        val gridWidthDp = cellDp * columns + gapDp * (columns - 1)
        val gridHeightDp = labelStripDp + cellDp * 7 + gapDp * 6

        val scrollState = rememberScrollState()
        LaunchedEffect(gridWidthDp) { scrollState.scrollTo(scrollState.maxValue) }

        Row {
            // Fixed weekday labels (Mon/Wed/Fri), not scrolled.
            Canvas(
                modifier = Modifier
                    .width(weekdayWidthDp)
                    .height(gridHeightDp)
            ) {
                val cell = cellDp.toPx()
                val gap = gapDp.toPx()
                val labelStrip = labelStripDp.toPx()
                labelPaint.textSize = cell * 0.62f
                weekdayLabels.forEach { (row, name) ->
                    val top = labelStrip + row * (cell + gap)
                    drawContext.canvas.nativeCanvas.drawText(
                        name,
                        0f,
                        top + cell * 0.5f + labelPaint.textSize * 0.35f,
                        labelPaint
                    )
                }
            }

            Row(modifier = Modifier.horizontalScroll(scrollState)) {
                Canvas(
                    modifier = Modifier
                        .width(gridWidthDp)
                        .height(gridHeightDp)
                        .pointerInput(heatmap) {
                            detectTapGestures { offset ->
                                val cell = cellDp.toPx()
                                val gap = gapDp.toPx()
                                val labelStrip = labelStripDp.toPx()
                                val col = (offset.x / (cell + gap)).toInt()
                                val row = ((offset.y - labelStrip) / (cell + gap)).toInt()
                                if (row in 0..6 && col >= 0) {
                                    val idx = col * 7 + row
                                    if (idx in heatmap.indices) {
                                        selectedIndex = if (selectedIndex == idx) null else idx
                                    }
                                }
                            }
                        }
                ) {
                    val cell = cellDp.toPx()
                    val gap = gapDp.toPx()
                    val labelStrip = labelStripDp.toPx()
                    val radius = CornerRadius(cell * 0.25f, cell * 0.25f)

                    labelPaint.textSize = labelStrip * 0.72f
                    monthLabels.forEach { (col, name) ->
                        drawContext.canvas.nativeCanvas.drawText(
                            name,
                            col * (cell + gap),
                            labelStrip * 0.8f,
                            labelPaint
                        )
                    }

                    heatmap.forEachIndexed { index, bucket ->
                        val col = index / 7
                        val row = index % 7
                        val minutes = bucket.timeMs / 60_000L
                        val color = when {
                            minutes <= 0 -> emptyColor
                            minutes < 15 -> levels[0]
                            minutes < 30 -> levels[1]
                            minutes < 60 -> levels[2]
                            else -> levels[3]
                        }
                        drawRoundRect(
                            color = color,
                            topLeft = Offset(col * (cell + gap), labelStrip + row * (cell + gap)),
                            size = Size(cell, cell),
                            cornerRadius = radius
                        )
                    }

                    selectedIndex?.let { idx ->
                        val col = idx / 7
                        val row = idx % 7
                        drawRoundRect(
                            color = outlineColor,
                            topLeft = Offset(col * (cell + gap), labelStrip + row * (cell + gap)),
                            size = Size(cell, cell),
                            cornerRadius = radius,
                            style = Stroke(width = cell * 0.14f)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        HeatmapLegend(emptyColor = emptyColor, levels = levels)
    }
}

@Composable
internal fun HeatmapLegend(emptyColor: Color, levels: List<Color>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        StyledText(
            text = stringResource(id = R.string.statistics_less),
            style = MaterialTheme.typography.labelSmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        Spacer(Modifier.width(6.dp))
        (listOf(emptyColor) + levels).forEach { color ->
            Box(
                modifier = Modifier
                    .padding(horizontal = 2.dp)
                    .size(12.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(color)
            )
        }
        Spacer(Modifier.width(6.dp))
        StyledText(
            text = stringResource(id = R.string.statistics_more),
            style = MaterialTheme.typography.labelSmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }
}
