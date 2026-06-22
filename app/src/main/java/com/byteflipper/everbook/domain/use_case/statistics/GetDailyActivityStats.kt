/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.domain.use_case.statistics

import com.byteflipper.everbook.domain.repository.StatisticsRepository
import com.byteflipper.everbook.domain.statistics.ActivityCalendarDay
import com.byteflipper.everbook.domain.statistics.DailyActivityBookStats
import com.byteflipper.everbook.domain.statistics.DailyActivityStats
import com.byteflipper.everbook.domain.statistics.DayBucket
import com.byteflipper.everbook.domain.statistics.ReadingSession
import com.byteflipper.everbook.domain.statistics.alignToWeekStart
import com.byteflipper.everbook.domain.use_case.book.GetBooks
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject

class GetDailyActivityStats @Inject constructor(
    private val repository: StatisticsRepository,
    private val getBooks: GetBooks
) {
    suspend fun execute(
        date: LocalDate,
        calendarMonth: YearMonth = YearMonth.from(date),
        weekStartsMonday: Boolean = true
    ): DailyActivityStats {
        val zone = ZoneId.systemDefault()
        val sessions = repository.getSessions()
        val booksById = getBooks.execute("").associateBy { it.id }
        val sessionsByDate = sessions.groupBy { it.localDate(zone) }
        val daySessions = sessionsByDate[date].orEmpty()
        val weekStart = alignToWeekStart(date, weekStartsMonday)

        val dayBooks = daySessions
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

        val hourlyBuckets = buildHourlyBuckets(date, daySessions, zone)

        return DailyActivityStats(
            date = date,
            timeMs = daySessions.sumOf { it.durationMs },
            pagesRead = daySessions.sumOf { it.pagesRead },
            bookCount = daySessions.map { it.bookId }.distinct().size,
            books = dayBooks,
            hourlyBuckets = hourlyBuckets,
            weekDays = (0..6).map { offset ->
                val day = weekStart.plusDays(offset.toLong())
                sessionsByDate.toCalendarDay(day)
            },
            calendarMonth = calendarMonth,
            calendarDays = buildMonthDays(calendarMonth, sessionsByDate, weekStartsMonday)
        )
    }

    private fun buildMonthDays(
        month: YearMonth,
        sessionsByDate: Map<LocalDate, List<ReadingSession>>,
        weekStartsMonday: Boolean
    ): List<ActivityCalendarDay> {
        val first = alignToWeekStart(month.atDay(1), weekStartsMonday)
        val last = alignToWeekStart(month.atEndOfMonth(), weekStartsMonday).plusDays(6)
        val days = java.time.temporal.ChronoUnit.DAYS.between(first, last).toInt()
        return (0..days).map { offset ->
            sessionsByDate.toCalendarDay(first.plusDays(offset.toLong()))
        }
    }

    private fun Map<LocalDate, List<ReadingSession>>.toCalendarDay(
        date: LocalDate
    ): ActivityCalendarDay {
        val daySessions = this[date].orEmpty()
        return ActivityCalendarDay(
            date = date,
            timeMs = daySessions.sumOf { it.durationMs },
            pagesRead = daySessions.sumOf { it.pagesRead },
            bookCount = daySessions.map { it.bookId }.distinct().size,
            isActive = daySessions.isNotEmpty()
        )
    }

    private fun buildHourlyBuckets(
        date: LocalDate,
        sessions: List<ReadingSession>,
        zone: ZoneId
    ): List<DayBucket> {
        val hourMs = LongArray(24)
        for (session in sessions) {
            val hour = Instant.ofEpochMilli(session.startTime).atZone(zone).hour
            hourMs[hour] += session.durationMs
        }
        val dayStart = date.atStartOfDay(zone).toInstant().toEpochMilli()
        return (0 until 24).map { h ->
            DayBucket(
                dayStartMillis = dayStart + h * 3_600_000L,
                timeMs = hourMs[h]
            )
        }
    }

    private fun ReadingSession.localDate(zone: ZoneId): LocalDate {
        return Instant.ofEpochMilli(startTime).atZone(zone).toLocalDate()
    }

}
