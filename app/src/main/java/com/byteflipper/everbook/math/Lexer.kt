/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.math

/**
 * Простой лексер LaTeX-формул.
 * Поддерживает команды (буквы после `\`), спецсимволы и одиночные символы.
 * Не обрабатывает комментарии и пробелы внутри формул, т.к. пробелы в LaTeX
 * внутри команды обычно игнорируются. При необходимости это можно расширить.
 */
internal class Lexer(private val input: String) {
    private var pos = 0
    private val length = input.length

    fun nextToken(): Token {
        skipWhitespace()
        if (pos >= length) return Token.EOF

        val ch = input[pos]

        if (ch == '\\' && pos + 1 < length && input[pos + 1] == '\\') {
            pos += 2
            return Token.Command("\\\\")
        }

        if (ch == '\\') {
            val start = ++pos
            while (pos < length && input[pos].isLetter()) pos++
            val name = input.substring(start, pos)
            return Token.Command(name)
        }

        Token.Special.Kind.values().firstOrNull { it.char == ch }?.let {
            pos++
            return Token.Special(it)
        }

        pos++
        return Token.Symbol(ch)
    }

    private fun skipWhitespace() {
        while (pos < length && input[pos].isWhitespace()) pos++
    }
} 