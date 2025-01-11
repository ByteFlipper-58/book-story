package com.byteflipper.everbook.domain.use_case.book

import com.byteflipper.everbook.domain.reader.ReaderText
import com.byteflipper.everbook.domain.repository.BookRepository
import javax.inject.Inject

class GetText @Inject constructor(
    private val repository: BookRepository
) {

    suspend fun execute(bookId: Int): List<ReaderText> {
        return repository.getBookText(bookId = bookId)
    }
}