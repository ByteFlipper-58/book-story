/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.statistics

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.byteflipper.everbook.R
import com.byteflipper.everbook.domain.statistics.ReadingStatistics
import com.byteflipper.everbook.domain.statistics.StatMetric
import com.byteflipper.everbook.domain.statistics.StatSection
import com.byteflipper.everbook.domain.statistics.defaultStatMetrics
import com.byteflipper.everbook.domain.statistics.defaultStatSections
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import com.byteflipper.everbook.presentation.core.components.common.StyledText
import com.byteflipper.everbook.presentation.navigator.NavigatorBackIconButton
import com.byteflipper.everbook.ui.statistics.StatisticsEvent
import com.byteflipper.everbook.ui.statistics.StatisticsState
import kotlinx.coroutines.delay


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsContent(
    listState: LazyListState,
    scrollBehavior: TopAppBarScrollBehavior,
    state: StatisticsState,
    onChangeRange: (StatisticsEvent.OnChangeRange) -> Unit,
    onSetGoal: (StatisticsEvent.OnSetGoal) -> Unit,
    onToggleSection: (StatisticsEvent.OnToggleSection) -> Unit,
    onReorderSections: (StatisticsEvent.OnReorderSections) -> Unit,
    onToggleMetric: (StatisticsEvent.OnToggleMetric) -> Unit,
    onReorderMetrics: (StatisticsEvent.OnReorderMetrics) -> Unit,
    onSetWeekStart: (StatisticsEvent.OnSetWeekStart) -> Unit,
    onResetLayout: (StatisticsEvent.OnResetLayout) -> Unit,
    onResetMetrics: (StatisticsEvent.OnResetMetrics) -> Unit,
    navigateToBookInfo: (Int) -> Unit,
    navigateToAllBooks: () -> Unit,
    navigateToActivity: () -> Unit,
    navigateBack: () -> Unit
) {
    val stats = state.statistics
    val isEmpty = !state.isLoading &&
            stats.totalTimeMs == 0L &&
            stats.perBook.isEmpty()
    val todayMinutes = ((stats.daily.lastOrNull()?.timeMs ?: 0L) / 60_000L).toInt()

    var showGoalDialog by remember { mutableStateOf(false) }
    if (showGoalDialog) {
        GoalDialog(
            initialMinutes = state.goalMinutes,
            onConfirm = {
                onSetGoal(StatisticsEvent.OnSetGoal(it))
                showGoalDialog = false
            },
            onDismiss = { showGoalDialog = false }
        )
    }
    var showResetLayoutDialog by remember { mutableStateOf(false) }
    if (showResetLayoutDialog) {
        ResetLayoutDialog(
            onConfirm = {
                onResetLayout(StatisticsEvent.OnResetLayout)
                showResetLayoutDialog = false
            },
            onDismiss = { showResetLayoutDialog = false }
        )
    }
    var showResetMetricsDialog by remember { mutableStateOf(false) }
    if (showResetMetricsDialog) {
        ResetMetricsDialog(
            onConfirm = {
                onResetMetrics(StatisticsEvent.OnResetMetrics)
                showResetMetricsDialog = false
            },
            onDismiss = { showResetMetricsDialog = false }
        )
    }

    var editMode by remember { mutableStateOf(false) }
    var metricEditPhase by remember { mutableStateOf(MetricEditPhase.Normal) }
    val metricPanelEditing = metricEditPhase == MetricEditPhase.Entering ||
            metricEditPhase == MetricEditPhase.Editing

    fun enterMetricEditMode() {
        if (metricEditPhase == MetricEditPhase.Normal) {
            metricEditPhase = MetricEditPhase.Entering
        }
    }

    fun exitMetricEditMode() {
        if (metricEditPhase == MetricEditPhase.Entering ||
            metricEditPhase == MetricEditPhase.Editing
        ) {
            metricEditPhase = MetricEditPhase.Exiting
        }
    }

    LaunchedEffect(metricEditPhase) {
        when (metricEditPhase) {
            MetricEditPhase.Entering -> {
                delay(190)
                if (metricEditPhase == MetricEditPhase.Entering) {
                    metricEditPhase = MetricEditPhase.Editing
                }
            }

            MetricEditPhase.Exiting -> {
                delay(220)
                if (metricEditPhase == MetricEditPhase.Exiting) {
                    metricEditPhase = MetricEditPhase.Normal
                }
            }

            else -> Unit
        }
    }

    BackHandler(enabled = editMode || metricEditPhase != MetricEditPhase.Normal) {
        if (metricEditPhase != MetricEditPhase.Normal) exitMetricEditMode() else editMode = false
    }
    val canResetLayout = remember(state.sections, state.metrics) {
        state.sections != defaultStatSections() || state.metrics != defaultStatMetrics()
    }

    val sectionsRef by rememberUpdatedState(state.sections)
    val reorderState = rememberReorderableLazyListState(listState) { from, to ->
        val fromKey = from.key as? String ?: return@rememberReorderableLazyListState
        val toKey = to.key as? String ?: return@rememberReorderableLazyListState
        if (fromKey.startsWith("s_") && toKey.startsWith("s_")) {
            val order = sectionsRef.map { it.section }.toMutableList()
            val f = order.indexOfFirst { "s_${it.name}" == fromKey }
            val t = order.indexOfFirst { "s_${it.name}" == toKey }
            if (f != -1 && t != -1) {
                order.add(t, order.removeAt(f))
                onReorderSections(StatisticsEvent.OnReorderSections(order))
            }
        }
    }

    Scaffold(
        Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .windowInsetsPadding(WindowInsets.navigationBars),
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            LargeTopAppBar(
                title = { StyledText(stringResource(id = R.string.statistics_screen)) },
                navigationIcon = {
                    NavigatorBackIconButton(
                        navigateBack = {
                            when {
                                metricEditPhase != MetricEditPhase.Normal -> exitMetricEditMode()
                                editMode -> editMode = false
                                else -> navigateBack()
                            }
                        }
                    )
                },
                actions = {
                    if (!state.isLoading && !isEmpty && metricEditPhase == MetricEditPhase.Normal) {
                        if (editMode) {
                            IconButton(
                                enabled = canResetLayout,
                                onClick = { showResetLayoutDialog = true }
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.RestartAlt,
                                    contentDescription = stringResource(
                                        id = R.string.statistics_reset_layout_content_desc
                                    )
                                )
                            }
                        }
                        IconButton(onClick = { editMode = !editMode }) {
                            Crossfade(targetState = editMode, label = "editIcon") { editing ->
                                Icon(
                                    imageVector = if (editing) Icons.Outlined.Done
                                    else Icons.Outlined.Tune,
                                    contentDescription = stringResource(
                                        id = R.string.statistics_customize_content_desc
                                    ),
                                    tint = if (editing) MaterialTheme.colorScheme.primary
                                    else LocalContentColor.current
                                )
                            }
                        }
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        }
    ) { paddingValues ->
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            isEmpty -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Insights,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.surfaceContainerHighest
                    )
                    Spacer(Modifier.height(16.dp))
                    StyledText(
                        text = stringResource(id = R.string.statistics_empty),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    )
                }
            }

            else -> {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(
                        start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item(key = "top_panel") {
                        StatisticsTopPanel(
                            metrics = state.metrics,
                            stats = stats,
                            todayMinutes = todayMinutes,
                            goalMinutes = state.goalMinutes,
                            onGoalClick = { showGoalDialog = true },
                            onEditClick = { enterMetricEditMode() },
                            editMode = metricPanelEditing,
                            onEditDone = { exitMetricEditMode() },
                            onResetClick = { showResetMetricsDialog = true },
                            onToggleMetric = {
                                onToggleMetric(StatisticsEvent.OnToggleMetric(it))
                            },
                            onReorderMetrics = {
                                onReorderMetrics(StatisticsEvent.OnReorderMetrics(it))
                            },
                            modifier = Modifier.animateItem()
                        )
                    }

                    if (editMode && metricEditPhase == MetricEditPhase.Normal) {
                        items(state.sections, key = { "s_${it.section.name}" }) { pref ->
                            ReorderableItem(reorderState, key = "s_${pref.section.name}") {
                                EditCard(
                                    handleModifier = Modifier.draggableHandle(),
                                    title = sectionTitle(pref.section),
                                    visible = pref.visible,
                                    onToggleVisible = {
                                        onToggleSection(StatisticsEvent.OnToggleSection(pref.section))
                                    }
                                ) {
                                    SectionBody(
                                        section = pref.section,
                                        stats = stats,
                                        navigateToBookInfo = navigateToBookInfo,
                                        navigateToAllBooks = navigateToAllBooks,
                                        navigateToActivity = navigateToActivity
                                    )
                                }
                            }
                        }
                        item(key = "week_start_setting") {
                            WeekStartRow(
                                checked = state.weekStartMonday,
                                onChange = { onSetWeekStart(StatisticsEvent.OnSetWeekStart(it)) },
                                modifier = Modifier.animateItem()
                            )
                        }
                    } else {
                        item(key = "below_panel_content") {
                            Crossfade(
                                targetState = metricPanelEditing,
                                animationSpec = tween(300),
                                label = "sectionsCrossfade"
                            ) { editing ->
                                if (editing) {
                                    MetricEditPool(
                                        metrics = state.metrics,
                                        stats = stats,
                                        onToggleMetric = {
                                            onToggleMetric(StatisticsEvent.OnToggleMetric(it))
                                        }
                                    )
                                } else {
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        state.sections.filter { it.visible }.forEach { pref ->
                                            when (pref.section) {
                                                StatSection.ACTIVITY -> {
                                                    ActivitySection(
                                                        buckets = stats.daily.takeLast(7),
                                                        navigateToActivity = navigateToActivity
                                                    )
                                                }

                                                StatSection.TIME_OF_DAY -> {
                                                    if (stats.timeOfDayMs.any { it > 0 }) {
                                                        TimeOfDaySection(timeOfDayMs = stats.timeOfDayMs)
                                                    }
                                                }

                                                StatSection.CALENDAR -> {
                                                    HeatmapSection(heatmap = stats.heatmap)
                                                }

                                                StatSection.BOOKS -> {
                                                    if (stats.perBook.isNotEmpty()) {
                                                        BooksSection(
                                                            perBook = stats.perBook,
                                                            navigateToBookInfo = navigateToBookInfo,
                                                            navigateToAllBooks = navigateToAllBooks
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionBody(
    section: StatSection,
    stats: ReadingStatistics,
    navigateToBookInfo: (Int) -> Unit,
    navigateToAllBooks: () -> Unit,
    navigateToActivity: () -> Unit
) {
    when (section) {
        StatSection.ACTIVITY -> ActivitySection(
            buckets = stats.daily.takeLast(7),
            navigateToActivity = navigateToActivity
        )

        StatSection.TIME_OF_DAY -> TimeOfDaySection(timeOfDayMs = stats.timeOfDayMs)

        StatSection.CALENDAR -> HeatmapSection(heatmap = stats.heatmap)

        StatSection.BOOKS -> BooksSection(
            perBook = stats.perBook,
            navigateToBookInfo = navigateToBookInfo,
            navigateToAllBooks = navigateToAllBooks
        )
    }
}
