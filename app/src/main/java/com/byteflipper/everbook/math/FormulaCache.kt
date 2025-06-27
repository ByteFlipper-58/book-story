/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.math

import android.graphics.Paint
import android.util.LruCache

/**
 * Простой LRU-кэш, чтобы не парсить одну и ту же формулу многократно.
 */
internal object FormulaCache {
    private data class CacheKey(val latex: String, val fontSizePx: Int)
    
    // Храним до 100 формул
    private val cache = LruCache<CacheKey, Box>(100)
    
    fun getOrBuild(latex: String, fontSizePx: Int, mainPaint: Paint, scriptPaint: Paint): Box {
        val key = CacheKey(latex, fontSizePx)
        return cache.get(key) ?: run {
            val normalizedLatex = preprocess(latex)
            val layout = Layout(mainPaint, scriptPaint)
            val lexer = Lexer(normalizedLatex)
            val parser = Parser(lexer)
            val expr = parser.parse()
            val box = layout.layout(expr)
            cache.put(key, box)
            box
        }
    }
    
    // Простейший препроцессор для \begin{matrix} ... \end{matrix} и др.
    private fun preprocess(src: String): String {
        var text = src.replace("\n", " ").replace("\r", " ").replace("\t", " ")
        
        val replacements = mapOf(
            "\\begin\\{matrix\\}" to "\\matrix{",
            "\\end\\{matrix\\}" to "}",
            "\\begin\\{pmatrix\\}" to "\\pmatrix{",
            "\\end\\{pmatrix\\}" to "}",
            "\\begin\\{bmatrix\\}" to "\\bmatrix{",
            "\\end\\{bmatrix\\}" to "}",
            "\\begin\\{cases\\}" to "\\cases{",
            "\\end\\{cases\\}" to "}",
        )
        replacements.forEach { (from, to) ->
            text = text.replace(Regex(from), to)
        }
        return text
    }
    
    fun clear() {
        cache.evictAll()
    }
} 