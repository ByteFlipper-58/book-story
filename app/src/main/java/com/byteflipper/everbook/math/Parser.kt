/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.math

/**
 * Рекурсивный descent-парсер для подмножества LaTeX.
 */
internal class Parser(private val lexer: Lexer) {
    private var lookahead: Token = lexer.nextToken()

    fun parse(): Expr {
        val expr = parseExpression()
        return if (expr is Expr.Row && expr.elements.size == 1) {
            expr.elements[0]
        } else {
            expr
        }
    }

    // Expression := Atom { Atom | SupSub }
    private fun parseExpression(stopOnRight: Boolean = false): Expr {
        val elements = mutableListOf<Expr>()

        while (!isTerminator(lookahead, stopOnRight)) {
            var atom = parseAtom()
            
            if (atom is Expr.Symbol && atom.value in setOf("∑", "∫", "∏", "lim")) {
                var lowerLimit: Expr? = null
                var upperLimit: Expr? = null
                
                if (lookahead is Token.Special && (lookahead as Token.Special).kind == Token.Special.Kind.SUB) {
                    consume() // _
                    lowerLimit = parseBracketedOrSingle()
                }
                
                if (lookahead is Token.Special && (lookahead as Token.Special).kind == Token.Special.Kind.SUP) {
                    consume() // ^
                    upperLimit = parseBracketedOrSingle()
                }
                
                atom = if (lowerLimit != null || upperLimit != null) {
                    Expr.BigOperator(atom.value, lowerLimit, upperLimit)
                } else {
                    atom
                }
            } else {
                atom = parsePostfix(atom)
            }
            
            elements.add(atom)
        }

        return when (elements.size) {
            0 -> Expr.Row(emptyList())
            1 -> elements[0]
            else -> Expr.Row(elements)
        }
    }

    private fun parseAtom(): Expr = when (val tok = lookahead) {
        is Token.Symbol -> {
            consume()
            val char = tok.char.toString()
            // Проверяем, является ли это числом
            if (char.all { it.isDigit() || it == '.' }) {
                Expr.Number(char)
            } else {
                Expr.Symbol(char)
            }
        }

        is Token.Command -> {
            consume()
            when (tok.name) {
                "frac" -> parseFraction()
                "sqrt" -> parseRoot()
                "left" -> parseLeftRight()
                "mathbb", "mathcal", "mathfrak", "mathscr", "mathrm", 
                "mathsf", "mathtt", "mathit", "mathbf", "boldsymbol" -> 
                    parseSpecialCommand(tok.name)
                "matrix" -> parseMatrix()
                "pmatrix" -> parseMatrix("(", ")")
                "bmatrix" -> parseMatrix("[", "]")
                "vmatrix" -> parseMatrix("|", "|")
                "Vmatrix" -> parseMatrix("‖", "‖")
                "text" -> parseText()
                "binom" -> parseBinom()
                "overbrace" -> parseOverBrace()
                "underbrace" -> parseUnderBrace()
                "cases" -> parseCases()
                "sum" -> Expr.Symbol("∑")
                "int" -> Expr.Symbol("∫")
                "prod" -> Expr.Symbol("∏")
                "lim" -> Expr.Symbol("lim")
                else -> {
                    val mapped = LatexMappings.mapCommand(tok.name)
                    if (mapped != null) Expr.Symbol(mapped) else Expr.Symbol("\\${tok.name}")
                }
            }
        }

        is Token.Special -> {
            when (tok.kind) {
                Token.Special.Kind.L_BRACE -> {
                    consume() // {
                    val inner = parseExpression()
                    expect(Token.Special.Kind.R_BRACE)
                    Expr.Group(inner)
                }
                else -> {
                    consume()
                    Expr.Symbol(tok.kind.char.toString())
                }
            }
        }

        Token.EOF -> Expr.Row(emptyList())
    }

