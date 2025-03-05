/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.domain.use_case.history

import com.byteflipper.everbook.domain.history.History
import com.byteflipper.everbook.domain.repository.HistoryRepository
import javax.inject.Inject

class InsertHistory @Inject constructor(
    private val repository: HistoryRepository
) {

    suspend fun execute(history: History) {
        repository.insertHistory(history)
    }
}