package com.byteflipper.everbook.domain.use_case.book

import com.byteflipper.everbook.domain.library.book.Book
import com.byteflipper.everbook.domain.repository.BookRepository
import javax.inject.Inject

class GetBooks @Inject constructor(
    private val repository: BookRepository
) {

    suspend fun execute(query: String): List<Book> {
        return repository.getBooks(query)
    }
}