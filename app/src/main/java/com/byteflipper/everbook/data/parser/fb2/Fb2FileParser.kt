/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.data.parser.fb2

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.w3c.dom.Document
import org.w3c.dom.Element
import com.byteflipper.everbook.R
import com.byteflipper.everbook.data.parser.FileParser
import com.byteflipper.everbook.domain.library.book.Book
import com.byteflipper.everbook.domain.library.book.BookWithCover
import com.byteflipper.everbook.domain.library.category.Category
import com.byteflipper.everbook.domain.ui.UIText
import java.io.File
import javax.inject.Inject
import javax.xml.parsers.DocumentBuilderFactory

class Fb2FileParser @Inject constructor() : FileParser {

    override suspend fun parse(file: File): BookWithCover? {
        return try {
            val factory = DocumentBuilderFactory.newInstance()
            val builder = factory.newDocumentBuilder()
            val document = withContext(Dispatchers.IO) {
                builder.parse(file)
            }

            val titleFromFile = extractElementContent(document, "book-title")
            val title = titleFromFile ?: file.nameWithoutExtension.trim()

            val authorFirstName = extractElementContent(document, "first-name")
            val authorLastName = extractElementContent(document, "last-name")
            val authorFromFile = StringBuilder()

            if (authorFirstName != null) {
                authorFromFile.append(
                    "$authorFirstName "
                )
            }
            if (authorLastName != null) {
                authorFromFile.append(
                    authorLastName
                )
            }

            val author = if (authorFromFile.isNotBlank()) {
                UIText.StringValue(authorFromFile.toString().trim())
            } else {
                UIText.StringResource(R.string.unknown_author)
            }

            val descriptionFromFile = extractElementContent(document, "annotation")

            BookWithCover(
                book = Book(
                    title = title,
                    author = author,
                    description = descriptionFromFile,
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

    private fun extractElementContent(document: Document, tagName: String): String? {
        val nodeList = document.getElementsByTagName(tagName)
        if (nodeList.length > 0) {
            val element = nodeList.item(0) as Element
            return element.textContent.trim()
        }
        return null
    }
}