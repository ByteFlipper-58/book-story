/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.data.repository

import com.byteflipper.everbook.data.local.room.BookDao
import com.byteflipper.everbook.data.mapper.history.HistoryMapper
import com.byteflipper.everbook.domain.history.History
import com.byteflipper.everbook.domain.repository.HistoryRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * History repository.
 * Manages all [History] related work.
 */
@Singleton
class HistoryRepositoryImpl @Inject constructor(
    private val database: BookDao,

    private val historyMapper: HistoryMapper,
) : HistoryRepository {

    /**
     * Insert history in database.
     */
    override suspend fun insertHistory(history: History) {
        database.insertHistory(
            listOf(
                historyMapper.toHistoryEntity(
                    history
                )
            )
        )
    }

    /**
     * Get all history from database.
     */
    override suspend fun getHistory(): List<History> {
        return database.getHistory().map {
            historyMapper.toHistory(
                it
            )
        }
    }

    /**
     * Get latest history of the matching [bookId].
     */
    override suspend fun getLatestBookHistory(bookId: Int): History? {
        val history = database.getLatestHistoryForBook(bookId)
        return history?.let { historyMapper.toHistory(it) }
    }

    /**
     * Delete whole history.
     */
    override suspend fun deleteWholeHistory() {
        database.deleteWholeHistory()
    }

    /**
     * Delete all history of the matching [bookId].
     */
    override suspend fun deleteBookHistory(bookId: Int) {
        database.deleteBookHistory(bookId)
    }

    /**
     * Delete specific history item.
     */
    override suspend fun deleteHistory(history: History) {
        database.deleteHistory(
            listOf(
                historyMapper.toHistoryEntity(
                    history
                )
            )
        )
    }
}