/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.statistics

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

internal val FireColor = Color(0xFFFF7043)
internal val TrophyColor = Color(0xFFFFB300)

@Composable
internal fun statisticsRangeLabel(range: StatisticsRange): String = stringResource(
    id = when (range) {
        StatisticsRange.WEEK -> R.string.statistics_range_7
        StatisticsRange.MONTH -> R.string.statistics_range_30
        StatisticsRange.THREE_MONTHS -> R.string.statistics_range_3_months
        StatisticsRange.YEAR -> R.string.statistics_range_year
        StatisticsRange.ALL_TIME -> R.string.statistics_range_all
    }
)

@Composable
internal fun activityScaleLabel(scale: ActivityBucketScale): String = stringResource(
    id = when (scale) {
        ActivityBucketScale.DAY -> R.string.statistics_activity_scale_days
        ActivityBucketScale.WEEK -> R.string.statistics_activity_scale_weeks
        ActivityBucketScale.MONTH -> R.string.statistics_activity_scale_months
        ActivityBucketScale.YEAR -> R.string.statistics_activity_scale_years
    }
)

internal fun activityBucketWidth(scale: ActivityBucketScale): Dp {
    return when (scale) {
        ActivityBucketScale.DAY -> 18.dp
        ActivityBucketScale.WEEK -> 28.dp
        ActivityBucketScale.MONTH -> 36.dp
        ActivityBucketScale.YEAR -> 48.dp
    }
}

@Composable
internal fun formatPeriodComparison(comparison: ReadingPeriodComparison): String {
    val delta = comparison.deltaMs
    return when {
        comparison.previousTimeMs == 0L && comparison.currentTimeMs > 0L -> {
            stringResource(id = R.string.statistics_compare_no_previous)
        }

        delta > 0L -> {
            stringResource(id = R.string.statistics_compare_more, formatDuration(delta))
        }

        delta < 0L -> {
            stringResource(id = R.string.statistics_compare_less, formatDuration(-delta))
        }

        else -> stringResource(id = R.string.statistics_compare_same)
    }
}

internal fun formatActivityBucketLabel(bucket: DayBucket, scale: ActivityBucketScale): String {
    val locale = Locale.getDefault()
    return when (scale) {
        ActivityBucketScale.DAY -> SimpleDateFormat("EEE, d MMM", locale)
            .format(Date(bucket.dayStartMillis))

        ActivityBucketScale.WEEK -> {
            val start = Date(bucket.dayStartMillis)
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = bucket.dayStartMillis
            calendar.add(Calendar.DAY_OF_YEAR, 6)
            val end = calendar.time
            val formatter = SimpleDateFormat("d MMM", locale)
            "${formatter.format(start)} - ${formatter.format(end)}"
        }

        ActivityBucketScale.MONTH -> SimpleDateFormat("LLL yyyy", locale)
            .format(Date(bucket.dayStartMillis))

        ActivityBucketScale.YEAR -> SimpleDateFormat("yyyy", locale)
            .format(Date(bucket.dayStartMillis))
    }
}

@Composable
internal fun ActivityAxisLabels(
    buckets: List<DayBucket>,
    scale: ActivityBucketScale,
    modifier: Modifier = Modifier
) {
    if (buckets.isEmpty()) return

    Row(modifier = modifier.fillMaxWidth()) {
        buckets.forEachIndexed { index, bucket ->
            val label = activityAxisLabel(index, bucket, buckets.lastIndex, scale)
            StyledText(
                text = label,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                ),
                maxLines = 1
            )
        }
    }
}

internal fun activityAxisLabel(
    index: Int,
    bucket: DayBucket,
    lastIndex: Int,
    scale: ActivityBucketScale
): String {
    val locale = Locale.getDefault()
    return when (scale) {
        ActivityBucketScale.DAY -> {
            val show = lastIndex <= 6 || index == 0 || index == lastIndex || index % 7 == 0
            if (show) SimpleDateFormat("d MMM", locale).format(Date(bucket.dayStartMillis)) else ""
        }

        ActivityBucketScale.WEEK -> {
            val show = lastIndex <= 14 || index == 0 || index == lastIndex || index % 4 == 0
            if (show) SimpleDateFormat("d MMM", locale).format(Date(bucket.dayStartMillis)) else ""
        }

        ActivityBucketScale.MONTH -> SimpleDateFormat("LLL", locale).format(Date(bucket.dayStartMillis))

        ActivityBucketScale.YEAR -> SimpleDateFormat("yyyy", locale).format(Date(bucket.dayStartMillis))
    }
}

internal data class ActivityChartState(
    val buckets: List<DayBucket>,
    val scale: ActivityBucketScale
)

