/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.reader

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.Density
import com.byteflipper.everbook.math.FormulaCache
import com.byteflipper.everbook.math.PaintProvider
import com.byteflipper.everbook.math.Layout
import com.byteflipper.everbook.math.Lexer
import com.byteflipper.everbook.math.Parser
import androidx.compose.material3.Text
import androidx.compose.ui.platform.LocalContext

/**
 * Рендер LaTeX-формулы с использованием нового движка.
 */
@Composable
internal fun ReaderLayoutMath(
    latex: String,
    fontColor: Color,
    fontSize: TextUnit,
    sidePadding: Dp,
    inline: Boolean = false,
    textAlign: TextAlign = TextAlign.Start
) {
    if (!com.byteflipper.everbook.math.MathConfig.enabled) {
        Text(
            text = "$${latex}$",
            color = fontColor,
            fontSize = fontSize,
            modifier = if (inline) Modifier else Modifier.padding(horizontal = sidePadding)
        )
        return
    }

    val density = androidx.compose.ui.platform.LocalDensity.current
    val context = LocalContext.current
    val fontPx = with(density) { fontSize.toPx() }
    
    val mainPaint = PaintProvider.get(fontPx.toInt(), context)
    val scriptPaint = PaintProvider.get((fontPx * 0.7f).toInt(), context)
    
    val box = FormulaCache.getOrBuild(latex, fontPx.toInt(), mainPaint, scriptPaint)

    val baseModifier = if (inline) {
        Modifier
            .width(with(density) { box.width.toDp() })
            .height(with(density) { box.height.toDp() })
    } else {
        Modifier
            .fillMaxWidth()
            .height(with(density) { box.height.toDp() })
            .padding(horizontal = sidePadding)
    }

    Canvas(
        modifier = baseModifier
    ) {
        val x = if (inline) {
            0f
        } else {
            when (textAlign) {
                TextAlign.Center -> (size.width - box.width) / 2
                TextAlign.End -> size.width - box.width
                else -> 0f
            }
        }
        box.draw(this, x, box.ascent, fontColor, mainPaint)
    }
}

private fun TextUnit.toPx(density: Density) = with(density) { this@toPx.toPx() } 