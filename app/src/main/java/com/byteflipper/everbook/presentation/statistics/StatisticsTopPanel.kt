/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.statistics
import androidx.compose.ui.res.painterResource

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.byteflipper.everbook.R
import com.byteflipper.everbook.domain.statistics.ReadingStatistics
import com.byteflipper.everbook.domain.statistics.StatMetric
import com.byteflipper.everbook.domain.statistics.StatMetricPref
import com.byteflipper.everbook.presentation.core.components.common.StyledText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun StatisticsTopPanel(
    metrics: List<StatMetricPref>,
    stats: ReadingStatistics,
    todayMinutes: Int,
    goalMinutes: Int,
    onGoalClick: () -> Unit,
    onEditClick: () -> Unit,
    editMode: Boolean,
    onEditDone: () -> Unit,
    onResetClick: () -> Unit,
    onToggleMetric: (StatMetric) -> Unit,
    onReorderMetrics: (List<StatMetric>) -> Unit,
    modifier: Modifier = Modifier
) {
    val panelMetrics = remember(metrics, editMode) {
        if (editMode) metrics else metrics.filter { it.visible }
    }
    val pages = remember(panelMetrics) {
        val firstPage = panelMetrics.take(3)
        val restPages = panelMetrics.drop(3).chunked(6)
        listOf(firstPage) + restPages
    }

    val fraction = if (goalMinutes > 0) {
        (todayMinutes.toFloat() / goalMinutes).coerceIn(0f, 1f)
    } else 0f
    val reached = goalMinutes > 0 && todayMinutes >= goalMinutes
    val pagerState = rememberPagerState(pageCount = { pages.size })
    LaunchedEffect(editMode, pages.size) {
        if (!editMode && pagerState.currentPage != 0) {
            pagerState.animateScrollToPage(0)
        } else if (pagerState.currentPage >= pages.size) {
            pagerState.scrollToPage((pages.size - 1).coerceAtLeast(0))
        }
    }

    val panelHeight = 212.dp
    Column(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(tween(240))
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(panelHeight),
            pageSpacing = 16.dp
        ) { page ->
            if (page == 0) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        GoalRing(
                            fraction = fraction,
                            reached = reached,
                            size = panelHeight - 56.dp,
                            todayMinutes = todayMinutes,
                            goalMinutes = goalMinutes
                        )
                    }
                    TopMetricColumn(
                        metrics = pages[page],
                        allMetrics = metrics,
                        stats = stats,
                        modifier = Modifier.weight(1f),
                        editMode = editMode,
                        onToggleMetric = onToggleMetric,
                        onReorderMetrics = onReorderMetrics
                    )
                }
            } else {
                TopMetricGridPage(
                    metrics = pages[page],
                    allMetrics = metrics,
                    stats = stats,
                    modifier = Modifier.fillMaxSize(),
                    editMode = editMode,
                    onToggleMetric = onToggleMetric,
                    onReorderMetrics = onReorderMetrics
                )
            }
        }

        if (pages.size > 1) {
            Spacer(Modifier.height(12.dp))
            PagerDots(
                count = pages.size,
                currentPage = pagerState.currentPage,
                offsetFraction = pagerState.currentPageOffsetFraction
            )
        }

        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val secondaryWeight by animateFloatAsState(
                targetValue = if (editMode) 1f else 0.2f,
                animationSpec = tween(320),
                label = "secondaryWeight"
            )
            FilledTonalButton(
                onClick = if (editMode) onResetClick else onGoalClick,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Crossfade(
                    targetState = editMode,
                    animationSpec = tween(200),
                    label = "primaryIcon"
                ) { editing ->
                    Icon(
                        painter = if (editing) painterResource(R.drawable.ic_restart_alt_rounded_24px)
                        else painterResource(R.drawable.ic_flag_rounded_24px),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(Modifier.width(8.dp))
                Crossfade(
                    targetState = editMode,
                    animationSpec = tween(200),
                    label = "primaryLabel"
                ) { editing ->
                    StyledText(
                        text = stringResource(
                            id = if (editing) R.string.statistics_reset_layout_action
                            else R.string.statistics_goal_button
                        ),
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Clip
                    )
                }
            }
            FilledTonalButton(
                onClick = if (editMode) onEditDone else onEditClick,
                modifier = Modifier.weight(secondaryWeight),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Crossfade(
                    targetState = editMode,
                    animationSpec = tween(200),
                    label = "secondaryIcon"
                ) { editing ->
                    Icon(
                        painter = if (editing) painterResource(R.drawable.ic_done_rounded_24px)
                        else painterResource(R.drawable.ic_edit_rounded_24px),
                        contentDescription = stringResource(
                            id = R.string.statistics_edit_panel_content_desc
                        ),
                        modifier = Modifier.size(20.dp)
                    )
                }
                AnimatedVisibility(
                    visible = editMode,
                    enter = expandHorizontally(tween(280), clip = false) + fadeIn(tween(220, delayMillis = 60)),
                    exit = shrinkHorizontally(tween(220), clip = false) + fadeOut(tween(120))
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Spacer(Modifier.width(8.dp))
                        StyledText(
                            text = stringResource(id = R.string.done),
                            style = MaterialTheme.typography.labelLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Clip
                        )
                    }
                }
            }
        }
    }
}
