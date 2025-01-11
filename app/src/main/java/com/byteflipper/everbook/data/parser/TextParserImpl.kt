package com.byteflipper.everbook.data.parser

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.byteflipper.everbook.data.parser.epub.EpubTextParser
import com.byteflipper.everbook.data.parser.fb2.Fb2TextParser
import com.byteflipper.everbook.data.parser.html.HtmlTextParser
import com.byteflipper.everbook.data.parser.pdf.PdfTextParser
import com.byteflipper.everbook.data.parser.txt.TxtTextParser
import com.byteflipper.everbook.domain.reader.ReaderText
import java.io.File
import javax.inject.Inject

private const val TEXT_PARSER = "Text Parser"

class TextParserImpl @Inject constructor(
    // Markdown parser (Markdown)
    private val txtTextParser: TxtTextParser,
    private val pdfTextParser: PdfTextParser,
    private val fb2TextParser: Fb2TextParser,

    // Document parser (HTML+Markdown)
    private val epubTextParser: EpubTextParser,
    private val htmlTextParser: HtmlTextParser
) : TextParser {
    override suspend fun parse(file: File): List<ReaderText> {
        if (!file.exists()) {
            Log.e(TEXT_PARSER, "File does not exist.")
            return emptyList()
        }

        val fileFormat = ".${file.extension}".lowercase().trim()
        return withContext(Dispatchers.IO) {
            when (fileFormat) {
                ".pdf" -> {
                    pdfTextParser.parse(file)
                }

                ".epub" -> {
                    epubTextParser.parse(file)
                }

                ".txt" -> {
                    txtTextParser.parse(file)
                }

                ".fb2" -> {
                    fb2TextParser.parse(file)
                }

                ".zip" -> {
                    epubTextParser.parse(file)
                }

                ".html" -> {
                    htmlTextParser.parse(file)
                }

                ".htm" -> {
                    htmlTextParser.parse(file)
                }

                ".md" -> {
                    htmlTextParser.parse(file)
                }

                else -> {
                    Log.e(TEXT_PARSER, "Wrong file format, could not find supported extension.")
                    emptyList()
                }
            }
        }
    }
}