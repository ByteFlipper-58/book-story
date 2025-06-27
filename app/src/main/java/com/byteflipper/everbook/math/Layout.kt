/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.math

import android.graphics.Paint

/**
 * Разметчик (layout-движок), переводящий AST в измеренные боксы.
 */
internal class Layout(
    private val paint: Paint,
    private val scriptPaint: Paint
) {
    private val gap = paint.textSize * 0.1f
    private val lineThickness = paint.textSize * 0.05f
    
    // Математическая ось обычно находится на высоте ~0.35 × x-height от baseline
    private val mainAxisHeight = -paint.fontMetrics.ascent * 0.35f
    private val scriptAxisHeight = -scriptPaint.fontMetrics.ascent * 0.35f

    /**
     * Преобразует AST выражения в измеренный бокс
     */
    fun layout(expr: Expr, isScript: Boolean = false): Box {
        val p = if (isScript) scriptPaint else paint
        val axisHeight = if (isScript) scriptAxisHeight else mainAxisHeight
        return when (expr) {
            is Expr.Symbol -> Box.SymbolBox(expr.value, p)
            
            is Expr.Number -> Box.SymbolBox(expr.value, p)
            
            is Expr.Text -> Box.HBox(expr.value.map { Box.SymbolBox(it.toString(), p) })
            
            is Expr.Group -> layout(expr.content, isScript)
            
            is Expr.Superscript -> {
                val base = layout(expr.base, isScript)
                val sup = layout(expr.exponent, true)
                Box.SupSubBox(base, sup, null, gap)
            }
            
            is Expr.Subscript -> {
                val base = layout(expr.base, isScript)
                val sub = layout(expr.subscript, true)
                Box.SupSubBox(base, null, sub, gap)
            }
            
            is Expr.SupSub -> {
                val base = layout(expr.base, isScript)
                val sup = layout(expr.superscript, true)
                val sub = layout(expr.subscript, true)
                Box.SupSubBox(base, sup, sub, gap)
            }
            
            is Expr.Fraction -> {
                val num = layout(expr.numerator, isScript)
                val den = layout(expr.denominator, isScript)
                Box.FractionBox(num, den, lineThickness, gap)
            }
            
            is Expr.Root -> {
                val radicand = layout(expr.radicand, isScript)
                val degree = expr.degree?.let { layout(it, true) }
                Box.RootBox(radicand, degree, lineThickness, gap)
            }
            
            is Expr.BigOperator -> {
                val op = Box.SymbolBox(expr.operator, p)
                val lower = expr.lowerLimit?.let { layout(it, true) }
                val upper = expr.upperLimit?.let { layout(it, true) }
                
                Box.BigOperatorBox(expr.operator, p, lower, upper, gap)
            }
            
            is Expr.Row -> {
                if (expr.elements.isEmpty()) {
                    Box.HSpace(0f)
                } else {
                    val boxes = mutableListOf<Box>()
                    expr.elements.forEachIndexed { index, el ->
                        boxes.add(layout(el, isScript))
                        if (index < expr.elements.size - 1) {
                            boxes.add(Box.HSpace(gap * 0.5f))
                        }
                    }
                    Box.HBox(boxes)
                }
            }
            
            is Expr.Binom -> {
                val n = layout(expr.n, isScript)
                val k = layout(expr.k, isScript)
                Box.BinomBox(n, k, gap)
            }
            
            is Expr.Matrix -> {
                val rows = expr.rows.map { row ->
                    row.elements.map { layout(it, isScript) }
                }
                Box.MatrixBox(rows, expr.leftBracket, expr.rightBracket, gap, p)
            }
            
            is Expr.Cases -> {
                val rows = expr.cases.map { (left, right) ->
                    layout(left, isScript) to layout(right, isScript)
                }
                Box.CasesBox(rows, gap, p)
            }
            
            is Expr.OverBrace -> {
                val content = layout(expr.content, isScript)
                val annotation = layout(expr.annotation, true)
                Box.BraceBox(content, annotation, true, gap)
            }
            
            is Expr.UnderBrace -> {
                val content = layout(expr.content, isScript)
                val annotation = layout(expr.annotation, true)
                Box.BraceBox(content, annotation, false, gap)
            }
            
            is Expr.LeftRight -> {
                val content = layout(expr.content, isScript)
                val leftBracket = if (expr.leftBracket == ".") null else
                    Box.StretchyBracketBox(expr.leftBracket, content.height, p)
                val rightBracket = if (expr.rightBracket == ".") null else
                    Box.StretchyBracketBox(expr.rightBracket, content.height, p)
                
                val boxes = buildList {
                    leftBracket?.let { add(it) }
                    add(content)
                    rightBracket?.let { add(it) }
                }
                
                Box.HBox(boxes)
            }
            
            is Expr.SpecialCommand -> when (expr.command) {
                "mathbb", "mathcal", "mathfrak", "mathscr", "mathrm", "mathsf", 
                "mathtt", "mathit", "mathbf", "boldsymbol" -> {
                    expr.argument?.let { layout(it, isScript) } ?: Box.HSpace(0f)
                }
                else -> Box.SymbolBox("?", p)
            }
        }
    }
} 