package com.byteflipper.everbook.domain.use_case.book

import com.byteflipper.everbook.domain.library.book.Book
import com.byteflipper.everbook.domain.repository.BookRepository
import javax.inject.Inject

class GetBookById @Inject constructor(
    private val repository: BookRepository
) {

    suspend fun execute(id: Int): Book? {
        return repository.getBooksById(listOf(id)).firstOrNull()
    }
}