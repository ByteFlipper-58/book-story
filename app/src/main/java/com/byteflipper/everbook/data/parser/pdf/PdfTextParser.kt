/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.data.parser.pdf

import android.app.Application
import android.util.Log
import com.byteflipper.everbook.data.parser.ReaderTextChunkBuffer
import com.byteflipper.everbook.data.parser.ReaderTextChunkSink
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.io.MemoryUsageSetting
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import kotlinx.coroutines.yield
import com.byteflipper.everbook.data.parser.MarkdownParser
import com.byteflipper.everbook.data.parser.TextParser
import com.byteflipper.everbook.domain.file.CachedFile
import com.byteflipper.everbook.domain.reader.ReaderText
import com.byteflipper.everbook.domain.reader.hasReadableReaderText
import com.byteflipper.everbook.presentation.core.util.clearAllMarkdown
import javax.inject.Inject

private const val PDF_TAG = "PDF Parser"
private const val PDF_PARAGRAPH_START = "</br>"
private val PDF_PARAGRAPH_SPLIT_REGEX = Regex("$PDF_PARAGRAPH_START|\\n")

class PdfTextParser @Inject constructor(
    private val markdownParser: MarkdownParser,
    private val application: Application
) : TextParser {

    override suspend fun parse(
        cachedFile: CachedFile,
        onChunk: ReaderTextChunkSink?
    ): List<ReaderText> {
        Log.i(PDF_TAG, "Started PDF parsing: ${cachedFile.name}.")

        // Mirror of the importer guard: never load an oversized PDF into PDFBox (OOM risk).
        // Returning empty makes the reader fall back to native page rendering.
        if (cachedFile.size > PDF_TEXT_PARSE_MAX_BYTES) {
            Log.w(PDF_TAG, "PDF too large for text extraction; using native mode.")
            return emptyList()
        }

        return try {
            yield()

            PDFBoxResourceLoader.init(application)

            yield()

            val pdfStripper = PDFTextStripper()
            pdfStripper.paragraphStart = PDF_PARAGRAPH_START
            val readerText = mutableListOf<ReaderText>()
            val chunkBuffer = ReaderTextChunkBuffer(onChunk = onChunk)
            var chapterAdded = false

            PDDocument.load(
                cachedFile.openInputStream(),
                MemoryUsageSetting.setupTempFileOnly().setTempDir(application.cacheDir)
            ).use {
                for (pageIndex in 1..it.numberOfPages) {
                    yield()
                    pdfStripper.startPage = pageIndex
                    pdfStripper.endPage = pageIndex

                    val pageText = pdfStripper.getText(it)
                        .replace("\r", "")
                    val compactText = normalizePdfTextStageOne(pageText)
                    val paragraphLines = normalizePdfTextStageTwo(text = compactText)
                    val lines = normalizePdfTextStageThree(paragraphLines)

                    for (line in lines) {
                        if (line.isBlank()) continue

                        when (line) {
                            "***", "---" -> {
                                readerText.add(ReaderText.Separator)
                                chunkBuffer.add(ReaderText.Separator)
                            }

                            else -> {
                                val chapterTitle = line.clearAllMarkdown()

                                if (!chapterAdded && chapterTitle.isNotBlank()) {
                                    val chapter = ReaderText.Chapter(
                                        title = chapterTitle,
                                        nested = false
                                    )
                                    readerText.add(0, chapter)
                                    chunkBuffer.add(chapter)
                                    chapterAdded = true
                                } else {
                                    val text = ReaderText.Text(
                                        line = markdownParser.parse(line),
                                        source = line
                                    )
                                    readerText.add(text)
                                    chunkBuffer.add(text)
                                }
                            }
                        }
                    }
                }
            }

            chunkBuffer.flush()
            yield()

            if (!chapterAdded) {
                readerText.firstOrNull { it is ReaderText.Text }?.let { firstText ->
                    val chapterTitle = (firstText as ReaderText.Text).source.clearAllMarkdown()
                    if (chapterTitle.isNotBlank()) {
                        readerText.add(
                            0,
                            ReaderText.Chapter(
                                title = chapterTitle,
                                nested = false
                            )
                        )
                        chapterAdded = true
                    }
                }
            }

            if (!readerText.hasReadableReaderText()) {
                Log.e(PDF_TAG, "Could not extract text from PDF.")
                return emptyList()
            }

            Log.i(PDF_TAG, "Successfully finished PDF parsing.")
            readerText
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private fun normalizePdfTextStageOne(text: String): String {
        val builder = StringBuilder(text.length)
        var previousWasSpace = false
        text.forEach { char ->
            if (char == ' ') {
                if (!previousWasSpace) builder.append(char)
                previousWasSpace = true
            } else {
                builder.append(char)
                previousWasSpace = false
            }
        }
        return builder.toString()
    }

    private fun normalizePdfTextStageTwo(text: String): List<String> {
        return text.split(PDF_PARAGRAPH_SPLIT_REGEX)
            .filter { it.isNotBlank() }
    }

    private fun normalizePdfTextStageThree(unformattedLines: List<String>): List<String> {
        val lines = mutableListOf<String>()

        unformattedLines.forEachIndexed { index, string ->
            try {
                val line = string.trim()

                if (index == 0) {
                    lines.add(line)
                    return@forEachIndexed
                }

                if (line.all { it.isDigit() }) {
                    return@forEachIndexed
                }

                if (line.first().isLowerCase()) {
                    val currentLine = lines[lines.lastIndex]

                    if (currentLine.length > 1 && currentLine.last() == '-') {
                        if (currentLine[currentLine.lastIndex - 1].isLowerCase()) {
                            lines[lines.lastIndex] = currentLine.dropLast(1) + line
                            return@forEachIndexed
                        }
                    }

                    lines[lines.lastIndex] += " $line"
                    return@forEachIndexed
                }

                if (line.first().isUpperCase() || line.first().isDigit()) {
                    lines.add(line)
                    return@forEachIndexed
                }

                if (line.first().isLetter()) {
                    lines[lines.lastIndex] += " $line"
                    return@forEachIndexed
                }
            } catch (e: Exception) {
                e.printStackTrace()
                return@forEachIndexed
            }
        }

        return lines
    }
}
