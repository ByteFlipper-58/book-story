/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.domain.use_case.translation

import androidx.compose.ui.text.AnnotatedString
import com.byteflipper.everbook.domain.reader.ReaderText
import com.byteflipper.everbook.domain.translation.BookTranslationEntry
import com.byteflipper.everbook.domain.translation.BookTranslationEntryType
import java.security.MessageDigest
import javax.inject.Inject
import kotlin.math.min

private const val MAX_TRANSLATION_CHUNK_LENGTH = 3600

class BookTranslationPlanner @Inject constructor() {
    fun buildUnits(text: List<ReaderText>): List<BookTranslationUnit> =
        text.mapIndexedNotNull { index, entry ->
            when (entry) {
                is ReaderText.Chapter -> entry.title
                    .takeIf { it.isNotBlank() }
                    ?.let {
                        BookTranslationUnit(
                            readerTextIndex = index,
                            type = BookTranslationEntryType.CHAPTER,
                            text = it.trim()
                        )
                    }

                is ReaderText.Text -> entry.line.text
                    .takeIf { it.isNotBlank() }
                    ?.let {
                        BookTranslationUnit(
                            readerTextIndex = index,
                            type = BookTranslationEntryType.TEXT,
                            text = it.trim()
                        )
                    }

                is ReaderText.Image,
                is ReaderText.Math,
                ReaderText.Separator -> null
            }
        }

    fun fingerprint(text: List<ReaderText>): String {
        val digest = MessageDigest.getInstance("SHA-256")
        buildUnits(text).forEach { unit ->
            digest.update(
                "${unit.readerTextIndex}:${unit.type.name}:${unit.text}\n"
                    .toByteArray(Charsets.UTF_8)
            )
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    fun buildPieces(
        unit: BookTranslationUnit,
        maxPieceLength: Int
    ): BookTranslationUnitWork =
        BookTranslationUnitWork(
            unit = unit,
            pieces = unit.chunks(maxPieceLength).mapIndexed { index, text ->
                BookTranslationPiece(
                    unit = unit,
                    pieceIndex = index,
                    text = text
                )
            }
        )

    fun applyTranslation(
        original: List<ReaderText>,
        entries: List<BookTranslationEntry>
    ): List<ReaderText> {
        if (original.isEmpty() || entries.isEmpty()) return original

        val entriesByIndex = entries.associateBy { it.readerTextIndex }
        return original.mapIndexed { index, entry ->
            val translated = entriesByIndex[index]
                ?.translatedText
                ?.trim()
                ?.takeIf { it.isNotBlank() }
                ?: return@mapIndexed entry

            when (entry) {
                is ReaderText.Chapter -> entry.copy(title = translated)
                is ReaderText.Text -> ReaderText.Text(
                    line = AnnotatedString(translated),
                    source = translated
                )

                is ReaderText.Image,
                is ReaderText.Math,
                ReaderText.Separator -> entry
            }
        }
    }
}

data class BookTranslationUnit(
    val readerTextIndex: Int,
    val type: BookTranslationEntryType,
    val text: String
) {
    fun chunks(maxLength: Int = MAX_TRANSLATION_CHUNK_LENGTH): List<String> {
        if (text.length <= maxLength) return listOf(text)

        val chunks = mutableListOf<String>()
        var index = 0
        while (index < text.length) {
            val maxEnd = min(index + maxLength, text.length)
            val splitAt = findSplitPoint(
                index = index,
                maxEnd = maxEnd,
                maxLength = maxLength
            )
            chunks += text.substring(index, splitAt).trim()
            index = splitAt
            while (index < text.length && text[index].isWhitespace()) index++
        }
        return chunks.filter { it.isNotBlank() }
    }

    private fun findSplitPoint(
        index: Int,
        maxEnd: Int,
        maxLength: Int
    ): Int {
        if (maxEnd >= text.length) return text.length

        val punctuationSplit = text.lastIndexOfAny(
            chars = charArrayOf('.', '!', '?', ';', ':', '\n'),
            startIndex = maxEnd - 1
        ).takeIf { it > index + maxLength / 2 }
        if (punctuationSplit != null) return punctuationSplit + 1

        val whitespaceSplit = text.lastIndexOf(' ', startIndex = maxEnd - 1)
            .takeIf { it > index + maxLength / 2 }
        if (whitespaceSplit != null) return whitespaceSplit

        return maxEnd
    }
}

data class BookTranslationUnitWork(
    val unit: BookTranslationUnit,
    val pieces: List<BookTranslationPiece>
)

data class BookTranslationPiece(
    val unit: BookTranslationUnit,
    val pieceIndex: Int,
    val text: String
)
