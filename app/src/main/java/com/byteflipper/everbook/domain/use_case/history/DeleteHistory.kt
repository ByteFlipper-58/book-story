package com.byteflipper.everbook.domain.use_case.history

import com.byteflipper.everbook.domain.history.History
import com.byteflipper.everbook.domain.repository.HistoryRepository
import javax.inject.Inject

class DeleteHistory @Inject constructor(
    private val repository: HistoryRepository
) {

    suspend fun execute(history: History) {
        repository.deleteHistory(history)
    }
}