@Composable
internal fun WeekActivityChart(
    buckets: List<DayBucket>,
    selectedIndex: Int?,
    onSelect: (Int) -> Unit
) {
    val maxMs = remember(buckets) { buckets.maxOfOrNull { it.timeMs } ?: 0L }
    val labelFormat = remember { SimpleDateFormat("EEE", Locale.getDefault()) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(184.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        buckets.forEachIndexed { index, bucket ->
            ActivityBucketColumn(
                bucket = bucket,
                maxMs = maxMs,
                selected = selectedIndex == index,
                label = labelFormat.format(Date(bucket.dayStartMillis)),
                onClick = { onSelect(index) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
internal fun MonthActivityChart(
    buckets: List<DayBucket>,
    selectedIndex: Int?,
    onSelect: (Int) -> Unit
) {
    val maxMs = remember(buckets) { buckets.maxOfOrNull { it.timeMs } ?: 0L }
    val scrollState = rememberScrollState()
    val maxScroll = scrollState.maxValue
    LaunchedEffect(buckets, maxScroll) {
        scrollState.scrollTo(maxScroll)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(184.dp)
            .horizontalScroll(scrollState),
        verticalAlignment = Alignment.Bottom
    ) {
        buckets.forEachIndexed { index, bucket ->
            if (index > 0 && index % 7 == 0) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 7.dp, vertical = 22.dp)
                        .width(1.dp)
                        .height(126.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f))
                )
            }
            ActivityBucketColumn(
                bucket = bucket,
                maxMs = maxMs,
                selected = selectedIndex == index,
                label = activityAxisLabel(index, bucket, buckets.lastIndex, ActivityBucketScale.DAY),
                onClick = { onSelect(index) },
                modifier = Modifier.width(34.dp)
            )
        }
    }
}

@Composable
internal fun ActivityBucketColumn(
    bucket: DayBucket,
    maxMs: Long,
    selected: Boolean,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val fraction = if (maxMs > 0) bucket.timeMs.toFloat() / maxMs else 0f
    val targetHeight = if (bucket.timeMs > 0L) {
        (116f * fraction).dp.coerceAtLeast(10.dp)
    } else {
        8.dp
    }
    val targetColor = when {
        selected -> MaterialTheme.colorScheme.tertiary
        bucket.timeMs > 0L -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.surfaceContainerHighest
    }
    val barColor by androidx.compose.animation.animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(250),
        label = "barColor"
    )
    val trackColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.45f)

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 2.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(124.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            // Track only behind the bar, same height as bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(targetHeight)
                    .clip(RoundedCornerShape(12.dp))
                    .background(trackColor)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(targetHeight)
                    .clip(RoundedCornerShape(12.dp))
                    .background(barColor)
            )
        }
        Spacer(Modifier.height(6.dp))
        StyledText(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            ),
            maxLines = 1
        )
    }
}

@Composable
internal fun ActivityBarChart(
    buckets: List<DayBucket>,
    scale: ActivityBucketScale,
    selectedIndex: Int?,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val maxMs = remember(buckets) { buckets.maxOfOrNull { it.timeMs } ?: 0L }
    val barColor = MaterialTheme.colorScheme.primaryContainer
    val todayColor = MaterialTheme.colorScheme.primary
    val selectedColor = MaterialTheme.colorScheme.tertiary
    val baselineColor = MaterialTheme.colorScheme.surfaceContainerHighest
    val gridColor = MaterialTheme.colorScheme.outlineVariant

    var play by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { play = true }
    val grow by animateFloatAsState(if (play) 1f else 0f, tween(700), label = "bars")

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp)
            .pointerInput(buckets) {
                detectTapGestures { offset ->
                    if (buckets.isEmpty()) return@detectTapGestures
                    val slot = size.width / buckets.size
                    val index = (offset.x / slot).toInt().coerceIn(0, buckets.lastIndex)
                    onSelect(index)
                }
            }
    ) {
        if (buckets.isEmpty()) return@Canvas
        // Baseline.
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

        if (scale == ActivityBucketScale.DAY && buckets.size > 7) {
            buckets.indices
                .filter { it > 0 && it % 7 == 0 }
                .forEach { index ->
                    val x = index * slot
                    drawLine(
                        color = gridColor.copy(alpha = 0.45f),
                        start = Offset(x, 0f),
                        end = Offset(x, size.height),
                        strokeWidth = 1f
                    )
                }
        }

        buckets.forEachIndexed { index, bucket ->
            val fraction = if (maxMs > 0) bucket.timeMs.toFloat() / maxMs else 0f
            val barHeight = if (bucket.timeMs > 0L) {
                (fraction * size.height * grow).coerceAtLeast(minBar)
            } else {
                minBar
            }
            val x = index * slot + (slot - barWidth) / 2f
            val y = size.height - barHeight
            val isToday = index == buckets.lastIndex
            drawRoundRect(
                color = when {
                    index == selectedIndex -> selectedColor
                    isToday -> todayColor
                    bucket.timeMs > 0 -> barColor
                    else -> baselineColor
                },
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = corner
            )
        }
    }
}
