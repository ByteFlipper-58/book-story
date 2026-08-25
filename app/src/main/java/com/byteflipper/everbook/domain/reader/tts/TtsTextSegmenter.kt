/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.domain.reader.tts

/** Character range of one sentence inside a paragraph. */
data class TtsSentence(
    val start: Int,
    val end: Int
)

/**
 * Splits a paragraph into speakable sentences.
 *
 * Rules beyond "break after . ! ?":
 * - closing quotes/brackets that follow the terminator stay with the sentence;
 * - CJK terminators (。！？…) break as well, and need no trailing whitespace;
 * - a terminator does not break when it is part of an ellipsis, an ordinal/decimal number,
 *   a single-letter initial ("J. R. R.") or a known abbreviation;
 * - sentences longer than the engine limit are split again on the nearest clause boundary.
 */
object TtsTextSegmenter {

    private const val MIN_SPLIT_LENGTH = 24

    private val ABBREVIATIONS = setOf(
        // Latin
        "mr", "mrs", "ms", "dr", "prof", "st", "sr", "jr", "vs", "etc", "e.g", "i.e", "no",
        "fig", "op", "cf", "al", "inc", "ltd", "co", "approx", "dept", "est", "min", "max",
        // Cyrillic
        "г", "гг", "в", "вв", "т", "тт", "стр", "рис", "табл", "им", "ул", "пр", "просп",
        "обл", "руб", "коп", "тыс", "млн", "млрд", "проф", "акад", "др", "тд", "тп", "см",
        "напр", "ок", "изд", "перев", "мин", "макс"
    )

    private val CJK_TERMINATORS = charArrayOf('。', '！', '？', '．', '…', '‥')
    private val LATIN_TERMINATORS = charArrayOf('.', '!', '?')
    private val TRAILING_CHARS = charArrayOf(
        '"', '\'', '»', '”', '’', ')', ']', '}', '›', '”', '」', '』', '“'
    )
    private val CLAUSE_BREAKS = charArrayOf(';', ':', ',', '—', '–', '、', '，', '；', '：')

    /**
     * Sentence ranges of [text], each trimmed of surrounding whitespace and never longer than
     * [maxLength]. Returns an empty list when there is nothing to pronounce.
     */
    fun segment(text: String, maxLength: Int): List<TtsSentence> {
        if (text.isBlank()) return emptyList()
        val limit = maxLength.coerceAtLeast(MIN_SPLIT_LENGTH)

        val sentences = mutableListOf<TtsSentence>()
        var start = 0
        var index = 0
        while (index < text.length) {
            val char = text[index]
            val isTerminator = char in LATIN_TERMINATORS || char in CJK_TERMINATORS
            if (!isTerminator) {
                index++
                continue
            }

            var end = index + 1
            while (end < text.length && (text[end] in LATIN_TERMINATORS ||
                        text[end] in CJK_TERMINATORS)
            ) {
                end++
            }
            while (end < text.length && text[end] in TRAILING_CHARS) {
                end++
            }

            val breaksHere = if (char in CJK_TERMINATORS) {
                true
            } else {
                val followedByBreak = end >= text.length || text[end].isWhitespace()
                followedByBreak && !isFalseStop(text, start, index)
            }

            if (breaksHere) {
                addTrimmed(text, start, end, limit, sentences)
                start = end
            }
            index = end
        }

        addTrimmed(text, start, text.length, limit, sentences)
        return sentences
    }

    /**
     * A `.` that does not end a sentence: ellipsis, a number ("3.14", "1."), a single-letter
     * initial or a known abbreviation.
     */
    private fun isFalseStop(text: String, sentenceStart: Int, dotIndex: Int): Boolean {
        if (text[dotIndex] != '.') return false

        val next = text.getOrNull(dotIndex + 1)
        val previous = text.getOrNull(dotIndex - 1) ?: return false
        if (previous == '.') return true
        if (previous.isDigit() && next?.isDigit() == true) return true

        var wordStart = dotIndex
        while (wordStart > sentenceStart && text[wordStart - 1].isLetterOrDigit()) {
            wordStart--
        }
        val word = text.substring(wordStart, dotIndex)
        if (word.isEmpty()) return false
        if (word.length == 1 && word[0].isLetter() && word[0].isUpperCase()) return true
        if (word.all { it.isDigit() }) return true

        val normalized = word.lowercase()
        if (normalized in ABBREVIATIONS) return true
        // "e.g." / "т.д." style: the previous token already carries a dot.
        val extended = text.substring(
            (wordStart - 2).coerceAtLeast(sentenceStart),
            dotIndex
        ).lowercase()
        return extended in ABBREVIATIONS
    }

    private fun addTrimmed(
        text: String,
        rawStart: Int,
        rawEnd: Int,
        limit: Int,
        target: MutableList<TtsSentence>
    ) {
        var start = rawStart
        var end = rawEnd.coerceAtMost(text.length)
        while (start < end && text[start].isWhitespace()) start++
        while (end > start && text[end - 1].isWhitespace()) end--
        if (start >= end) return

        if (end - start <= limit) {
            target += TtsSentence(start, end)
            return
        }
        splitLongRange(text, start, end, limit, target)
    }

    /** Splits an over-long sentence on the last clause break (or space) before the limit. */
    private fun splitLongRange(
        text: String,
        start: Int,
        end: Int,
        limit: Int,
        target: MutableList<TtsSentence>
    ) {
        var chunkStart = start
        while (end - chunkStart > limit) {
            val hardEnd = chunkStart + limit
            var cut = -1
            for (i in hardEnd - 1 downTo chunkStart + MIN_SPLIT_LENGTH) {
                if (text[i] in CLAUSE_BREAKS) {
                    cut = i + 1
                    break
                }
            }
            if (cut == -1) {
                for (i in hardEnd - 1 downTo chunkStart + MIN_SPLIT_LENGTH) {
                    if (text[i].isWhitespace()) {
                        cut = i
                        break
                    }
                }
            }
            if (cut == -1) cut = hardEnd

            var chunkEnd = cut
            while (chunkEnd > chunkStart && text[chunkEnd - 1].isWhitespace()) chunkEnd--
            if (chunkEnd > chunkStart) target += TtsSentence(chunkStart, chunkEnd)

            chunkStart = cut
            while (chunkStart < end && text[chunkStart].isWhitespace()) chunkStart++
        }
        if (chunkStart < end) target += TtsSentence(chunkStart, end)
    }
}
