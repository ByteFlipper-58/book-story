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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material.icons.outlined.WbTwilight
import androidx.compose.material.icons.rounded.DragHandle
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

internal enum class MetricEditPhase {
    Normal,
    Entering,
    Editing,
    Exiting
}

@Composable
internal fun StatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    accent: Color,
    value: String,
    label: String
) {
    // Google Fit-style: tall rounded icon capsule on the left, label above value.
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(40.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(accent.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            StyledText(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                maxLines = 1
            )
            StyledText(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                ),
                maxLines = 1
            )
        }
    }
}

@Composable
internal fun TopMetricColumn(
    metrics: List<StatMetricPref>,
    allMetrics: List<StatMetricPref>,
    stats: ReadingStatistics,
    modifier: Modifier = Modifier,
    editMode: Boolean = false,
    onToggleMetric: (StatMetric) -> Unit = {},
    onReorderMetrics: (List<StatMetric>) -> Unit = {}
) {
    if (editMode) {
        val listState = rememberLazyListState()
        val metricsRef by rememberUpdatedState(allMetrics)
        val reorderState = rememberReorderableLazyListState(listState) { from, to ->
            val fromMetric = (from.key as? String)
                ?.removePrefix("panel_")
                ?.let { name -> StatMetric.entries.firstOrNull { it.name == name } }
                ?: return@rememberReorderableLazyListState
            val toMetric = (to.key as? String)
                ?.removePrefix("panel_")
                ?.let { name -> StatMetric.entries.firstOrNull { it.name == name } }
                ?: return@rememberReorderableLazyListState
            reorderMetrics(metricsRef, fromMetric, toMetric)?.let(onReorderMetrics)
        }
        LazyColumn(
            state = listState,
            modifier = modifier,
            userScrollEnabled = false,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(metrics, key = { "panel_${it.metric.name}" }) { pref ->
                ReorderableItem(reorderState, key = "panel_${pref.metric.name}") {
                    MetricPanelCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),
                        pref = pref,
                        stats = stats,
                        editMode = true,
                        handleModifier = Modifier.draggableHandle(),
                        onToggleMetric = onToggleMetric
                    )
                }
            }
        }
    } else {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            for (slot in 0 until 3) {
                val pref = metrics.getOrNull(slot)
                if (pref != null) {
                    MetricPanelCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        pref = pref,
                        stats = stats,
                        editMode = false,
                        onToggleMetric = onToggleMetric
                    )
                } else {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
internal fun TopMetricGridPage(
    metrics: List<StatMetricPref>,
    allMetrics: List<StatMetricPref>,
    stats: ReadingStatistics,
    modifier: Modifier = Modifier,
    editMode: Boolean = false,
    onToggleMetric: (StatMetric) -> Unit = {},
    onReorderMetrics: (List<StatMetric>) -> Unit = {}
) {
    val gridState = rememberLazyGridState()
    val metricsRef by rememberUpdatedState(allMetrics)
    val reorderState = rememberReorderableLazyGridState(gridState) { from, to ->
        val fromMetric = (from.key as? String)
            ?.removePrefix("panel_")
            ?.let { name -> StatMetric.entries.firstOrNull { it.name == name } }
            ?: return@rememberReorderableLazyGridState
        val toMetric = (to.key as? String)
            ?.removePrefix("panel_")
            ?.let { name -> StatMetric.entries.firstOrNull { it.name == name } }
            ?: return@rememberReorderableLazyGridState
        reorderMetrics(metricsRef, fromMetric, toMetric)?.let(onReorderMetrics)
    }
    LazyVerticalGrid(
        state = gridState,
        columns = GridCells.Fixed(2),
        modifier = modifier,
        userScrollEnabled = false,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        gridItems(metrics, key = { "panel_${it.metric.name}" }) { pref ->
            ReorderableItem(reorderState, key = "panel_${pref.metric.name}") {
                MetricPanelCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    pref = pref,
                    stats = stats,
                    editMode = editMode,
                    handleModifier = Modifier.draggableHandle(),
                    onToggleMetric = onToggleMetric
                )
            }
        }
    }
}

internal fun reorderMetrics(
    metrics: List<StatMetricPref>,
    fromMetric: StatMetric,
    toMetric: StatMetric
): List<StatMetric>? {
    val order = metrics.map { it.metric }.toMutableList()
    val fromIndex = order.indexOf(fromMetric)
    val toIndex = order.indexOf(toMetric)
    if (fromIndex == -1 || toIndex == -1 || fromIndex == toIndex) return null
    order.add(toIndex, order.removeAt(fromIndex))
    return order
}

@Composable
internal fun MetricPanelCard(
    modifier: Modifier = Modifier,
    pref: StatMetricPref,
    stats: ReadingStatistics,
    editMode: Boolean,
    handleModifier: Modifier = Modifier,
    onToggleMetric: (StatMetric) -> Unit
) {
    Box(modifier = modifier.animateContentSize(tween(220))) {
        AnimatedVisibility(
            visible = pref.visible,
            modifier = Modifier.fillMaxSize(),
            enter = fadeIn(tween(180)) + scaleIn(tween(180), initialScale = 0.96f),
            exit = fadeOut(tween(140)) + scaleOut(tween(140), targetScale = 0.96f)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                MetricCard(
                    modifier = Modifier.fillMaxSize(),
                    metric = pref.metric,
                    stats = stats
                )
                AnimatedVisibility(
                    visible = editMode,
                    enter = fadeIn(tween(140)) + scaleIn(tween(140), initialScale = 0.9f),
                    exit = fadeOut(tween(100)) + scaleOut(tween(100), targetScale = 0.9f),
                    modifier = Modifier.align(Alignment.TopStart)
                ) {
                    OverlayControl {
                        Icon(
                            imageVector = Icons.Rounded.DragHandle,
                            contentDescription = stringResource(id = R.string.drag_content_desc),
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = handleModifier.size(22.dp)
                        )
                    }
                }
                AnimatedVisibility(
                    visible = editMode,
                    enter = fadeIn(tween(140)) + scaleIn(tween(140), initialScale = 0.9f),
                    exit = fadeOut(tween(100)) + scaleOut(tween(100), targetScale = 0.9f),
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    OverlayControl(onClick = { onToggleMetric(pref.metric) }) {
                        Icon(
                            imageVector = Icons.Outlined.Remove,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
        AnimatedVisibility(
            visible = !pref.visible && editMode,
            modifier = Modifier.fillMaxSize(),
            enter = fadeIn(tween(180)),
            exit = fadeOut(tween(140))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.35f))
            )
        }
    }
}

@Composable
internal fun MetricEditCard(
    pref: StatMetricPref,
    stats: ReadingStatistics,
    onToggleMetric: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardAlpha by animateFloatAsState(
        targetValue = if (pref.visible) 0.42f else 1f,
        animationSpec = tween(220),
        label = "metricPoolCardAlpha"
    )
    Box(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(74.dp)
                .alpha(cardAlpha)
                .then(
                    if (!pref.visible) Modifier.clickable(onClick = onToggleMetric)
                    else Modifier
                )
        ) {
            MetricCard(
                modifier = Modifier.fillMaxSize(),
                metric = pref.metric,
                stats = stats
            )
        }

        OverlayControl(
            modifier = Modifier.align(Alignment.TopEnd),
            onClick = onToggleMetric
        ) {
            Icon(
                imageVector = if (pref.visible) Icons.Outlined.Remove else Icons.Outlined.Add,
                contentDescription = null,
                tint = if (pref.visible) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
internal fun MetricEditPool(
    metrics: List<StatMetricPref>,
    stats: ReadingStatistics,
    onToggleMetric: (StatMetric) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        metrics.chunked(2).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                row.forEach { pref ->
                    MetricEditCard(
                        pref = pref,
                        stats = stats,
                        onToggleMetric = { onToggleMetric(pref.metric) },
                        modifier = Modifier
                            .weight(1f)
                            .animateContentSize(tween(180))
                    )
                }
                if (row.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
internal fun MetricCard(
    modifier: Modifier = Modifier,
    metric: StatMetric,
    stats: ReadingStatistics
) {
    when (metric) {
        StatMetric.TOTAL_TIME -> StatCard(
            modifier = modifier,
            icon = Icons.Outlined.Schedule,
            accent = MaterialTheme.colorScheme.primary,
            value = formatDuration(stats.totalTimeMs),
            label = stringResource(id = R.string.statistics_total_time)
        )

        StatMetric.AVG_PER_DAY -> StatCard(
            modifier = modifier,
            icon = Icons.Outlined.TrendingUp,
            accent = MaterialTheme.colorScheme.secondary,
            value = formatDuration(stats.averagePerActiveDayMs),
            label = stringResource(id = R.string.statistics_avg_per_day)
        )

        StatMetric.STREAK -> StatCard(
            modifier = modifier,
            icon = Icons.Outlined.LocalFireDepartment,
            accent = FireColor,
            value = animatedCount(stats.streakDays).toString(),
            label = stringResource(id = R.string.statistics_streak)
        )

        StatMetric.BEST_STREAK -> StatCard(
            modifier = modifier,
            icon = Icons.Outlined.EmojiEvents,
            accent = TrophyColor,
            value = animatedCount(stats.longestStreakDays).toString(),
            label = stringResource(id = R.string.statistics_best_streak)
        )

        StatMetric.FINISHED -> StatCard(
            modifier = modifier,
            icon = Icons.Outlined.CheckCircle,
            accent = MaterialTheme.colorScheme.primary,
            value = animatedCount(stats.booksFinished).toString(),
            label = stringResource(id = R.string.statistics_books_finished)
        )

        StatMetric.IN_PROGRESS -> StatCard(
            modifier = modifier,
            icon = Icons.Outlined.AutoStories,
            accent = MaterialTheme.colorScheme.tertiary,
            value = animatedCount(stats.booksInProgress).toString(),
            label = stringResource(id = R.string.statistics_books_in_progress)
        )

        StatMetric.LONGEST_SESSION -> StatCard(
            modifier = modifier,
            icon = Icons.Outlined.Timer,
            accent = MaterialTheme.colorScheme.primary,
            value = formatDuration(stats.longestSessionMs),
            label = stringResource(id = R.string.statistics_longest_session)
        )

        StatMetric.BEST_DAY -> StatCard(
            modifier = modifier,
            icon = Icons.Outlined.Star,
            accent = TrophyColor,
            value = formatDuration(stats.bestDayMs),
            label = stringResource(id = R.string.statistics_best_day)
        )

        StatMetric.TOTAL_SESSIONS -> StatCard(
            modifier = modifier,
            icon = Icons.Outlined.Repeat,
            accent = MaterialTheme.colorScheme.secondary,
            value = animatedCount(stats.totalSessions).toString(),
            label = stringResource(id = R.string.statistics_total_sessions)
        )
    }
}
