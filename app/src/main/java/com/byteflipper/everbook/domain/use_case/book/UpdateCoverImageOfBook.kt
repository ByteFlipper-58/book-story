package com.byteflipper.everbook.domain.use_case.book

import com.byteflipper.everbook.domain.library.book.Book
import com.byteflipper.everbook.domain.repository.BookRepository
import com.byteflipper.everbook.domain.util.CoverImage
import javax.inject.Inject

class UpdateCoverImageOfBook @Inject constructor(
    private val repository: BookRepository
) {

    suspend fun execute(bookWithOldCover: Book, newCoverImage: CoverImage?) {
        repository.updateCoverImageOfBook(bookWithOldCover, newCoverImage)
    }
}