/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.data.parser.html

import android.util.Log
import kotlinx.coroutines.yield
import org.jsoup.Jsoup
import com.byteflipper.everbook.data.parser.DocumentParser
import com.byteflipper.everbook.data.parser.TextParser
import com.byteflipper.everbook.domain.reader.ReaderText
import java.io.File
import javax.inject.Inject

private const val HTML_TAG = "HTML Parser"

class HtmlTextParser @Inject constructor(
    private val documentParser: DocumentParser
) : TextParser {

    override suspend fun parse(file: File): List<ReaderText> {
        Log.i(HTML_TAG, "Started HTML parsing: ${file.name}.")

        return try {
            val readerText = documentParser.run {
                Jsoup.parse(file).parseDocument()
            }

            yield()

            if (
                readerText.filterIsInstance<ReaderText.Text>().isEmpty() ||
                readerText.filterIsInstance<ReaderText.Chapter>().isEmpty()
            ) {
                Log.e(HTML_TAG, "Could not extract text from HTML.")
                return emptyList()
            }

            Log.i(HTML_TAG, "Successfully finished HTML parsing.")
            readerText
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}