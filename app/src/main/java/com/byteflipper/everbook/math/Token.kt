/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.math

/**
 * Представление лексемы LaTeX-формулы.
 */
internal sealed interface Token {
    /** Команда, начинающаяся с символа `\`, например `\frac`, `\sqrt`.  */
    data class Command(val name: String) : Token

    /** Одиночный символ (буква, цифра, спец-символ, греческая буква без `\`). */
    data class Symbol(val char: Char) : Token

    /** Спецсимволы, имеющие собственное семантическое значение в парсере. */
    data class Special(val kind: Kind) : Token {
        enum class Kind(val char: Char) {
            L_BRACE('{'), R_BRACE('}'),
            L_BRACKET('['), R_BRACKET(']'),
            L_PAREN('('),  R_PAREN(')'),
            SUP('^'), SUB('_'),
            LEFT('\\'), RIGHT('\\');
        }
    }

    object EOF : Token
} 