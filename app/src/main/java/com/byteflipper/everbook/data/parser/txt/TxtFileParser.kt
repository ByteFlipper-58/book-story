package com.byteflipper.everbook.data.parser.txt

import com.byteflipper.everbook.R
import com.byteflipper.everbook.data.parser.FileParser
import com.byteflipper.everbook.domain.library.book.Book
import com.byteflipper.everbook.domain.library.book.BookWithCover
import com.byteflipper.everbook.domain.library.category.Category
import com.byteflipper.everbook.domain.ui.UIText
import java.io.File
import javax.inject.Inject

class TxtFileParser @Inject constructor() : FileParser {

    override suspend fun parse(file: File): BookWithCover? {
        return try {
            val title = file.nameWithoutExtension.trim()
            val author = UIText.StringResource(R.string.unknown_author)

            BookWithCover(
                book = Book(
                    title = title,
                    author = author,
                    description = null,
                    scrollIndex = 0,
                    scrollOffset = 0,
                    progress = 0f,
                    filePath = file.path,
                    lastOpened = null,
                    category = Category.entries[0],
                    coverImage = null
                ),
                coverImage = null
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}