    private fun parsePostfix(base: Expr): Expr {
        var result = base
        
        while (true) {
            when {
                lookahead is Token.Special && (lookahead as Token.Special).kind == Token.Special.Kind.SUP -> {
                    consume() // ^
                    val exp = parseBracketedOrSingle()
                    result = if (lookahead is Token.Special && (lookahead as Token.Special).kind == Token.Special.Kind.SUB) {
                        consume() // _
                        val sub = parseBracketedOrSingle()
                        Expr.SupSub(result, exp, sub)
                    } else {
                        Expr.Superscript(result, exp)
                    }
                }
                lookahead is Token.Special && (lookahead as Token.Special).kind == Token.Special.Kind.SUB -> {
                    consume() // _
                    val sub = parseBracketedOrSingle()
                    result = if (lookahead is Token.Special && (lookahead as Token.Special).kind == Token.Special.Kind.SUP) {
                        consume() // ^
                        val exp = parseBracketedOrSingle()
                        Expr.SupSub(result, exp, sub)
                    } else {
                        Expr.Subscript(result, sub)
                    }
                }
                else -> break
            }
        }
        
        return result
    }

    private fun parseFraction(): Expr {
        expect(Token.Special.Kind.L_BRACE)
        val num = parseExpression()
        expect(Token.Special.Kind.R_BRACE)
        expect(Token.Special.Kind.L_BRACE)
        val den = parseExpression()
        expect(Token.Special.Kind.R_BRACE)
        return Expr.Fraction(num, den)
    }

    private fun parseRoot(): Expr {
        var degree: Expr? = null
        if (lookahead is Token.Special && (lookahead as Token.Special).kind == Token.Special.Kind.L_BRACKET) {
            consume()
            degree = parseExpression()
            expect(Token.Special.Kind.R_BRACKET)
        }
        expect(Token.Special.Kind.L_BRACE)
        val radicand = parseExpression()
        expect(Token.Special.Kind.R_BRACE)
        return Expr.Root(radicand, degree)
    }

    private fun parseBracketedOrSingle(): Expr {
        return if (lookahead is Token.Special && (lookahead as Token.Special).kind == Token.Special.Kind.L_BRACE) {
            consume()
            val inner = parseExpression()
            expect(Token.Special.Kind.R_BRACE)
            inner
        } else {
            parseAtom()
        }
    }

    private fun parseLeftRight(): Expr {
        val leftBracket = when (val tok = lookahead) {
            is Token.Special -> {
                val char = tok.kind.char.toString()
                consume()
                char
            }
            is Token.Symbol -> {
                val char = tok.char.toString()
                consume()
                char
            }
            else -> {
                consume()
                "("
            }
        }

        val inner = parseExpression(stopOnRight = true)

        if (lookahead is Token.Command && (lookahead as Token.Command).name == "right") {
            consume()
        }

        val rightBracket = when (val tok = lookahead) {
            is Token.Special -> {
                val char = tok.kind.char.toString()
                consume()
                char
            }
            is Token.Symbol -> {
                val char = tok.char.toString()
                consume()
                char
            }
            else -> {
                consume()
                ")"
            }
        }

        return Expr.LeftRight(leftBracket, inner, rightBracket)
    }

    private fun parseSpecialCommand(command: String): Expr {
        expect(Token.Special.Kind.L_BRACE)
        val content = parseExpression()
        expect(Token.Special.Kind.R_BRACE)
        
        // Для команд типа mathbb пытаемся сразу применить маппинг
        if (command == "mathbb" && content is Expr.Symbol) {
            val mapped = LatexMappings.mapMathbb(content.value)
            if (mapped != null) {
                return Expr.Symbol(mapped)
            }
        }
        
        return Expr.SpecialCommand(command, content)
    }

