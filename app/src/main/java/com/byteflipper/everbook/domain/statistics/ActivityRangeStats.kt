/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.domain.statistics

import androidx.compose.runtime.Immutable

@Immutable
enum class ActivityRange {
    DAY, WEEK, MONTH, THREE_MONTHS, YEAR
}

@Immutable
data class ActivityRangeStats(
    val range: ActivityRange,
    val label: String,
    val totalTimeMs: Long,
    val totalPages: Int,
    val totalBooks: Int,
    val avgDailyTimeMs: Long,
    val dayCount: Int,
    val buckets: List<DayBucket>,
    val books: List<DailyActivityBookStats>
)
