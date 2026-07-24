/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.domain.reader

object HighlightPalette {

    const val maxSize: Int = 12

    val defaultArgbPalette: List<Int> = listOf(
        0xFFFFF176.toInt(),
        0xFFAED581.toInt(),
        0xFF4FC3F7.toInt(),
        0xFFF06292.toInt(),
        0xFFBA68C8.toInt(),
        0xFFFFB74D.toInt()
    )

    fun normalize(colors: List<Int>): List<Int> = colors.distinct()
        .take(maxSize)
        .ifEmpty { defaultArgbPalette }

    fun encode(colors: List<Int>): String = normalize(colors)
        .joinToString(",") { it.toUInt().toString(16) }

    fun decode(value: String?): List<Int> = value
        ?.split(',')
        ?.mapNotNull { it.toUIntOrNull(16)?.toInt() }
        ?.let(::normalize)
        ?: defaultArgbPalette
}
