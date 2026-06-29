/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.data.parser.pdf

import android.app.Application
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.io.MemoryUsageSetting
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.byteflipper.everbook.R
import com.byteflipper.everbook.data.parser.FileParser
import com.byteflipper.everbook.domain.file.CachedFile
import com.byteflipper.everbook.domain.library.book.Book
import com.byteflipper.everbook.domain.library.book.BookWithCover
import com.byteflipper.everbook.domain.library.category.Category
import com.byteflipper.everbook.domain.reader.PdfReadingMode
import com.byteflipper.everbook.domain.ui.UIText
import javax.inject.Inject

/**
 * Backstop for text extraction. PDFs are loaded with a disk-backed scratch file
 * ([MemoryUsageSetting.setupTempFileOnly]) so memory stays bounded regardless of file size — but a
 * very large file is still slow to parse page-by-page on a phone. Above this size we skip text
 * extraction and import as a native-only book (read via the page renderer). Below it, text is
 * extracted normally, so a large text+image PDF keeps both reading modes. Shared by
 * [PdfFileParser] (import) and [PdfTextParser] (on-open text).
 */
internal const val PDF_TEXT_PARSE_MAX_BYTES = 300L * 1024 * 1024

class PdfFileParser @Inject constructor(
    private val application: Application
) : FileParser {

    override suspend fun parse(cachedFile: CachedFile): BookWithCover? {
        // Too large to safely load into PDFBox — import as a native-only book. PdfRenderer
        // streams pages from a file descriptor and never loads the whole document into memory.
        if (cachedFile.size > PDF_TEXT_PARSE_MAX_BYTES) {
            return BookWithCover(
                book = Book(
                    title = cachedFile.name.substringBeforeLast(".").trim(),
                    author = UIText.StringResource(R.string.unknown_author),
                    description = null,
                    scrollIndex = 0,
                    scrollOffset = 0,
                    progress = 0f,
                    filePath = cachedFile.path,
                    lastOpened = null,
                    category = Category.entries[0],
                    coverImage = null,
                    pdfReadingMode = PdfReadingMode.ORIGINAL_PDF,
                    pdfTextModeAvailable = false
                ),
                coverImage = null
            )
        }

        return try {
            PDFBoxResourceLoader.init(application)
            // Disk-backed load: scratch data goes to a temp file in the app cache, so memory
            // stays bounded even for large files (no full in-RAM copy → no OutOfMemory).
            val document = PDDocument.load(
                cachedFile.openInputStream(),
                MemoryUsageSetting.setupTempFileOnly().setTempDir(application.cacheDir)
            )

            val title = document.documentInformation.title
                ?: cachedFile.name.substringBeforeLast(".").trim()
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
                    filePath = cachedFile.path,
                    lastOpened = null,
                    category = Category.entries[0],
                    coverImage = null
                ),
                coverImage = null
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } catch (e: OutOfMemoryError) {
            e.printStackTrace()
            null
        }
    }
}