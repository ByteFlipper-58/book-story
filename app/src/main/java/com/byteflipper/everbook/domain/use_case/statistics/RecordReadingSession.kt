/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.domain.use_case.statistics

import com.byteflipper.everbook.domain.repository.StatisticsRepository
import com.byteflipper.everbook.domain.statistics.ReadingSession
import javax.inject.Inject

class RecordReadingSession @Inject constructor(
    private val repository: StatisticsRepository
) {
    /**
     * Persists a finished reading session. Sessions shorter than [MIN_SESSION_MS]
     * are dropped — a quick open-and-close carries no useful statistics.
     */
    suspend fun execute(session: ReadingSession) {
        if (session.durationMs < MIN_SESSION_MS) return
        repository.recordSession(session)
    }

    companion object {
        const val MIN_SESSION_MS = 5_000L
    }
}
