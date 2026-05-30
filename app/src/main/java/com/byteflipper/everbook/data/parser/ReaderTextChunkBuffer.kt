/*
 * EverBook - a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.data.parser

import com.byteflipper.everbook.domain.reader.ReaderText
import kotlinx.coroutines.yield

internal const val READER_TEXT_INITIAL_CHUNK_SIZE = 16
internal const val READER_TEXT_CHUNK_SIZE = 160
internal typealias ReaderTextChunkSink = suspend (List<ReaderText>) -> Unit

internal class ReaderTextChunkBuffer(
    private val chunkSize: Int = READER_TEXT_CHUNK_SIZE,
    private val initialChunkSize: Int = READER_TEXT_INITIAL_CHUNK_SIZE,
    private val onChunk: ReaderTextChunkSink?
) {
    private val pending = mutableListOf<ReaderText>()
    private var emittedChunkCount = 0

    suspend fun add(entry: ReaderText) {
        if (onChunk == null) return
        pending.add(entry)
        flushIfFull()
    }

    suspend fun addAll(entries: List<ReaderText>) {
        if (onChunk == null) return
        entries.forEach { entry ->
            add(entry)
        }
    }

    suspend fun flush() {
        if (onChunk == null) return
        if (pending.isEmpty()) return
        onChunk.invoke(pending.toList())
        pending.clear()
        emittedChunkCount++
        yield()
    }

    private suspend fun flushIfFull() {
        val targetSize = when {
            emittedChunkCount == 0 -> initialChunkSize
            emittedChunkCount < 3 -> chunkSize
            else -> chunkSize * 2
        }
        if (pending.size >= targetSize) {
            if (pending.lastOrNull() is ReaderText.Chapter) return
            flush()
        }
    }
}
