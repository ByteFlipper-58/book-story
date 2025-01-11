package com.byteflipper.everbook.domain.use_case.book

import com.byteflipper.everbook.domain.library.book.BookWithCover
import com.byteflipper.everbook.domain.repository.BookRepository
import javax.inject.Inject

class InsertBook @Inject constructor(
    private val repository: BookRepository
) {

    suspend fun execute(bookWithCover: BookWithCover): Boolean {
        return repository.insertBook(bookWithCover)
    }
}