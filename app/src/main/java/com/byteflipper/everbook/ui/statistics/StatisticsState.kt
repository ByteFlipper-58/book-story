/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.ui.statistics

import androidx.compose.runtime.Immutable
import com.byteflipper.everbook.domain.statistics.ReadingStatistics
import com.byteflipper.everbook.domain.statistics.StatMetricPref
import com.byteflipper.everbook.domain.statistics.StatSectionPref
import com.byteflipper.everbook.domain.statistics.StatisticsRange
import com.byteflipper.everbook.domain.statistics.defaultStatMetrics
import com.byteflipper.everbook.domain.statistics.defaultStatSections

@Immutable
data class StatisticsState(
    val statistics: ReadingStatistics = ReadingStatistics(),
    val isLoading: Boolean = true,
    val statisticsRange: StatisticsRange = StatisticsRange.WEEK,
    val goalMinutes: Int = 30,
    val sections: List<StatSectionPref> = defaultStatSections(),
    val metrics: List<StatMetricPref> = defaultStatMetrics(),
    val weekStartMonday: Boolean = true
)
