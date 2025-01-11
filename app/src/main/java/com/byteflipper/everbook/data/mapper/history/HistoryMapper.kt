package com.byteflipper.everbook.data.mapper.history

import com.byteflipper.everbook.data.local.dto.HistoryEntity
import com.byteflipper.everbook.domain.history.History

interface HistoryMapper {
    suspend fun toHistoryEntity(history: History): HistoryEntity

    suspend fun toHistory(historyEntity: HistoryEntity): History
}