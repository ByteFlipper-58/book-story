/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.statistics

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.byteflipper.everbook.R
import com.byteflipper.everbook.domain.statistics.ActivityRange
import com.byteflipper.everbook.presentation.core.components.common.StyledText
import com.byteflipper.everbook.presentation.navigator.NavigatorBackIconButton
import com.byteflipper.everbook.ui.statistics.ActivityState
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityContent(
    state: ActivityState,
    listState: LazyListState,
    scrollBehavior: TopAppBarScrollBehavior,
    navigateBack: () -> Unit,
    navigateToBookInfo: (Int) -> Unit,
    onRangeChange: (ActivityRange) -> Unit,
    onPreviousPeriod: () -> Unit,
    onNextPeriod: () -> Unit,
    onSelectDate: (LocalDate) -> Unit,
    onShowCalendar: () -> Unit,
    onDismissCalendar: () -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onBreakdownClick: (ActivityRange, LocalDate) -> Unit
) {
    if (state.isCalendarVisible) {
        ActivityCalendarBottomSheet(
            state = state,
            onDismiss = onDismissCalendar,
            onPreviousMonth = onPreviousMonth,
            onNextMonth = onNextMonth,
            onSelectDate = {
                onSelectDate(it)
                onDismissCalendar()
            }
        )
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .windowInsetsPadding(WindowInsets.navigationBars),
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            LargeTopAppBar(
                title = { StyledText(stringResource(id = R.string.activity_screen)) },
                navigationIcon = { NavigatorBackIconButton(navigateBack = navigateBack) },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Crossfade(
            targetState = state.range,
            animationSpec = tween(300),
            label = "range_crossfade"
        ) { targetRange ->
            val isDay = targetRange == ActivityRange.DAY
            val rangeStats = state.rangeStats

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = listState,
                contentPadding = PaddingValues(
                    start = 16.dp,
                    top = padding.calculateTopPadding() + 12.dp,
                    end = 16.dp,
                    bottom = padding.calculateBottomPadding() + 20.dp
                ),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item(key = "nav_row") {
                    ActivityNavigationRow(
                        state = state,
                        onRangeChange = onRangeChange,
                        onPreviousPeriod = onPreviousPeriod,
                        onNextPeriod = onNextPeriod,
                        onShowCalendar = onShowCalendar
                    )
                }
                if (isDay) {
                    item(key = "day_summary") {
                        ActivityDaySummary(
                            timeMs = state.stats.timeMs,
                            goalMinutes = state.goalMinutes
                        )
                    }
                    item(key = "hourly_chart") {
                        ActivityHourlyChart(buckets = state.stats.hourlyBuckets)
                    }
                    item(key = "books_header") {
                        StyledText(
                            text = stringResource(id = R.string.activity_books_title),
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                    if (state.stats.books.isEmpty()) {
                        item(key = "empty_books") {
                            ActivityEmptyBooks(isDay = true)
                        }
                    } else {
                        itemsIndexed(
                            items = state.stats.books,
                            key = { _, book -> book.bookId }
                        ) { index, book ->
                            ActivityRankedBookItem(
                                rank = index + 1,
                                book = book,
                                onClick = { navigateToBookInfo(book.bookId) }
                            )
                        }
                    }
                } else if (rangeStats != null) {
                    item(key = "chart") {
                        ActivityRangeBarChart(
                            buckets = rangeStats.buckets,
                            range = rangeStats.range
                        )
                    }
                    item(key = "breakdown_header") {
                        ActivityBreakdownHeader(
                            rangeLabel = rangeStats.label,
                            showAvgDaily = rangeStats.range != ActivityRange.WEEK
                        )
                    }
                    item(key = "breakdown_cards") {
                        val today = LocalDate.now()
                        val zone = ZoneId.systemDefault()
                        val filtered = rangeStats.buckets
                            .filter { bucket ->
                                val d = Instant.ofEpochMilli(bucket.dayStartMillis)
                                    .atZone(zone).toLocalDate()
                                !d.isAfter(today)
                            }
                            .reversed()
                        ActivityBreakdownGroup(
                            buckets = filtered,
                            range = rangeStats.range,
                            onCardClick = onBreakdownClick
                        )
                    }
                    item(key = "books_header") {
                        val titleRes = when (rangeStats.range) {
                            ActivityRange.WEEK -> R.string.activity_books_title_week
                            ActivityRange.MONTH -> R.string.activity_books_title_month
                            ActivityRange.THREE_MONTHS -> R.string.activity_books_title_3months
                            ActivityRange.YEAR -> R.string.activity_books_title_year
                            else -> R.string.activity_books_title
                        }
                        StyledText(
                            text = stringResource(id = titleRes),
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                    if (rangeStats.books.isEmpty()) {
                        item(key = "empty_books") {
                            ActivityEmptyBooks(isDay = false)
                        }
                    } else {
                        itemsIndexed(
                            items = rangeStats.books,
                            key = { _, book -> book.bookId }
                        ) { index, book ->
                            ActivityRankedBookItem(
                                rank = index + 1,
                                book = book,
                                onClick = { navigateToBookInfo(book.bookId) }
                            )
                        }
                    }
                }
            }
        }
    }
}
