package com.byteflipper.everbook.domain.use_case.history

import com.byteflipper.everbook.domain.history.History
import com.byteflipper.everbook.domain.repository.HistoryRepository
import javax.inject.Inject

class GetHistory @Inject constructor(
    private val repository: HistoryRepository
) {

    suspend fun execute(): List<History> {
        return repository.getHistory()
    }
}