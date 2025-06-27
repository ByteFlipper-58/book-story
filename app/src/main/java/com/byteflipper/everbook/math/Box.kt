/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.math

import android.graphics.Paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import kotlin.math.max
import kotlin.math.min

/**
 * Базовый класс для измеренных элементов формулы.
 * Система координат: baseline-relative.
 * axisHeight — смещение математической оси от baseline (положительное — выше).
 */
internal sealed class Box(
    val width: Float,
    val ascent: Float,
    val descent: Float,
    val axisHeight: Float
) {
    companion object {
        // Флаг для отладки - рисовать границы боксов
        const val DEBUG_BOUNDS = false
    }
    
    val height: Float = ascent + descent

    abstract fun draw(scope: DrawScope, x: Float, yBaseline: Float, color: Color, paint: Paint)
    
    // Отладочная функция для рисования границ бокса
    protected fun drawDebugBounds(scope: DrawScope, x: Float, yBaseline: Float) {
        if (DEBUG_BOUNDS) {
            // Рисуем границы бокса
            scope.drawRect(
                color = Color.Red.copy(alpha = 0.3f),
                topLeft = androidx.compose.ui.geometry.Offset(x, yBaseline - ascent),
                size = androidx.compose.ui.geometry.Size(width, height),
                style = Stroke(width = 1f)
            )
            // Рисуем baseline
            scope.drawLine(
                color = Color.Blue.copy(alpha = 0.5f),
                start = androidx.compose.ui.geometry.Offset(x, yBaseline),
                end = androidx.compose.ui.geometry.Offset(x + width, yBaseline),
                strokeWidth = 1f
            )
            // Рисуем математическую ось
            scope.drawLine(
                color = Color.Green.copy(alpha = 0.5f),
                start = androidx.compose.ui.geometry.Offset(x, yBaseline - axisHeight),
                end = androidx.compose.ui.geometry.Offset(x + width, yBaseline - axisHeight),
                strokeWidth = 1f
            )
        }
    }

    /**
     * Боксы, лежащие горизонтально последовательно.
     */
    class HBox(private val children: List<Box>) : Box(
        children.sumOf { it.width.toDouble() }.toFloat(),
        children.maxOfOrNull { it.ascent } ?: 0f,
        children.maxOfOrNull { it.descent } ?: 0f,
        children.firstOrNull()?.axisHeight ?: 0f
    ) {
        override fun draw(scope: DrawScope, x: Float, yBaseline: Float, color: Color, paint: Paint) {
            var dx = x
            children.forEach { c ->
                c.draw(scope, dx, yBaseline, color, paint)
                dx += c.width
            }
        }
    }

    /**
     * Символьный элемент.
     */
    class SymbolBox(
        private val text: String, 
        paint: Paint
    ) : Box(
        width = paint.measureText(text),
        ascent = -paint.fontMetrics.ascent,
        descent = paint.fontMetrics.descent,
        axisHeight = -paint.fontMetrics.ascent * 0.35f // Примерная высота оси для большинства шрифтов
    ) {
        private val cached = text
        override fun draw(scope: DrawScope, x: Float, yBaseline: Float, color: Color, paint: Paint) {
            drawDebugBounds(scope, x, yBaseline)
            scope.drawIntoCanvas { c ->
                paint.color = color.toArgb()
                c.nativeCanvas.drawText(cached, x, yBaseline, paint)
            }
        }
    }

    /**
     * База + супер/суб скрипты.
     */
    class SupSubBox(
        private val base: Box,
        private val superBox: Box?,
        private val subBox: Box?,
        private val gap: Float
    ) : Box(
        // Ширина включает базу и скрипты
        width = base.width + max(
            superBox?.width ?: 0f,
            subBox?.width ?: 0f
        ) * 0.7f, // скрипты занимают ~70% своей ширины из-за перекрытия
        ascent = base.ascent + (superBox?.let { 
            max(0f, it.ascent - base.ascent * 0.5f)
        } ?: 0f),
        descent = base.descent + (subBox?.let { 
            max(0f, it.descent - base.descent * 0.5f)
        } ?: 0f),
        axisHeight = base.axisHeight
    ) {
        override fun draw(scope: DrawScope, x: Float, yBaseline: Float, color: Color, paint: Paint) {
            base.draw(scope, x, yBaseline, color, paint)
            
            // Позиция для скриптов - сразу после базы с небольшим перекрытием
            val scriptX = x + base.width - base.width * 0.1f
            
            superBox?.let { s ->
                // Супер-скрипт поднимается от baseline на ~60% от ascent базы
                val sy = yBaseline - base.ascent * 0.6f
                s.draw(scope, scriptX, sy, color, paint)
            }
            subBox?.let { s ->
                // Суб-скрипт опускается от baseline на ~20% от descent базы
                val sy = yBaseline + base.descent * 0.2f + s.ascent
                s.draw(scope, scriptX, sy, color, paint)
            }
        }
    }

    /** Дробь с правильным выравниванием по математической оси */
    class FractionBox(
        private val num: Box,
        private val den: Box,
        private val lineThickness: Float,
        private val gap: Float
    ) : Box(
        width = maxOf(num.width, den.width) + gap * 2,
        // Высота от оси до верха числителя + половина линии
        ascent = num.axisHeight + lineThickness / 2 + gap + num.height,
        // Высота от оси до низа знаменателя + половина линии
        descent = -den.axisHeight + lineThickness / 2 + gap + den.height,
        axisHeight = num.axisHeight // Берем ось от числителя
    ) {
        override fun draw(scope: DrawScope, x: Float, yBaseline: Float, color: Color, paint: Paint) {
            val centerX = x + width / 2
            
            // Линия дроби на уровне математической оси
            val lineY = yBaseline - axisHeight
            scope.drawLine(
                color = color,
                start = androidx.compose.ui.geometry.Offset(x + gap, lineY),
                end = androidx.compose.ui.geometry.Offset(x + width - gap, lineY),
                strokeWidth = lineThickness
            )
            
            // Числитель выше линии
            val numX = centerX - num.width / 2
            val numY = lineY - lineThickness / 2 - gap - num.descent
            num.draw(scope, numX, numY, color, paint)
            
            // Знаменатель ниже линии
            val denX = centerX - den.width / 2
            val denY = lineY + lineThickness / 2 + gap + den.ascent
            den.draw(scope, denX, denY, color, paint)
        }
    }

    /** Корень с правильной геометрией */
    class RootBox(
        private val radicand: Box,
        private val degree: Box?,
        private val lineThickness: Float,
        private val gap: Float
    ) : Box(
        width = radicand.width + gap * 6.5f + (degree?.width ?: 0f),
        ascent = radicand.ascent + gap + lineThickness * 2,
        descent = maxOf(radicand.descent, radicand.axisHeight * 0.5f),
        axisHeight = radicand.axisHeight
    ) {
        override fun draw(scope: DrawScope, x: Float, yBaseline: Float, color: Color, paint: Paint) {
            val startX = x + (degree?.width ?: 0f) * 0.7f
            
            // Рисуем степень корня если есть
            degree?.let { d ->
                val dx = x
                // Позиционируем степень выше и левее основного символа корня
                val dy = yBaseline - radicand.ascent * 0.8f - d.descent
                d.draw(scope, dx, dy, color, paint)
            }
            
            // Рисуем знак корня с помощью Path для плавных линий
            val path = Path()
            
            // Точки для знака корня
            val tickStart = yBaseline - axisHeight * 0.5f  // Начало "галочки" выше
            val tickBottom = yBaseline + descent       // Нижняя точка
            val tickMid = yBaseline - axisHeight * 0.3f // Середина подъема
            val tickTop = yBaseline - ascent + gap     // Верхняя точка
            
            // Сдвигаем начало левее для более длинной "галочки"
            val leftStart = startX - gap * 1.5f
            
            // Начинаем с левого края "галочки"
            path.moveTo(leftStart, tickStart)
            
            // Рисуем "галочку" - спуск вниз и подъем
            path.lineTo(startX + gap * 0.7f, tickBottom)
            path.lineTo(startX + gap * 1.5f, tickMid)
            
            // Подъем к верхней части
            path.lineTo(startX + gap * 3, tickTop)
            
            // Горизонтальная линия над подкоренным выражением
            path.lineTo(startX + gap * 3 + radicand.width + gap, tickTop)
            
            // Рисуем путь с увеличенной толщиной
            scope.drawPath(path, color, style = Stroke(width = lineThickness * 2f))
            
            // Подкоренное выражение
            val radX = startX + gap * 3.5f
            val radY = yBaseline
            radicand.draw(scope, radX, radY, color, paint)
        }
    }

    /** Большой оператор с правильным центрированием по оси */
    class BigOperatorBox(
        private val operator: String,
        private val paint: Paint,
        private val lowerLimit: Box?,
        private val upperLimit: Box?,
        private val gap: Float
    ) : Box(
        width = maxOf(
            paint.measureText(operator),
            lowerLimit?.width ?: 0f,
            upperLimit?.width ?: 0f
        ),
        ascent = run {
            val fm = paint.fontMetrics
            val opAscent = -fm.ascent
            opAscent / 2 + (upperLimit?.let { it.height + gap } ?: 0f)
        },
        descent = run {
            val fm = paint.fontMetrics
            val opAscent = -fm.ascent
            val opDescent = fm.descent
            opAscent / 2 + opDescent - opAscent + (lowerLimit?.let { it.height + gap } ?: 0f)
        },
        axisHeight = run {
            val fm = paint.fontMetrics
            -fm.ascent * 0.35f
        }
    ) {
        override fun draw(scope: DrawScope, x: Float, yBaseline: Float, color: Color, paint: Paint) {
            val opWidth = paint.measureText(operator)
            val centerX = x + width / 2
            
            // Рисуем оператор по центру ширины
            val opX = centerX - opWidth / 2
            scope.drawIntoCanvas { c ->
                paint.color = color.toArgb()
                c.nativeCanvas.drawText(operator, opX, yBaseline, paint)
            }
            
            val fm = paint.fontMetrics
            
            // Верхний предел
            upperLimit?.let { limit ->
                val limitX = centerX - limit.width / 2
                val limitY = yBaseline - fm.ascent - gap - limit.descent
                limit.draw(scope, limitX, limitY, color, paint)
            }
            
            // Нижний предел
            lowerLimit?.let { limit ->
                val limitX = centerX - limit.width / 2
                val limitY = yBaseline + fm.descent + gap + limit.ascent
                limit.draw(scope, limitX, limitY, color, paint)
            }
        }
    }

    /** Матрица с правильным выравниванием */
    class MatrixBox(
        private val rows: List<List<Box>>,
        private val leftBracket: String,
        private val rightBracket: String,
        private val gap: Float,
        private val paint: Paint
    ) : Box(
        width = run {
            if (rows.isEmpty()) 0f else {
                val numCols = rows.maxOf { it.size }
                val colWidths = IntArray(numCols) { col ->
                    rows.maxOf { row -> 
                        if (col < row.size) row[col].width.toInt() else 0 
                    }
                }
                val contentWidth = colWidths.sum() + (numCols - 1) * gap
                contentWidth + gap * 4 // отступы для скобок
            }
        },
        ascent = run {
            if (rows.isEmpty()) 0f else {
                val totalHeight = rows.sumOf { row ->
                    (row.maxOfOrNull { it.height } ?: 0f).toDouble()
                }.toFloat() + (rows.size - 1) * gap
                val firstRowAxis = rows.firstOrNull()?.firstOrNull()?.axisHeight ?: 0f
                totalHeight / 2 + firstRowAxis
            }
        },
        descent = run {
            if (rows.isEmpty()) 0f else {
                val totalHeight = rows.sumOf { row ->
                    (row.maxOfOrNull { it.height } ?: 0f).toDouble()
                }.toFloat() + (rows.size - 1) * gap
                val firstRowAxis = rows.firstOrNull()?.firstOrNull()?.axisHeight ?: 0f
                totalHeight / 2 - firstRowAxis
            }
        },
        axisHeight = rows.firstOrNull()?.firstOrNull()?.axisHeight ?: 0f
    ) {
        override fun draw(scope: DrawScope, x: Float, yBaseline: Float, color: Color, paint: Paint) {
            if (rows.isEmpty()) return
            
            // Вычисляем ширины столбцов
            val numCols = rows.maxOf { it.size }
            val colWidths = IntArray(numCols) { col ->
                rows.maxOf { row -> 
                    if (col < row.size) row[col].width.toInt() else 0 
                }
            }
            
            // Содержимое матрицы
            val contentX = x + gap * 2
            var rowY = yBaseline - ascent + gap
            
            rows.forEach { row ->
                var colX = contentX
                val rowHeight = row.maxOfOrNull { it.height } ?: 0f
                val rowBaseline = rowY + (row.maxOfOrNull { it.ascent } ?: 0f)
                
                row.forEachIndexed { idx, cell ->
                    val cellX = colX + (colWidths[idx] - cell.width.toInt()) / 2
                    cell.draw(scope, cellX.toFloat(), rowBaseline, color, paint)
                    if (idx < numCols - 1) {
                        colX += colWidths[idx] + gap.toInt()
                    }
                }
                
                rowY += rowHeight + gap
            }
            
            // Рисуем скобки
            val bracketY = yBaseline - axisHeight
            drawStretchyBracket(scope, x, bracketY, leftBracket, height, color, paint)
            drawStretchyBracket(scope, x + width - gap, bracketY, rightBracket, height, color, paint)
        }
        
        private fun drawStretchyBracket(
            scope: DrawScope,
            x: Float,
            centerY: Float,
            bracket: String,
            height: Float,
            color: Color,
            paint: Paint
        ) {
            when (bracket) {
                "(", ")" -> {
                    // Рисуем круглые скобки из дуг
                    val isLeft = bracket == "("
                    val path = Path()
                    val halfHeight = height / 2
                    val width = 12f
                    
                    if (isLeft) {
                        path.moveTo(x + width, centerY - halfHeight)
                        path.quadraticBezierTo(
                            x, centerY - halfHeight,
                            x, centerY
                        )
                        path.quadraticBezierTo(
                            x, centerY + halfHeight,
                            x + width, centerY + halfHeight
                        )
                    } else {
                        path.moveTo(x, centerY - halfHeight)
                        path.quadraticBezierTo(
                            x + width, centerY - halfHeight,
                            x + width, centerY
                        )
                        path.quadraticBezierTo(
                            x + width, centerY + halfHeight,
                            x, centerY + halfHeight
                        )
                    }
                    
                    scope.drawPath(path, color, style = Stroke(width = 2f))
                }
                "[", "]" -> {
                    // Квадратные скобки
                    val isLeft = bracket == "["
                    val width = 8f
                    val halfHeight = height / 2
                    
                    if (isLeft) {
                        scope.drawLine(
                            color,
                            androidx.compose.ui.geometry.Offset(x + width, centerY - halfHeight),
                            androidx.compose.ui.geometry.Offset(x, centerY - halfHeight),
                            2f
                        )
                        scope.drawLine(
                            color,
                            androidx.compose.ui.geometry.Offset(x, centerY - halfHeight),
                            androidx.compose.ui.geometry.Offset(x, centerY + halfHeight),
                            2f
                        )
                        scope.drawLine(
                            color,
                            androidx.compose.ui.geometry.Offset(x, centerY + halfHeight),
                            androidx.compose.ui.geometry.Offset(x + width, centerY + halfHeight),
                            2f
                        )
                    } else {
                        scope.drawLine(
                            color,
                            androidx.compose.ui.geometry.Offset(x, centerY - halfHeight),
                            androidx.compose.ui.geometry.Offset(x + width, centerY - halfHeight),
                            2f
                        )
                        scope.drawLine(
                            color,
                            androidx.compose.ui.geometry.Offset(x + width, centerY - halfHeight),
                            androidx.compose.ui.geometry.Offset(x + width, centerY + halfHeight),
                            2f
                        )
                        scope.drawLine(
                            color,
                            androidx.compose.ui.geometry.Offset(x + width, centerY + halfHeight),
                            androidx.compose.ui.geometry.Offset(x, centerY + halfHeight),
                            2f
                        )
                    }
                }
                "|" -> {
                    // Вертикальная линия
                    val halfHeight = height / 2
                    scope.drawLine(
                        color,
                        androidx.compose.ui.geometry.Offset(x + 4f, centerY - halfHeight),
                        androidx.compose.ui.geometry.Offset(x + 4f, centerY + halfHeight),
                        2f
                    )
                }
                else -> {
                    // Для других символов используем текст
                    val savedSize = paint.textSize
                    paint.textSize = savedSize * (height / (paint.fontMetrics.descent - paint.fontMetrics.ascent))
                    
                    scope.drawIntoCanvas { c ->
                        paint.color = color.toArgb()
                        val fm = paint.fontMetrics
                        val y = centerY - (fm.ascent + fm.descent) / 2
                        c.nativeCanvas.drawText(bracket, x, y, paint)
                    }
                    
                    paint.textSize = savedSize
                }
            }
        }
    }

    /** Биномиальный коэффициент */
    class BinomBox(
        private val n: Box,
        private val k: Box,
        private val gap: Float
    ) : Box(
        width = maxOf(n.width, k.width) + gap * 4,
        ascent = n.height + gap + n.axisHeight,
        descent = k.height + gap - n.axisHeight,
        axisHeight = n.axisHeight
    ) {
        override fun draw(scope: DrawScope, x: Float, yBaseline: Float, color: Color, paint: Paint) {
            val centerX = x + width / 2
            
            // n сверху
            val nX = centerX - n.width / 2
            val nY = yBaseline - axisHeight - gap - n.descent
            n.draw(scope, nX, nY, color, paint)
            
            // k снизу
            val kX = centerX - k.width / 2
            val kY = yBaseline - axisHeight + gap + k.ascent
            k.draw(scope, kX, kY, color, paint)
            
            // Рисуем скобки
            drawBinomBracket(scope, x, yBaseline - axisHeight, height, true, color)
            drawBinomBracket(scope, x + width - gap, yBaseline - axisHeight, height, false, color)
        }
        
        private fun drawBinomBracket(
            scope: DrawScope,
            x: Float,
            centerY: Float,
            height: Float,
            isLeft: Boolean,
            color: Color
        ) {
            val path = Path()
            val halfHeight = height / 2
            
            if (isLeft) {
                path.moveTo(x + 8f, centerY - halfHeight)
                path.cubicTo(
                    x, centerY - halfHeight * 0.5f,
                    x, centerY + halfHeight * 0.5f,
                    x + 8f, centerY + halfHeight
                )
            } else {
                path.moveTo(x - 8f, centerY - halfHeight)
                path.cubicTo(
                    x, centerY - halfHeight * 0.5f,
                    x, centerY + halfHeight * 0.5f,
                    x - 8f, centerY + halfHeight
                )
            }
            
            scope.drawPath(path, color, style = Stroke(width = 2f))
        }
    }

    /** Надскобка/подскобка */
    class BraceBox(
        private val content: Box,
        private val annotation: Box,
        private val isOver: Boolean,
        private val gap: Float
    ) : Box(
        width = maxOf(content.width, annotation.width + gap * 2),
        ascent = if (isOver) content.ascent + annotation.height + gap * 3 else content.ascent,
        descent = if (!isOver) content.descent + annotation.height + gap * 3 else content.descent,
        axisHeight = content.axisHeight
    ) {
        override fun draw(scope: DrawScope, x: Float, yBaseline: Float, color: Color, paint: Paint) {
            // content по центру
            val contentX = x + (width - content.width) / 2
            content.draw(scope, contentX, yBaseline, color, paint)
            
            // annotation
            val annotX = x + (width - annotation.width) / 2
            if (isOver) {
                val braceY = yBaseline - content.ascent - gap
                val annotY = braceY - gap - annotation.descent
                annotation.draw(scope, annotX, annotY, color, paint)
                // Рисуем фигурную скобку над content
                drawBrace(scope, contentX, braceY, content.width, gap * 2, true, color)
            } else {
                val braceY = yBaseline + content.descent + gap
                val annotY = braceY + gap + annotation.ascent
                annotation.draw(scope, annotX, annotY, color, paint)
                // Рисуем фигурную скобку под content
                drawBrace(scope, contentX, braceY, content.width, gap * 2, false, color)
            }
        }
        
        private fun drawBrace(
            scope: DrawScope,
            x: Float,
            y: Float,
            width: Float,
            height: Float,
            isOver: Boolean,
            color: Color
        ) {
            val path = Path()
            val midX = x + width / 2
            
            if (isOver) {
                path.moveTo(x, y)
                path.cubicTo(x, y - height, midX - width * 0.1f, y - height, midX, y - height * 0.5f)
                path.cubicTo(midX + width * 0.1f, y - height, x + width, y - height, x + width, y)
            } else {
                path.moveTo(x, y)
                path.cubicTo(x, y + height, midX - width * 0.1f, y + height, midX, y + height * 0.5f)
                path.cubicTo(midX + width * 0.1f, y + height, x + width, y + height, x + width, y)
            }
            
            scope.drawPath(path, color, style = Stroke(width = 2f))
        }
    }

    /** Cases (piecewise) */
    class CasesBox(
        private val rows: List<Pair<Box, Box>>,
        private val gap: Float,
        private val paint: Paint
    ) : Box(
        width = (rows.maxOfOrNull { it.first.width + gap * 2 + it.second.width } ?: 0f) + gap * 4,
        ascent = run {
            if (rows.isEmpty()) 0f else {
                val totalHeight = rows.sumOf { (l, r) -> 
                    maxOf(l.height, r.height).toDouble()
                }.toFloat() + (rows.size - 1) * gap
                val firstAxis = rows.firstOrNull()?.first?.axisHeight ?: 0f
                totalHeight / 2 + firstAxis
            }
        },
        descent = run {
            if (rows.isEmpty()) 0f else {
                val totalHeight = rows.sumOf { (l, r) -> 
                    maxOf(l.height, r.height).toDouble()
                }.toFloat() + (rows.size - 1) * gap
                val firstAxis = rows.firstOrNull()?.first?.axisHeight ?: 0f
                totalHeight / 2 - firstAxis
            }
        },
        axisHeight = rows.firstOrNull()?.first?.axisHeight ?: 0f
    ) {
        override fun draw(scope: DrawScope, x: Float, yBaseline: Float, color: Color, paint: Paint) {
            // Фигурная скобка слева
            drawLeftBrace(scope, x + gap, yBaseline - axisHeight, height, color)
            
            var rowY = yBaseline - ascent + gap
            rows.forEach { (left, right) ->
                val rowHeight = maxOf(left.height, right.height)
                val rowBaseline = rowY + maxOf(left.ascent, right.ascent)
                
                left.draw(scope, x + gap * 3, rowBaseline, color, paint)
                right.draw(scope, x + gap * 3 + left.width + gap * 2, rowBaseline, color, paint)
                
                rowY += rowHeight + gap
            }
        }
        
        private fun drawLeftBrace(scope: DrawScope, x: Float, centerY: Float, height: Float, color: Color) {
            val path = Path()
            val top = centerY - height / 2
            val bottom = centerY + height / 2
            val width = 10f
            
            path.moveTo(x + width, top)
            path.cubicTo(
                x, top + height * 0.1f,
                x, centerY - height * 0.1f,
                x + width * 0.5f, centerY
            )
            path.cubicTo(
                x, centerY + height * 0.1f,
                x, bottom - height * 0.1f,
                x + width, bottom
            )
            
            scope.drawPath(path, color, style = Stroke(width = 2f))
        }
    }

    /** Масштабируемая скобка (для \left/\right) */
    class StretchyBracketBox(
        private val bracket: String,
        private val contentHeight: Float,
        private val paint: Paint
    ) : Box(
        width = if (bracket in listOf("(", ")", "[", "]", "{", "}")) 10f else paint.measureText(bracket),
        ascent = contentHeight / 2,
        descent = contentHeight / 2,
        axisHeight = 0f
    ) {
        override fun draw(scope: DrawScope, x: Float, yBaseline: Float, color: Color, paint: Paint) {
            val centerY = yBaseline
            val halfHeight = contentHeight / 2
            
            val path = Path()
            when (bracket) {
                "(" -> {
                    path.moveTo(x + 8f, centerY - halfHeight)
                    path.cubicTo(
                        x, centerY - halfHeight * 0.5f,
                        x, centerY + halfHeight * 0.5f,
                        x + 8f, centerY + halfHeight
                    )
                }
                ")" -> {
                    path.moveTo(x + 2f, centerY - halfHeight)
                    path.cubicTo(
                        x + 10f, centerY - halfHeight * 0.5f,
                        x + 10f, centerY + halfHeight * 0.5f,
                        x + 2f, centerY + halfHeight
                    )
                }
                "[" -> {
                    path.moveTo(x + 8f, centerY - halfHeight)
                    path.lineTo(x + 2f, centerY - halfHeight)
                    path.lineTo(x + 2f, centerY + halfHeight)
                    path.lineTo(x + 8f, centerY + halfHeight)
                }
                "]" -> {
                    path.moveTo(x + 2f, centerY - halfHeight)
                    path.lineTo(x + 8f, centerY - halfHeight)
                    path.lineTo(x + 8f, centerY + halfHeight)
                    path.lineTo(x + 2f, centerY + halfHeight)
                }
                "|" -> {
                    path.moveTo(x + 5f, centerY - halfHeight)
                    path.lineTo(x + 5f, centerY + halfHeight)
                }
                else -> {
                    // Для остальных символов используем текст
                    val savedSize = paint.textSize
                    val scale = contentHeight / (paint.fontMetrics.descent - paint.fontMetrics.ascent)
                    paint.textSize = savedSize * min(scale, 3f) // Ограничиваем масштаб
                    
                    scope.drawIntoCanvas { c ->
                        paint.color = color.toArgb()
                        val fm = paint.fontMetrics
                        val y = centerY - (fm.ascent + fm.descent) / 2
                        c.nativeCanvas.drawText(bracket, x, y, paint)
                    }
                    
                    paint.textSize = savedSize
                    return
                }
            }
            
            scope.drawPath(path, color, style = Stroke(width = 2f))
        }
    }

    /** Горизонтальный пробел фиксированной ширины */
    class HSpace(width: Float) : Box(width, 0f, 0f, 0f) {
        override fun draw(scope: DrawScope, x: Float, yBaseline: Float, color: Color, paint: Paint) {
            // Ничего не рисуем, только занимаем место
        }
    }
}

private fun Color.toArgb() = android.graphics.Color.argb(alpha, red, green, blue)