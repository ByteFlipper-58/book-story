package com.byteflipper.everbook.data.mapper.book

import android.net.Uri
import com.byteflipper.everbook.R
import com.byteflipper.everbook.data.local.dto.BookEntity
import com.byteflipper.everbook.domain.library.book.Book
import com.byteflipper.everbook.domain.ui.UIText
import javax.inject.Inject

class BookMapperImpl @Inject constructor() : BookMapper {
    override suspend fun toBookEntity(book: Book): BookEntity {
        return BookEntity(
            id = book.id,
            title = book.title,
            filePath = book.filePath,
            scrollIndex = book.scrollIndex,
            scrollOffset = book.scrollOffset,
            progress = book.progress,
            author = book.author.getAsString(),
            description = book.description,
            image = book.coverImage?.toString(),
            category = book.category
        )
    }

    override suspend fun toBook(bookEntity: BookEntity): Book {
        return Book(
            id = bookEntity.id,
            title = bookEntity.title,
            author = bookEntity.author?.let { UIText.StringValue(it) } ?: UIText.StringResource(
                R.string.unknown_author
            ),
            description = bookEntity.description,
            scrollIndex = bookEntity.scrollIndex,
            scrollOffset = bookEntity.scrollOffset,
            progress = bookEntity.progress,
            filePath = bookEntity.filePath,
            lastOpened = null,
            category = bookEntity.category,
            coverImage = if (bookEntity.image != null) Uri.parse(bookEntity.image) else null
        )
    }
}