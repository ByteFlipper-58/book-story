/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.data.font

import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.min

/** Reads the human-readable family and style from the OpenType `name` table. */
internal object FontNameReader {
    private const val TABLE_DIRECTORY_OFFSET = 12
    private const val NAME_RECORD_SIZE = 12

    fun displayName(file: File): String? = runCatching {
        val bytes = file.readBytes()
        val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN)
        val tableCount = buffer.ushort(4)
        val nameTable = (0 until tableCount)
            .map { TABLE_DIRECTORY_OFFSET + it * 16 }
            .firstOrNull { offset -> buffer.tag(offset) == "name" }
            ?: return null
        val tableOffset = buffer.int(nameTable + 8)
        val recordCount = buffer.ushort(tableOffset + 2)
        val stringOffset = buffer.ushort(tableOffset + 4)
        val recordsOffset = tableOffset + 6

        fun value(nameId: Int): String? = (0 until recordCount)
            .map { recordsOffset + it * NAME_RECORD_SIZE }
            .firstNotNullOfOrNull { record ->
                if (buffer.ushort(record + 6) != nameId) return@firstNotNullOfOrNull null
                val platform = buffer.ushort(record)
                val length = buffer.ushort(record + 8)
                val offset = tableOffset + stringOffset + buffer.ushort(record + 10)
                if (offset < 0 || offset + length > bytes.size) return@firstNotNullOfOrNull null
                when (platform) {
                    0, 3 -> bytes.copyOfRange(offset, offset + length).toString(Charsets.UTF_16BE)
                    1 -> bytes.copyOfRange(offset, offset + length).toString(Charsets.ISO_8859_1)
                    else -> null
                }?.trim()?.takeIf(String::isNotEmpty)
            }

        val family = value(16) ?: value(1) ?: return null
        val style = value(17) ?: value(2)
        listOf(family, style)
            .filterNot { it.equals("Regular", ignoreCase = true) }
            .joinToString(", ")
            .takeIf(String::isNotEmpty)
    }.getOrNull()

    private fun ByteBuffer.ushort(offset: Int): Int = getShort(offset).toInt() and 0xffff

    private fun ByteBuffer.int(offset: Int): Int = getInt(offset)

    private fun ByteBuffer.tag(offset: Int): String =
        ByteArray(min(4, limit() - offset)).also { bytes ->
            repeat(bytes.size) { bytes[it] = get(offset + it) }
        }.toString(Charsets.US_ASCII)
}
