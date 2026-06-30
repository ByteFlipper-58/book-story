/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.domain.use_case.statistics

import com.byteflipper.everbook.domain.repository.StatisticsRepository
import com.byteflipper.everbook.domain.statistics.ActivityRange
import com.byteflipper.everbook.domain.statistics.ActivityRangeStats
import com.byteflipper.everbook.domain.statistics.DailyActivityBookStats
import com.byteflipper.everbook.domain.statistics.DayBucket
import com.byteflipper.everbook.domain.statistics.ReadingSession
import com.byteflipper.everbook.domain.statistics.alignToWeekStart
import com.byteflipper.everbook.domain.use_case.book.GetBooks
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import javax.inject.Inject

class GetActivityRangeStats @Inject constructor(
    private val repository: StatisticsRepository,
    private val getBooks: GetBooks
) {
    suspend fun execute(
        range: ActivityRange,
        anchorDate: LocalDate,
        weekStartsMonday: Boolean = true
    ): ActivityRangeStats {
        val zone = ZoneId.systemDefault()
        val (start, end) = rangeWindow(range, anchorDate, weekStartsMonday)
        val today = LocalDate.now()
        val effectiveEnd = if (end.isAfter(today)) today else end

        val sessions = repository.getSessions()
        val booksById = getBooks.execute("").associateBy { it.id }

        val rangeSessions = sessions.filter { s ->
            val d = Instant.ofEpochMilli(s.startTime).atZone(zone).toLocalDate()
            !d.isBefore(start) && !d.isAfter(effectiveEnd)
        }

        val totalTimeMs = rangeSessions.sumOf { it.durationMs }
        val totalPages = rangeSessions.sumOf { it.pagesRead }
        val uniqueBooks = rangeSessions.map { it.bookId }.distinct()
        val dayCount = ChronoUnit.DAYS.between(start, effectiveEnd).toInt() + 1
        val avgDailyTimeMs = if (dayCount > 0) totalTimeMs / dayCount else 0L

        val books = rangeSessions
            .groupBy { it.bookId }
            .mapNotNull { (bookId, bookSessions) ->
                val book = booksById[bookId] ?: return@mapNotNull null
                DailyActivityBookStats(
                    bookId = bookId,
                    title = book.title,
                    coverImage = book.coverImage,
                    timeMs = bookSessions.sumOf { it.durationMs },
                    pagesRead = bookSessions.sumOf { it.pagesRead },
                    progressStart = bookSessions.minOf { it.progressStart },
                    progressEnd = bookSessions.maxOf { it.progressEnd },
                    sessionCount = bookSessions.size
                )
            }
            .sortedByDescending { it.timeMs }

        val sessionsByDate = rangeSessions.groupBy { s ->
            Instant.ofEpochMilli(s.startTime).atZone(zone).toLocalDate()
        }

        val buckets = buildBuckets(range, start, end, sessionsByDate, zone, weekStartsMonday)
        val label = formatLabel(range, start, end)

        return ActivityRangeStats(
            range = range,
            label = label,
            totalTimeMs = totalTimeMs,
            totalPages = totalPages,
            totalBooks = uniqueBooks.size,
            avgDailyTimeMs = avgDailyTimeMs,
            dayCount = dayCount,
            buckets = buckets,
            books = books
        )
    }

    private fun rangeWindow(
        range: ActivityRange,
        anchor: LocalDate,
        weekStartsMonday: Boolean
    ): Pair<LocalDate, LocalDate> {
        return when (range) {
            ActivityRange.DAY -> anchor to anchor
            ActivityRange.WEEK -> {
                val start = alignToWeekStart(anchor, weekStartsMonday)
                start to start.plusDays(6)
            }
            ActivityRange.MONTH -> {
                val start = anchor.withDayOfMonth(1)
                start to anchor.withDayOfMonth(anchor.lengthOfMonth())
            }
            ActivityRange.THREE_MONTHS -> {
                val end = anchor.withDayOfMonth(anchor.lengthOfMonth())
                val start = end.minusMonths(2).withDayOfMonth(1)
                start to end
            }
            ActivityRange.YEAR -> {
                val end = anchor.withDayOfMonth(anchor.lengthOfMonth())
                val start = end.minusMonths(11).withDayOfMonth(1)
                start to end
            }
        }
    }

    private fun buildBuckets(
        range: ActivityRange,
        start: LocalDate,
        end: LocalDate,
        sessionsByDate: Map<LocalDate, List<ReadingSession>>,
        zone: ZoneId,
        weekStartsMonday: Boolean
    ): List<DayBucket> {
        return when (range) {
            ActivityRange.DAY, ActivityRange.WEEK -> {
                val days = ChronoUnit.DAYS.between(start, end).toInt() + 1
                (0 until days).map { offset ->
                    val day = start.plusDays(offset.toLong())
                    val ms = sessionsByDate[day]?.sumOf { it.durationMs } ?: 0L
                    DayBucket(
                        dayStartMillis = day.atStartOfDay(zone).toInstant().toEpochMilli(),
                        timeMs = ms
                    )
                }
            }
            ActivityRange.MONTH, ActivityRange.THREE_MONTHS -> {
                buildWeeklyBuckets(start, end, sessionsByDate, zone, weekStartsMonday)
            }
            ActivityRange.YEAR -> {
                buildMonthlyBuckets(start, end, sessionsByDate, zone)
            }
        }
    }

    private fun buildWeeklyBuckets(
        start: LocalDate,
        end: LocalDate,
        sessionsByDate: Map<LocalDate, List<ReadingSession>>,
        zone: ZoneId,
        weekStartsMonday: Boolean = true
    ): List<DayBucket> {
        val buckets = mutableListOf<DayBucket>()
        var weekStart = alignToWeekStart(start, weekStartsMonday)
        while (!weekStart.isAfter(end)) {
            val weekEnd = weekStart.plusDays(6)
            var timeMs = 0L
            var d = weekStart
            while (!d.isAfter(weekEnd)) {
                timeMs += sessionsByDate[d]?.sumOf { it.durationMs } ?: 0L
                d = d.plusDays(1)
            }
            buckets.add(
                DayBucket(
                    dayStartMillis = weekStart.atStartOfDay(zone).toInstant().toEpochMilli(),
                    timeMs = timeMs
                )
            )
            weekStart = weekStart.plusWeeks(1)
        }
        return buckets
    }

    private fun buildMonthlyBuckets(
        start: LocalDate,
        end: LocalDate,
        sessionsByDate: Map<LocalDate, List<ReadingSession>>,
        zone: ZoneId
    ): List<DayBucket> {
        val buckets = mutableListOf<DayBucket>()
        var monthStart = start.withDayOfMonth(1)
        while (!monthStart.isAfter(end)) {
            val monthEnd = minOf(
                monthStart.withDayOfMonth(monthStart.lengthOfMonth()),
                end
            )
            var timeMs = 0L
            var d = monthStart
            while (!d.isAfter(monthEnd)) {
                timeMs += sessionsByDate[d]?.sumOf { it.durationMs } ?: 0L
                d = d.plusDays(1)
            }
            buckets.add(
                DayBucket(
                    dayStartMillis = monthStart.atStartOfDay(zone).toInstant().toEpochMilli(),
                    timeMs = timeMs
                )
            )
            monthStart = monthStart.plusMonths(1)
        }
        return buckets
    }

    private fun formatLabel(range: ActivityRange, start: LocalDate, end: LocalDate): String {
        val locale = java.util.Locale.getDefault()
        val fmt = java.time.format.DateTimeFormatter.ofPattern("d MMM", locale)
        val fmtMonthYear = java.time.format.DateTimeFormatter.ofPattern("LLLL yyyy", locale)
        return when (range) {
            ActivityRange.DAY -> fmt.format(start)
            ActivityRange.WEEK -> "${fmt.format(start)} – ${fmt.format(end)}"
            ActivityRange.MONTH -> fmtMonthYear.format(start)
            ActivityRange.THREE_MONTHS -> "${fmt.format(start)} – ${fmt.format(end)}"
            ActivityRange.YEAR -> "${fmt.format(start)} – ${fmt.format(end)}"
        }
    }

}
