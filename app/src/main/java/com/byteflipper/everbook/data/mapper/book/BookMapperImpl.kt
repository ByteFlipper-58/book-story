package com.byteflipper.everbook.data.mapper.book

import android.net.Uri
import com.byteflipper.everbook.R
import com.byteflipper.everbook.data.local.dto.BookEntity
import com.byteflipper.everbook.domain.model.Book
import com.byteflipper.everbook.domain.util.UIText
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
            textPath = book.textPath,
            description = book.description,
            image = book.coverImage?.toString(),
            category = book.category,
            chapters = book.chapters
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
            textPath = bookEntity.textPath,
            filePath = bookEntity.filePath,
            lastOpened = null,
            category = bookEntity.category,
            coverImage = if (bookEntity.image != null) Uri.parse(bookEntity.image) else null,
            chapters = bookEntity.chapters
        )
    }
}