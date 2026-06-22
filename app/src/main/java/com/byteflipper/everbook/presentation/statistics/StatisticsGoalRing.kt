/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.statistics

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.byteflipper.everbook.R
import com.byteflipper.everbook.presentation.core.components.common.StyledText

@Composable
internal fun GoalRing(
    fraction: Float,
    reached: Boolean,
    size: Dp = 80.dp,
    todayMinutes: Int? = null,
    goalMinutes: Int? = null
) {
    val cs = MaterialTheme.colorScheme
    val track = cs.surfaceContainerHighest
    val sweepColors = if (reached) {
        listOf(cs.primary, cs.tertiary, cs.primary)
    } else {
        listOf(cs.tertiary, cs.primary, cs.tertiary)
    }

    var target by remember { mutableStateOf(0f) }
    LaunchedEffect(fraction) { target = fraction }
    val animFraction by animateFloatAsState(target, tween(900), label = "ring")
    val animPercent = (animFraction * 100).toInt()
    val big = size >= 120.dp

    val topLabel = stringResource(id = R.string.statistics_daily_goal)
    val bottomLabel = stringResource(id = R.string.statistics_goal_of_minutes, goalMinutes ?: 0)
    val labelArgb = cs.onSurfaceVariant.toArgb()
    val goalArgb = cs.primary.toArgb()
    val topPaint = remember(labelArgb) {
        android.graphics.Paint().apply {
            color = labelArgb
            isAntiAlias = true
        }
    }
    val bottomPaint = remember(goalArgb) {
        android.graphics.Paint().apply {
            color = goalArgb
            isAntiAlias = true
            typeface = android.graphics.Typeface.create(
                android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD
            )
        }
    }

    Box(
        modifier = Modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = this.size.minDimension * 0.16f
            val inset = stroke / 2f
            val arcSize = Size(this.size.width - stroke, this.size.height - stroke)

            drawArc(
                color = track,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
            if (animFraction > 0f) {
                drawArc(
                    brush = Brush.sweepGradient(sweepColors),
                    startAngle = -90f,
                    sweepAngle = 360f * animFraction,
                    useCenter = false,
                    topLeft = Offset(inset, inset),
                    size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                )
            }

            if (big && todayMinutes != null && goalMinutes != null) {
                val labelSize = this.size.minDimension * 0.082f
                topPaint.textSize = labelSize
                bottomPaint.textSize = labelSize
                val textRadius = this.size.minDimension / 2f - stroke - labelSize * 0.35f
                drawCurvedText(topLabel, topPaint, textRadius, top = true, letterSpacingPx = labelSize * 0.16f)
                drawCurvedText(bottomLabel, bottomPaint, textRadius, top = false, letterSpacingPx = labelSize * 0.16f)
            }
        }
        if (big && todayMinutes != null && goalMinutes != null) {
            StyledText(
                text = "$todayMinutes",
                style = MaterialTheme.typography.displaySmall.copy(
                    color = cs.onSurface,
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 1
            )
        } else {
            StyledText(
                text = "$animPercent%",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = cs.onSurface,
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 1
            )
        }
    }
}

internal fun DrawScope.drawCurvedText(
    text: String,
    paint: android.graphics.Paint,
    radius: Float,
    top: Boolean,
    letterSpacingPx: Float = 0f
) {
    val cx = size.width / 2f
    val cy = size.height / 2f
    val oval = android.graphics.RectF(cx - radius, cy - radius, cx + radius, cy + radius)
    val path = android.graphics.Path().apply {
        if (top) addArc(oval, 180f, 180f) else addArc(oval, 180f, -180f)
    }
    val pm = android.graphics.PathMeasure(path, false)
    val measuredTextWidth = paint.measureText(text) + letterSpacingPx * (text.length - 1).coerceAtLeast(0)
    val hOffset = (pm.length - measuredTextWidth) / 2f
    val vOffset = if (top) paint.textSize * 0.7f else -paint.textSize * 0.35f
    if (letterSpacingPx <= 0f || text.length <= 1) {
        drawContext.canvas.nativeCanvas.drawTextOnPath(text, path, hOffset, vOffset, paint)
        return
    }

    var cursor = hOffset
    text.forEach { char ->
        val glyph = char.toString()
        drawContext.canvas.nativeCanvas.drawTextOnPath(glyph, path, cursor, vOffset, paint)
        cursor += paint.measureText(glyph) + letterSpacingPx
    }
}
