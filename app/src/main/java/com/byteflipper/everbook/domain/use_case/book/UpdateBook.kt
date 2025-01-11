package com.byteflipper.everbook.domain.use_case.book

import com.byteflipper.everbook.domain.library.book.Book
import com.byteflipper.everbook.domain.repository.BookRepository
import javax.inject.Inject

class UpdateBook @Inject constructor(
    private val repository: BookRepository
) {

    suspend fun execute(book: Book) {
        repository.updateBook(book)
    }
}