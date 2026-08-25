/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.domain.reader.tts

import com.byteflipper.everbook.domain.reader.ReaderText

/**
 * Turns a book's flat `List<ReaderText>` into an utterance stream and navigates it.
 *
 * Segmentation is done per entry and cached for a small sliding window instead of up front:
 * a large book holds tens of thousands of paragraphs, and playback only ever needs the current
 * neighbourhood.
 */
class TtsBookQueue(
    private val entries: List<ReaderText>,
    private val maxUtteranceLength: Int,
    private val speakChapterTitles: Boolean
) {

    private val cache = object : LinkedHashMap<Int, List<TtsUtterance>>(
        CACHE_CAPACITY,
        0.75f,
        true
    ) {
        override fun removeEldestEntry(
            eldest: MutableMap.MutableEntry<Int, List<TtsUtterance>>?
        ): Boolean = size > CACHE_CAPACITY
    }

    val isEmpty: Boolean get() = entries.isEmpty()

    fun utterances(textIndex: Int): List<TtsUtterance> {
        if (textIndex !in entries.indices) return emptyList()
        cache[textIndex]?.let { return it }
        val built = build(textIndex)
        cache[textIndex] = built
        return built
    }

    fun utteranceAt(position: TtsPosition): TtsUtterance? =
        utterances(position.textIndex).getOrNull(position.sentenceIndex)

    /** First speakable position at or after [textIndex], or `null` past the end of the book. */
    fun firstFrom(textIndex: Int): TtsPosition? {
        var index = textIndex.coerceAtLeast(0)
        while (index in entries.indices) {
            if (utterances(index).isNotEmpty()) return TtsPosition(index, 0)
            index++
        }
        return null
    }

    private fun lastAtOrBefore(textIndex: Int): TtsPosition? {
        var index = textIndex.coerceAtMost(entries.lastIndex)
        while (index >= 0) {
            val size = utterances(index).size
            if (size > 0) return TtsPosition(index, size - 1)
            index--
        }
        return null
    }

    fun next(position: TtsPosition): TtsPosition? {
        val current = utterances(position.textIndex)
        if (position.sentenceIndex + 1 < current.size) {
            return TtsPosition(position.textIndex, position.sentenceIndex + 1)
        }
        return firstFrom(position.textIndex + 1)
    }

    fun previous(position: TtsPosition): TtsPosition? {
        if (position.sentenceIndex > 0) {
            return TtsPosition(position.textIndex, position.sentenceIndex - 1)
        }
        if (position.textIndex <= 0) return null
        return lastAtOrBefore(position.textIndex - 1)
    }

    fun nextParagraph(position: TtsPosition): TtsPosition? =
        firstFrom(position.textIndex + 1)

    /**
     * Start of the current paragraph when playback is already past its first sentence, otherwise
     * the start of the previous one — the behaviour users expect from a "previous track" button.
     */
    fun previousParagraph(position: TtsPosition): TtsPosition? {
        if (position.sentenceIndex > 0) return TtsPosition(position.textIndex, 0)
        var index = position.textIndex - 1
        while (index >= 0) {
            if (utterances(index).isNotEmpty()) return TtsPosition(index, 0)
            index--
        }
        return null
    }

    /** Whether moving from [from] to [to] crosses into a new chapter heading. */
    fun crossesChapter(from: TtsPosition, to: TtsPosition): Boolean {
        if (to.textIndex <= from.textIndex) return false
        return (from.textIndex + 1..to.textIndex).any { entries.getOrNull(it) is ReaderText.Chapter }
    }

    private fun build(textIndex: Int): List<TtsUtterance> {
        val entry = entries[textIndex]
        val isChapter = entry is ReaderText.Chapter
        val source = when (entry) {
            is ReaderText.Chapter -> entry.title.takeIf { speakChapterTitles }.orEmpty()
            // `line` is the rendered text, so utterance ranges line up with what is highlighted.
            is ReaderText.Text -> entry.line.text
            is ReaderText.Image, ReaderText.Separator, is ReaderText.Math -> ""
        }
        if (source.isBlank()) return emptyList()

        return TtsTextSegmenter.segment(source, maxUtteranceLength)
            .mapIndexed { sentenceIndex, sentence ->
                TtsUtterance(
                    id = "$textIndex:$sentenceIndex",
                    text = source.substring(sentence.start, sentence.end),
                    textIndex = textIndex,
                    sentenceIndex = sentenceIndex,
                    startChar = sentence.start,
                    endChar = sentence.end,
                    isChapterTitle = isChapter
                )
            }
    }

    companion object {
        private const val CACHE_CAPACITY = 64

        fun parsePosition(utteranceId: String): TtsPosition? {
            val separator = utteranceId.indexOf(':')
            if (separator <= 0) return null
            val textIndex = utteranceId.substring(0, separator).toIntOrNull() ?: return null
            val sentenceIndex = utteranceId.substring(separator + 1).toIntOrNull() ?: return null
            return TtsPosition(textIndex, sentenceIndex)
        }
    }
}
