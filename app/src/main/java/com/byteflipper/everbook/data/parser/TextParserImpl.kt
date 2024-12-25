package com.byteflipper.everbook.data.parser

import android.util.Log
import com.byteflipper.everbook.R
import com.byteflipper.everbook.data.parser.epub.EpubTextParser
import com.byteflipper.everbook.data.parser.fb2.Fb2TextParser
import com.byteflipper.everbook.data.parser.html.HtmlTextParser
import com.byteflipper.everbook.data.parser.pdf.PdfTextParser
import com.byteflipper.everbook.data.parser.txt.TxtTextParser
import com.byteflipper.everbook.domain.model.ChapterWithText
import com.byteflipper.everbook.domain.util.Resource
import com.byteflipper.everbook.domain.util.UIText
import java.io.File
import javax.inject.Inject

private const val TEXT_PARSER = "Text Parser"

class TextParserImpl @Inject constructor(
    private val txtTextParser: TxtTextParser,
    private val pdfTextParser: PdfTextParser,
    private val epubTextParser: EpubTextParser,
    private val fb2TextParser: Fb2TextParser,
    private val htmlTextParser: HtmlTextParser,
) : TextParser {
    override suspend fun parse(file: File): Resource<List<ChapterWithText>> {
        if (!file.exists()) {
            Log.e(TEXT_PARSER, "File does not exist.")
            return Resource.Error(
                UIText.StringResource(R.string.error_something_went_wrong_with_file)
            )
        }

        val fileFormat = ".${file.extension}".lowercase().trim()
        return when (fileFormat) {
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
                Resource.Error(UIText.StringResource(R.string.error_wrong_file_format))
            }
        }
    }
}







