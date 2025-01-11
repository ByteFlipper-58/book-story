package com.byteflipper.everbook.data.mapper.history

import com.byteflipper.everbook.data.local.dto.HistoryEntity
import com.byteflipper.everbook.domain.history.History
import javax.inject.Inject

class HistoryMapperImpl @Inject constructor() : HistoryMapper {
    override suspend fun toHistoryEntity(history: History): HistoryEntity {
        return HistoryEntity(
            id = history.id,
            bookId = history.bookId,
            time = history.time
        )
    }

    override suspend fun toHistory(historyEntity: HistoryEntity): History {
        return History(
            historyEntity.id,
            bookId = historyEntity.bookId,
            book = null,
            time = historyEntity.time
        )
    }
}