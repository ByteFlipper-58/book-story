package com.byteflipper.everbook.data.mapper.book

import com.byteflipper.everbook.data.local.dto.BookEntity
import com.byteflipper.everbook.domain.library.book.Book

interface BookMapper {
    suspend fun toBookEntity(book: Book): BookEntity

    suspend fun toBook(bookEntity: BookEntity): Book
}