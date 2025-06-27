/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.math

/**
 * AST-узлы математической формулы.
 */
internal sealed interface Expr {
    /** Одиночный символ (буква/греческий/оператор) */
    data class Symbol(val value: String) : Expr
    
    /** Число */
    data class Number(val value: String) : Expr
    
    /** Текст в режиме \text{} или \mathrm{} */
    data class Text(val value: String) : Expr
    
    /** Группа в фигурных скобках */
    data class Group(val content: Expr) : Expr
    
    /** Последовательность элементов */
    data class Row(val elements: List<Expr>) : Expr
    
    /** Верхний индекс */
    data class Superscript(val base: Expr, val exponent: Expr) : Expr
    
    /** Нижний индекс */
    data class Subscript(val base: Expr, val subscript: Expr) : Expr
    
    /** База с верхним и нижним индексами */
    data class SupSub(
        val base: Expr,
        val superscript: Expr,
        val subscript: Expr
    ) : Expr
    
    /** Дробь \frac{num}{den} */
    data class Fraction(val numerator: Expr, val denominator: Expr) : Expr
    
    /** Корень. Если degree == null, квадратный */
    data class Root(val radicand: Expr, val degree: Expr?) : Expr
    
    /** Большой оператор (сумма, интеграл и т.д.) с пределами */
    data class BigOperator(
        val operator: String,
        val lowerLimit: Expr?,
        val upperLimit: Expr?
    ) : Expr
    
    /** Матрица */
    data class Matrix(
        val rows: List<Row>,
        val leftBracket: String = "(",
        val rightBracket: String = ")"
    ) : Expr
    
    /** Биномиальный коэффициент */
    data class Binom(val n: Expr, val k: Expr) : Expr
    
    /** Cases (piecewise функции) */
    data class Cases(val cases: List<Pair<Expr, Expr>>) : Expr
    
    /** Надскобка */
    data class OverBrace(val content: Expr, val annotation: Expr) : Expr
    
    /** Подскобка */
    data class UnderBrace(val content: Expr, val annotation: Expr) : Expr
    
    /** Скобки с \left/\right */
    data class LeftRight(
        val leftBracket: String,
        val content: Expr,
        val rightBracket: String
    ) : Expr
    
    /** Специальные команды (mathbb, mathcal и т.д.) */
    data class SpecialCommand(
        val command: String,
        val argument: Expr?
    ) : Expr
} 