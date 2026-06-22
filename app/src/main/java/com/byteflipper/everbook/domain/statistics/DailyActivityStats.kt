/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.domain.statistics

import android.net.Uri
import androidx.compose.runtime.Immutable
import java.time.LocalDate
import java.time.YearMonth

@Immutable
data class DailyActivityStats(
    val date: LocalDate,
    val timeMs: Long = 0L,
    val pagesRead: Int = 0,
    val bookCount: Int = 0,
    val books: List<DailyActivityBookStats> = emptyList(),
    val hourlyBuckets: List<DayBucket> = emptyList(),
    val weekDays: List<ActivityCalendarDay> = emptyList(),
    val calendarMonth: YearMonth = YearMonth.from(date),
    val calendarDays: List<ActivityCalendarDay> = emptyList()
)

@Immutable
data class DailyActivityBookStats(
    val bookId: Int,
    val title: String,
    val coverImage: Uri?,
    val timeMs: Long,
    val pagesRead: Int,
    val progressStart: Float,
    val progressEnd: Float,
    val sessionCount: Int
) {
    val progressDelta: Float
        get() = (progressEnd - progressStart).coerceAtLeast(0f)
}

@Immutable
data class ActivityCalendarDay(
    val date: LocalDate,
    val timeMs: Long = 0L,
    val pagesRead: Int = 0,
    val bookCount: Int = 0,
    val isActive: Boolean = false
)
