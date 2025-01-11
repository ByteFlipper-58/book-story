package com.byteflipper.everbook.data.parser.html

import org.jsoup.Jsoup
import com.byteflipper.everbook.R
import com.byteflipper.everbook.data.parser.FileParser
import com.byteflipper.everbook.domain.library.book.Book
import com.byteflipper.everbook.domain.library.book.BookWithCover
import com.byteflipper.everbook.domain.library.category.Category
import com.byteflipper.everbook.domain.ui.UIText
import java.io.File
import javax.inject.Inject

class HtmlFileParser @Inject constructor() : FileParser {

    override suspend fun parse(file: File): BookWithCover? {
        return try {
            val document = Jsoup.parse(file)

            val title = document.select("head > title").text().trim().run {
                ifBlank {
                    file.nameWithoutExtension.trim()
                }
            }

            BookWithCover(
                book = Book(
                    title = title,
                    author = UIText.StringResource(R.string.unknown_author),
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