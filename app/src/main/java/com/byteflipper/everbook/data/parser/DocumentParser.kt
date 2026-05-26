/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.data.parser

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.yield
import org.jsoup.nodes.Document
import com.byteflipper.everbook.domain.reader.ReaderText
import com.byteflipper.everbook.domain.reader.hasReadableReaderText
import com.byteflipper.everbook.presentation.core.util.clearAllMarkdown
import com.byteflipper.everbook.presentation.core.util.clearMarkdown
import com.byteflipper.everbook.presentation.core.util.containsVisibleText
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import javax.inject.Inject

private val BOLD_ITALIC_REGEX = Regex("""\*\*\*\s*(.*?)\s*\*\*\*""")
private val BOLD_REGEX = Regex("""\*\*\s*(.*?)\s*\*\*""")
private val ITALIC_REGEX = Regex("""_\s*(.*?)\s*_""")
private val IMAGE_TOKEN_REGEX = Regex("""\[\[(.*?)\|(.*?)]]""")

class DocumentParser @Inject constructor(
    private val markdownParser: MarkdownParser
) {
    /**
     * Parses document to get it's text.
     * Fixes issues such as manual line breaking in <p>.
     * Applies Markdown to the text: Bold(**), Italic(_), Section separator(---), and Links(a > href).
     *
     * @return Parsed text line by line with Markdown(all lines are not blank).
     */
    suspend fun parseDocument(
        document: Document,
        zipFile: ZipFile? = null,
        imageEntriesByName: Map<String, ZipEntry>? = null,
        includeChapter: Boolean = true,
        onChunk: ReaderTextChunkSink? = null
    ): List<ReaderText> {
        yield()

        val readerText = mutableListOf<ReaderText>()
        val chunkBuffer = ReaderTextChunkBuffer(onChunk = onChunk)
        var chapterAdded = false
        val imagesByName = imageEntriesByName.orEmpty()

        val body = document.selectFirst("body")
            .run { this ?: document.body() }
            .apply {
                // Remove manual line breaks from all <p>, <a>
                select("p").forEach { element ->
                    element.html(element.html().replace(Regex("\\n+"), " "))
                    element.append("\n")
                }
                select("a").forEach { element ->
                    element.html(element.html().replace(Regex("\\n+"), ""))
                }

                // Remove <head>'s title
                select("title").remove()

                // Markdown
                select("hr").append("\n---\n")
                select("b").append("**").prepend("**")
                select("h1").append("**").prepend("**")
                select("h2").append("**").prepend("**")
                select("h3").append("**").prepend("**")
                select("strong").append("**").prepend("**")
                select("em").append("_").prepend("_")
                select("a").forEach { element ->
                    var link = element.attr("href")
                    if (!link.startsWith("http") || element.wholeText().isBlank()) return@forEach

                    if (link.startsWith("http://")) {
                        link = link.replace("http://", "https://")
                    }

                    element.prepend("[")
                    element.append("]($link)")
                }

                // Image (<img>)
                select("img").forEach { element ->
                    val src = element.attr("src")
                        .trim()
                        .substringAfterLast('/')
                        .substringAfterLast(File.separator)
                        .lowercase()
                        .takeIf {
                            it.containsVisibleText() && imagesByName.containsKey(it)
                        } ?: return@forEach

                    val alt = element.attr("alt").trim().takeIf {
                        it.clearMarkdown().containsVisibleText()
                    } ?: src.substringBeforeLast(".")

                    element.append("\n[[$src|$alt]]\n")
                }

                // Image (<image>)
                select("image").forEach { element ->
                    val src = element.attr("xlink:href")
                        .trim()
                        .substringAfterLast('/')
                        .substringAfterLast(File.separator)
                        .lowercase()
                        .takeIf {
                            it.containsVisibleText() && imagesByName.containsKey(it)
                        } ?: return@forEach

                    val alt = src.substringBeforeLast(".")

                    element.append("\n[[$src|$alt]]\n")
                }
            }

        for (line in body.wholeText().lines()) {
            val formattedLine = line
                .replace(BOLD_ITALIC_REGEX, "_**$1**_")
                .replace(BOLD_REGEX, "**$1**")
                .replace(ITALIC_REGEX, "_$1_")
                .trim()

            if (line.containsVisibleText()) {
                when {
                    IMAGE_TOKEN_REGEX.matches(line) -> {
                        val trimmedLine = line.removeSurrounding("[[", "]]")
                        val src = trimmedLine.substringBefore("|")
                        val alt = "_${trimmedLine.substringAfter("|")}_"
                        val imageEntry = imagesByName[src] ?: continue

                        val image = try {
                            zipFile?.getImage(imageEntry)?.asImageBitmap()
                        } catch (e: Exception) {
                            e.printStackTrace()
                            null
                        } ?: continue

                        image.prepareToDraw()
                        val imageText = ReaderText.Image(
                            imageBitmap = image
                        )
                        readerText.add(imageText)
                        chunkBuffer.add(imageText)

                        val captionText = ReaderText.Text(
                            line = markdownParser.parse(alt),
                            source = alt
                        )
                        readerText.add(captionText)
                        chunkBuffer.add(captionText)
                    }

                    line == "---" || line == "***" -> {
                        readerText.add(ReaderText.Separator)
                        chunkBuffer.add(ReaderText.Separator)
                    }

                    else -> {
                        if (
                            !chapterAdded &&
                            formattedLine.clearAllMarkdown().containsVisibleText() &&
                            includeChapter
                        ) {
                            val chapter = ReaderText.Chapter(
                                title = formattedLine.clearAllMarkdown(),
                                nested = false
                            )
                            readerText.add(0, chapter)
                            chunkBuffer.add(chapter)
                            chapterAdded = true
                        } else if (
                            formattedLine.clearMarkdown().containsVisibleText()
                        ) {
                            val text = ReaderText.Text(
                                line = markdownParser.parse(formattedLine),
                                source = formattedLine
                            )
                            readerText.add(text)
                            chunkBuffer.add(text)
                        }
                    }
                }
            }
        }

        chunkBuffer.flush()

        yield()

        if (!readerText.hasReadableReaderText(requireChapter = includeChapter)) {
            return emptyList()
        }

        return readerText
    }

    /**
     * Getting bitmap from [ZipFile] with compression
     * that depends on the [imageEntry] size.
     */
    private fun ZipFile.getImage(imageEntry: ZipEntry): Bitmap? {
        fun getBitmapFromInputStream(compressionLevel: Int = 1): Bitmap? {
            return getInputStream(imageEntry).use { inputStream ->
                BitmapFactory.decodeStream(
                    inputStream,
                    null,
                    BitmapFactory.Options().apply {
                        inPreferredConfig = Bitmap.Config.RGB_565
                        inSampleSize = compressionLevel
                    }
                )
            }
        }


        val uncompressedBitmap = getBitmapFromInputStream() ?: return null
        return uncompressedBitmap
    }
}
