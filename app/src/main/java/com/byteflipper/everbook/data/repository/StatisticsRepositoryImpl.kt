/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.data.repository

import com.byteflipper.everbook.data.local.dto.ReadingSessionEntity
import com.byteflipper.everbook.data.local.room.BookDao
import com.byteflipper.everbook.domain.repository.StatisticsRepository
import com.byteflipper.everbook.domain.statistics.ReadingSession
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Reading statistics repository. Stores and reads [ReadingSession]s.
 */
@Singleton
class StatisticsRepositoryImpl @Inject constructor(
    private val database: BookDao
) : StatisticsRepository {

    override suspend fun recordSession(session: ReadingSession) {
        database.insertSession(
            ReadingSessionEntity(
                id = session.id,
                bookId = session.bookId,
                startTime = session.startTime,
                endTime = session.endTime,
                progressStart = session.progressStart,
                progressEnd = session.progressEnd,
                pagesRead = session.pagesRead
            )
        )
    }

    override suspend fun getSessions(): List<ReadingSession> {
        return database.getAllSessions().map {
            ReadingSession(
                id = it.id,
                bookId = it.bookId,
                startTime = it.startTime,
                endTime = it.endTime,
                progressStart = it.progressStart,
                progressEnd = it.progressEnd,
                pagesRead = it.pagesRead
            )
        }
    }

    override suspend fun deleteBookSessions(bookId: Int) {
        database.deleteBookSessions(bookId)
    }

    override suspend fun deleteAllSessions() {
        database.deleteAllSessions()
    }
}