    private fun parseMatrix(leftBracket: String = "(", rightBracket: String = ")"): Expr {
        expect(Token.Special.Kind.L_BRACE)
        val rows = mutableListOf<Expr.Row>()
        
        while (!(lookahead is Token.Special && (lookahead as Token.Special).kind == Token.Special.Kind.R_BRACE)) {
            val elements = mutableListOf<Expr>()
            
            while (!(lookahead is Token.Command && (lookahead as Token.Command).name == "\\\\") &&
                   !(lookahead is Token.Special && (lookahead as Token.Special).kind == Token.Special.Kind.R_BRACE)) {
                elements.add(parseExpression())
                
                if (lookahead is Token.Special && (lookahead as Token.Special).kind.char == '&') {
                    consume()
                }
            }
            
            rows.add(Expr.Row(elements))
            
            if (lookahead is Token.Command && (lookahead as Token.Command).name == "\\\\") {
                consume()
            }
        }
        
        expect(Token.Special.Kind.R_BRACE)
        return Expr.Matrix(rows, leftBracket, rightBracket)
    }

    private fun parseText(): Expr {
        expect(Token.Special.Kind.L_BRACE)
        val content = parseTextContent()
        expect(Token.Special.Kind.R_BRACE)
        return Expr.Text(content)
    }

    private fun parseBinom(): Expr {
        expect(Token.Special.Kind.L_BRACE)
        val n = parseExpression()
        expect(Token.Special.Kind.R_BRACE)
        expect(Token.Special.Kind.L_BRACE)
        val k = parseExpression()
        expect(Token.Special.Kind.R_BRACE)
        return Expr.Binom(n, k)
    }

    private fun parseTextContent(): String {
        val content = StringBuilder()
        while (lookahead !is Token.Special || (lookahead as? Token.Special)?.kind != Token.Special.Kind.R_BRACE) {
            when (lookahead) {
                is Token.Symbol -> {
                    content.append((lookahead as Token.Symbol).char)
                    consume()
                }
                is Token.Command -> {
                    content.append("\\${(lookahead as Token.Command).name}")
                    consume()
                }
                is Token.Special -> {
                    content.append((lookahead as Token.Special).kind.char)
                    consume()
                }
                else -> break
            }
        }
        return content.toString()
    }

    private fun parseOverBrace(): Expr {
        expect(Token.Special.Kind.L_BRACE)
        val content = parseExpression()
        expect(Token.Special.Kind.R_BRACE)
        expect(Token.Special.Kind.L_BRACE)
        val annotation = parseExpression()
        expect(Token.Special.Kind.R_BRACE)
        return Expr.OverBrace(content, annotation)
    }

    private fun parseUnderBrace(): Expr {
        expect(Token.Special.Kind.L_BRACE)
        val content = parseExpression()
        expect(Token.Special.Kind.R_BRACE)
        expect(Token.Special.Kind.L_BRACE)
        val annotation = parseExpression()
        expect(Token.Special.Kind.R_BRACE)
        return Expr.UnderBrace(content, annotation)
    }

    private fun parseCases(): Expr {
        expect(Token.Special.Kind.L_BRACE)
        val cases = mutableListOf<Pair<Expr, Expr>>()
        
        while (lookahead !is Token.Special || (lookahead as? Token.Special)?.kind != Token.Special.Kind.R_BRACE) {
            val left = parseExpression()
            
            if (lookahead is Token.Special && (lookahead as Token.Special).kind.char == '&') {
                consume()
            }
            
            val right = parseExpression()
            cases.add(left to right)
            
            if (lookahead is Token.Command && (lookahead as Token.Command).name == "\\\\") {
                consume()
            }
        }
        
        expect(Token.Special.Kind.R_BRACE)
        return Expr.Cases(cases)
    }

    // Helpers
    private fun consume() {
        lookahead = lexer.nextToken()
    }

    private fun expect(kind: Token.Special.Kind) {
        val tok = lookahead
        if (tok is Token.Special && tok.kind == kind) {
            consume()
        } else {
            // В продакшене нужна обработка ошибок
            consume()
        }
    }

    private fun isTerminator(token: Token, stopOnRight: Boolean): Boolean {
        return when (token) {
            Token.EOF -> true
            is Token.Special -> token.kind in setOf(
                Token.Special.Kind.R_BRACE,
                Token.Special.Kind.R_BRACKET,
                Token.Special.Kind.R_PAREN
            ) || token.kind.char == '&'
            is Token.Command -> stopOnRight && token.name == "right" || token.name == "\\\\"
            else -> false
        }
    }
}