/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.domain.statistics

import android.net.Uri
import androidx.compose.runtime.Immutable

/**
 * Aggregated reading statistics shown on the statistics screen (phase 1).
 */
@Immutable
data class ReadingStatistics(
    val totalTimeMs: Long = 0,
    val streakDays: Int = 0,
    val longestStreakDays: Int = 0,
    val averagePerActiveDayMs: Long = 0,
    val booksFinished: Int = 0,
    val booksInProgress: Int = 0,
    val totalSessions: Int = 0,
    val longestSessionMs: Long = 0,
    val bestDayMs: Long = 0,
    /** Reading time by part of day: [morning, day, evening, night]. */
    val timeOfDayMs: List<Long> = listOf(0, 0, 0, 0),
    /** Reading time per day, oldest → newest, zero-filled. Covers the longest chart range. */
    val daily: List<DayBucket> = emptyList(),
    /** Reading time buckets for the currently selected activity range. */
    val activityBuckets: List<DayBucket> = emptyList(),
    /** Unit used by [activityBuckets]. */
    val activityBucketScale: ActivityBucketScale = ActivityBucketScale.DAY,
    /** Comparison between the selected activity range and the previous equally sized range. */
    val activityComparison: ReadingPeriodComparison? = null,
    /** Reading time per day for the calendar heatmap: week-aligned (starts Monday), ends today. */
    val heatmap: List<DayBucket> = emptyList(),
    /** Per-book reading time, sorted by time descending. */
    val perBook: List<BookReadTime> = emptyList()
)

@Immutable
data class DayBucket(
    val dayStartMillis: Long,
    val timeMs: Long
)

@Immutable
data class ReadingPeriodComparison(
    val currentTimeMs: Long,
    val previousTimeMs: Long
) {
    val deltaMs: Long get() = currentTimeMs - previousTimeMs
}

@Immutable
enum class ActivityBucketScale {
    DAY,
    WEEK,
    MONTH,
    YEAR
}

@Immutable
data class BookReadTime(
    val bookId: Int,
    val title: String,
    val coverImage: Uri?,
    val progress: Float,
    val timeMs: Long,
    val sessionCount: Int
)
