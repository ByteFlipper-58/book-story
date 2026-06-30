/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.domain.use_case.statistics

import com.byteflipper.everbook.domain.repository.StatisticsRepository
import com.byteflipper.everbook.domain.statistics.ActivityBucketScale
import com.byteflipper.everbook.domain.statistics.BookReadTime
import com.byteflipper.everbook.domain.statistics.DayBucket
import com.byteflipper.everbook.domain.statistics.ReadingPeriodComparison
import com.byteflipper.everbook.domain.statistics.ReadingStatistics
import com.byteflipper.everbook.domain.statistics.ReadingSession
import com.byteflipper.everbook.domain.statistics.StatisticsRange
import com.byteflipper.everbook.domain.statistics.alignToWeekStart
import com.byteflipper.everbook.domain.use_case.book.GetBooks
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.YearMonth
import java.time.temporal.ChronoUnit
import javax.inject.Inject

class GetReadingStatistics @Inject constructor(
    private val repository: StatisticsRepository,
    private val getBooks: GetBooks
) {
    suspend fun execute(
        weekStartsMonday: Boolean = true,
        range: StatisticsRange = StatisticsRange.WEEK
    ): ReadingStatistics {
        val sessions = repository.getSessions()
        val books = getBooks.execute("")
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)

        val activeDays = sessions.map {
            Instant.ofEpochMilli(it.startTime).atZone(zone).toLocalDate()
        }.toSet()

        // Streak: consecutive days up to today (a grace day for "read yesterday").
        var day: LocalDate? = when {
            activeDays.contains(today) -> today
            activeDays.contains(today.minusDays(1)) -> today.minusDays(1)
            else -> null
        }
        var streak = 0
        while (day != null && activeDays.contains(day)) {
            streak++
            day = day.minusDays(1)
        }

        // Longest streak ever: longest run of consecutive active days.
        var longestStreak = 0
        var run = 0
        var prev: LocalDate? = null
        for (activeDay in activeDays.sorted()) {
            run = if (prev != null && activeDay == prev.plusDays(1)) run + 1 else 1
            if (run > longestStreak) longestStreak = run
            prev = activeDay
        }

        // Daily buckets for the longest chart range; UI slices last 7/30.
        val msByDay = sessions.groupBy {
            Instant.ofEpochMilli(it.startTime).atZone(zone).toLocalDate()
        }.mapValues { (_, daySessions) -> daySessions.sumOf { it.durationMs } }

        val daily = (CHART_DAYS - 1 downTo 0).map { offset ->
            val d = today.minusDays(offset.toLong())
            DayBucket(
                dayStartMillis = d.atStartOfDay(zone).toInstant().toEpochMilli(),
                timeMs = msByDay[d] ?: 0L
            )
        }

        // Heatmap: aligned grid starting on the chosen week-start day, ending today.
        // dayOfWeek: Mon=1..Sun=7. Offset to the most recent week-start on/before today.
        val backToWeekStart = if (weekStartsMonday) {
            today.dayOfWeek.value - 1
        } else {
            today.dayOfWeek.value % 7 // Sunday(7)->0, Mon(1)->1 ... Sat(6)->6
        }
        val firstWeekStart = today
            .minusDays(backToWeekStart.toLong())
            .minusWeeks((HEATMAP_WEEKS - 1).toLong())
        val heatmapDays = java.time.temporal.ChronoUnit.DAYS.between(firstWeekStart, today).toInt()
        val heatmap = (0..heatmapDays).map { offset ->
            val d = firstWeekStart.plusDays(offset.toLong())
            DayBucket(
                dayStartMillis = d.atStartOfDay(zone).toInstant().toEpochMilli(),
                timeMs = msByDay[d] ?: 0L
            )
        }

        val booksById = books.associateBy { it.id }
        val perBook = sessions.groupBy { it.bookId }
            .mapNotNull { (bookId, bookSessions) ->
                val book = booksById[bookId] ?: return@mapNotNull null
                BookReadTime(
                    bookId = bookId,
                    title = book.title,
                    coverImage = book.coverImage,
                    progress = book.progress,
                    timeMs = bookSessions.sumOf { it.durationMs },
                    sessionCount = bookSessions.size
                )
            }
            .sortedByDescending { it.timeMs }

        val totalTime = sessions.sumOf { it.durationMs }
        val averagePerActiveDay =
            if (activeDays.isNotEmpty()) totalTime / activeDays.size else 0L

        // Reading time by part of day, from each session's start hour.
        val timeOfDay = longArrayOf(0, 0, 0, 0)
        sessions.forEach { session ->
            val hour = Instant.ofEpochMilli(session.startTime).atZone(zone).hour
            val part = when (hour) {
                in 5..11 -> 0  // morning
                in 12..16 -> 1 // day
                in 17..21 -> 2 // evening
                else -> 3      // night
            }
            timeOfDay[part] += session.durationMs
        }

        val activityStart = rangeStart(range, today, activeDays.minOrNull(), weekStartsMonday)
        val activityBuckets = buildActivityBuckets(
            range = range,
            weekStartsMonday = weekStartsMonday,
            start = activityStart,
            today = today,
            zone = zone,
            msByDay = msByDay
        )
        val activityComparison = buildPeriodComparison(
            range = range,
            sessions = sessions,
            zone = zone,
            start = activityStart,
            today = today
        )

        return ReadingStatistics(
            totalTimeMs = totalTime,
            streakDays = streak,
            longestStreakDays = longestStreak,
            averagePerActiveDayMs = averagePerActiveDay,
            booksFinished = books.count { it.progress >= 1f },
            booksInProgress = books.count { it.progress > 0f && it.progress < 1f },
            totalSessions = sessions.size,
            longestSessionMs = sessions.maxOfOrNull { it.durationMs } ?: 0L,
            bestDayMs = msByDay.values.maxOrNull() ?: 0L,
            timeOfDayMs = timeOfDay.toList(),
            daily = daily,
            activityBuckets = activityBuckets.buckets,
            activityBucketScale = activityBuckets.scale,
            activityComparison = activityComparison,
            heatmap = heatmap,
            perBook = perBook
        )
    }

    private fun rangeStart(
        range: StatisticsRange,
        today: LocalDate,
        firstActiveDay: LocalDate?,
        weekStartsMonday: Boolean
    ): LocalDate {
        return when (range) {
            StatisticsRange.WEEK -> alignToWeekStart(today, weekStartsMonday)
            StatisticsRange.MONTH -> today.withDayOfMonth(1)
            StatisticsRange.THREE_MONTHS -> today.minusMonths(2).withDayOfMonth(1)
            StatisticsRange.YEAR -> today.minusMonths(11).withDayOfMonth(1)
            StatisticsRange.ALL_TIME -> firstActiveDay ?: today
        }
    }

    private fun buildActivityBuckets(
        range: StatisticsRange,
        weekStartsMonday: Boolean,
        start: LocalDate,
        today: LocalDate,
        zone: ZoneId,
        msByDay: Map<LocalDate, Long>
    ): ActivityBuckets {
        return when (range) {
            StatisticsRange.WEEK,
            StatisticsRange.MONTH -> ActivityBuckets(
                scale = ActivityBucketScale.DAY,
                buckets = buildDailyBuckets(start, today, zone, msByDay)
            )

            StatisticsRange.THREE_MONTHS -> ActivityBuckets(
                scale = ActivityBucketScale.WEEK,
                buckets = buildWeeklyBuckets(
                    start = start,
                    today = today,
                    weekStartsMonday = weekStartsMonday,
                    zone = zone,
                    msByDay = msByDay
                )
            )

            StatisticsRange.YEAR -> ActivityBuckets(
                scale = ActivityBucketScale.MONTH,
                buckets = buildMonthlyBuckets(start, today, zone, msByDay)
            )

            StatisticsRange.ALL_TIME -> {
                val monthCount = ChronoUnit.MONTHS.between(
                    YearMonth.from(start),
                    YearMonth.from(today)
                ).toInt() + 1
                if (monthCount <= ALL_TIME_MONTH_LIMIT) {
                    ActivityBuckets(
                        scale = ActivityBucketScale.MONTH,
                        buckets = buildMonthlyBuckets(start, today, zone, msByDay)
                    )
                } else {
                    ActivityBuckets(
                        scale = ActivityBucketScale.YEAR,
                        buckets = buildYearlyBuckets(start, today, zone, msByDay)
                    )
                }
            }
        }
    }

    private fun buildDailyBuckets(
        start: LocalDate,
        today: LocalDate,
        zone: ZoneId,
        msByDay: Map<LocalDate, Long>
    ): List<DayBucket> {
        val days = ChronoUnit.DAYS.between(start, today).toInt()
        return (0..days).map { offset ->
            val date = start.plusDays(offset.toLong())
            DayBucket(
                dayStartMillis = date.atStartOfDay(zone).toInstant().toEpochMilli(),
                timeMs = msByDay[date] ?: 0L
            )
        }
    }

    private fun buildWeeklyBuckets(
        start: LocalDate,
        today: LocalDate,
        weekStartsMonday: Boolean,
        zone: ZoneId,
        msByDay: Map<LocalDate, Long>
    ): List<DayBucket> {
        val alignedStart = alignToWeekStart(start, weekStartsMonday)
        val weeks = ChronoUnit.WEEKS.between(alignedStart, today).toInt()
        return (0..weeks).map { offset ->
            val weekStart = alignedStart.plusWeeks(offset.toLong())
            val weekEndExclusive = weekStart.plusWeeks(1)
            DayBucket(
                dayStartMillis = weekStart.atStartOfDay(zone).toInstant().toEpochMilli(),
                timeMs = msByDay.entries.sumOf { (date, ms) ->
                    if (!date.isBefore(weekStart) && date.isBefore(weekEndExclusive)) ms else 0L
                }
            )
        }
    }

    private fun buildMonthlyBuckets(
        start: LocalDate,
        today: LocalDate,
        zone: ZoneId,
        msByDay: Map<LocalDate, Long>
    ): List<DayBucket> {
        val firstMonth = YearMonth.from(start)
        val lastMonth = YearMonth.from(today)
        val months = ChronoUnit.MONTHS.between(firstMonth, lastMonth).toInt()
        return (0..months).map { offset ->
            val month = firstMonth.plusMonths(offset.toLong())
            DayBucket(
                dayStartMillis = month.atDay(1).atStartOfDay(zone).toInstant().toEpochMilli(),
                timeMs = msByDay.entries.sumOf { (date, ms) ->
                    if (YearMonth.from(date) == month) ms else 0L
                }
            )
        }
    }

    private fun buildYearlyBuckets(
        start: LocalDate,
        today: LocalDate,
        zone: ZoneId,
        msByDay: Map<LocalDate, Long>
    ): List<DayBucket> {
        val years = today.year - start.year
        return (0..years).map { offset ->
            val year = start.year + offset
            val yearStart = LocalDate.of(year, 1, 1)
            DayBucket(
                dayStartMillis = yearStart.atStartOfDay(zone).toInstant().toEpochMilli(),
                timeMs = msByDay.entries.sumOf { (date, ms) ->
                    if (date.year == year) ms else 0L
                }
            )
        }
    }

    private fun buildPeriodComparison(
        range: StatisticsRange,
        sessions: List<ReadingSession>,
        zone: ZoneId,
        start: LocalDate,
        today: LocalDate
    ): ReadingPeriodComparison? {
        if (range == StatisticsRange.ALL_TIME) return null

        val currentDays = ChronoUnit.DAYS.between(start, today).toInt() + 1
        val previousEnd = start.minusDays(1)
        val previousStart = previousEnd.minusDays((currentDays - 1).toLong())

        fun totalBetween(from: LocalDate, to: LocalDate): Long {
            return sessions.sumOf { session ->
                val date = Instant.ofEpochMilli(session.startTime).atZone(zone).toLocalDate()
                if (!date.isBefore(from) && !date.isAfter(to)) session.durationMs else 0L
            }
        }

        return ReadingPeriodComparison(
            currentTimeMs = totalBetween(start, today),
            previousTimeMs = totalBetween(previousStart, previousEnd)
        )
    }

    companion object {
        const val CHART_DAYS = 30
        const val HEATMAP_WEEKS = 53
        const val ALL_TIME_MONTH_LIMIT = 24
    }
}

private data class ActivityBuckets(
    val scale: ActivityBucketScale,
    val buckets: List<DayBucket>
)
