/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.math

import androidx.compose.ui.text.buildAnnotatedString
import com.byteflipper.everbook.domain.reader.ReaderText

private val REGEX = Regex("\\$([^$]+)\\$")

/**
 * Проходит по списку [ReaderText] и заменяет участки `$...$` на ReaderText.Math.
 */
internal fun List<ReaderText>.withLatexMath(): List<ReaderText> {
    val out = mutableListOf<ReaderText>()
    forEach { entry ->
        if (entry is ReaderText.Text) {
            val raw = entry.line.text
            var lastIndex = 0
            REGEX.findAll(raw).forEach { m ->
                if (m.range.first > lastIndex) {
                    val part = raw.substring(lastIndex, m.range.first)
                    if (part.isNotEmpty()) {
                        out += ReaderText.Text(buildAnnotatedString { append(part) })
                    }
                }
                val formula = m.groupValues[1]
                out += ReaderText.Math(formula)
                lastIndex = m.range.last + 1
            }
            if (lastIndex < raw.length) {
                val tail = raw.substring(lastIndex)
                if (tail.isNotEmpty()) {
                    out += ReaderText.Text(buildAnnotatedString { append(tail) })
                }
            }
        } else {
            out += entry
        }
    }
    return out.mergeConsecutiveMath()
}

// Объединяет подряд идущие ReaderText.Math, учитывая переносы строк между ними
private fun List<ReaderText>.mergeConsecutiveMath(): List<ReaderText> {
    val result = mutableListOf<ReaderText>()
    val buffer = StringBuilder()
    var lastWasMath = false
    for (i in indices) {
        val curr = this[i]
        if (curr is ReaderText.Math) {
            if (lastWasMath) buffer.append(" ")
            buffer.append(curr.latex)
            lastWasMath = true
        } else {
            if (lastWasMath && buffer.isNotEmpty()) {
                result += ReaderText.Math(buffer.toString())
                buffer.clear()
            }
            // Если между формулами был явный перенос строки — вставляем пустой Math (или можно ReaderText.Text("\n"))
            if (curr is ReaderText.Text && curr.line.text.trim().isEmpty() && (curr.line.text.contains("\n") || curr.line.text.contains("\r"))) {
                if (result.isNotEmpty() && result.last() is ReaderText.Math) {
                    // Завершаем предыдущий Math, добавляем перенос
                    result += ReaderText.Text(buildAnnotatedString { append("\n") })
                }
            } else {
                result += curr
            }
            lastWasMath = false
        }
    }
    if (lastWasMath && buffer.isNotEmpty()) {
        result += ReaderText.Math(buffer.toString())
    }
    return result
} 