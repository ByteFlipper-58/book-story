package com.byteflipper.everbook.data.parser.pdf

import android.app.Application
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.byteflipper.everbook.R
import com.byteflipper.everbook.data.parser.FileParser
import com.byteflipper.everbook.domain.library.book.Book
import com.byteflipper.everbook.domain.library.book.BookWithCover
import com.byteflipper.everbook.domain.library.category.Category
import com.byteflipper.everbook.domain.ui.UIText
import java.io.File
import javax.inject.Inject

class PdfFileParser @Inject constructor(
    private val application: Application
) : FileParser {

    override suspend fun parse(file: File): BookWithCover? {
        return try {
            PDFBoxResourceLoader.init(application)

            val document = PDDocument.load(file)

            val title = document.documentInformation.title ?: file.nameWithoutExtension.trim()
            val author = document.documentInformation.author.run {
                if (isNullOrBlank()) UIText.StringResource(R.string.unknown_author)
                else UIText.StringValue(this)
            }
            val description = document.documentInformation.subject

            document.close()

            BookWithCover(
                book = Book(
                    title = title,
                    author = author,
                    description = description,
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