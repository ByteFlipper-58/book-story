/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.ui.statistics

import androidx.compose.runtime.Immutable
import com.byteflipper.everbook.domain.statistics.ActivityRange
import com.byteflipper.everbook.domain.statistics.ActivityRangeStats
import com.byteflipper.everbook.domain.statistics.DailyActivityStats
import java.time.LocalDate
import java.time.YearMonth

@Immutable
data class ActivityState(
    val range: ActivityRange = ActivityRange.DAY,
    val stats: DailyActivityStats = DailyActivityStats(LocalDate.now()),
    val rangeStats: ActivityRangeStats? = null,
    val selectedDate: LocalDate = LocalDate.now(),
    val calendarMonth: YearMonth = YearMonth.now(),
    val goalMinutes: Int = StatisticsModel.DEFAULT_GOAL,
    val isLoading: Boolean = true,
    val isCalendarVisible: Boolean = false
)
