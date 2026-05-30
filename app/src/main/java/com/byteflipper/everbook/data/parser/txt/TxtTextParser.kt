/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.data.parser.txt

import android.util.Log
import com.byteflipper.everbook.data.parser.ReaderTextChunkBuffer
import com.byteflipper.everbook.data.parser.ReaderTextChunkSink
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import com.byteflipper.everbook.data.parser.MarkdownParser
import com.byteflipper.everbook.data.parser.TextParser
import com.byteflipper.everbook.domain.file.CachedFile
import com.byteflipper.everbook.domain.reader.ReaderText
import com.byteflipper.everbook.domain.reader.hasReadableReaderText
import com.byteflipper.everbook.presentation.core.util.clearAllMarkdown
import javax.inject.Inject

private const val TXT_TAG = "TXT Parser"

class TxtTextParser @Inject constructor(
    private val markdownParser: MarkdownParser
) : TextParser {

    override suspend fun parse(
        cachedFile: CachedFile,
        onChunk: ReaderTextChunkSink?
    ): List<ReaderText> {
        Log.i(TXT_TAG, "Started TXT parsing: ${cachedFile.name}.")

        return try {
            val readerText = mutableListOf<ReaderText>()
            val chunkBuffer = ReaderTextChunkBuffer(onChunk = onChunk)
            var chapterAdded = false

            withContext(Dispatchers.IO) {
                cachedFile.openInputStream()?.bufferedReader()?.useLines { lines ->
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

            if (!readerText.hasReadableReaderText()) {
                Log.e(TXT_TAG, "Could not extract text from TXT.")
                return emptyList()
            }

            Log.i(TXT_TAG, "Successfully finished TXT parsing.")
            readerText
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
