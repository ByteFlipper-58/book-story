package com.byteflipper.book_story.domain.use_case.history

import com.byteflipper.book_story.domain.model.History
import com.byteflipper.book_story.domain.repository.HistoryRepository
import javax.inject.Inject

class GetHistory @Inject constructor(
    private val repository: HistoryRepository
) {

    suspend fun execute(): List<History> {
        return repository.getHistory()
    }
}