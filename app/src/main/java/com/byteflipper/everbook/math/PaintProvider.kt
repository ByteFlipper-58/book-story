/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.math

import android.content.Context
import android.graphics.Paint
import androidx.core.content.res.ResourcesCompat
import com.byteflipper.everbook.R
import java.util.concurrent.ConcurrentHashMap

/**
 * Глобальный пул [Paint] по ключу размера шрифта (px).
 * Избегаем создания тысяч объектов и утечек нативных ресурсов.
 */
internal object PaintProvider {
    private val cache = ConcurrentHashMap<Int, Paint>()

    fun get(fontPx: Int, context: Context): Paint {
        return cache.getOrPut(fontPx) {
            val typeface = ResourcesCompat.getFont(context, R.font.noto_sans_math_regular)
                ?: android.graphics.Typeface.DEFAULT
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                isSubpixelText = true
                textSize = fontPx.toFloat()
                this.typeface = typeface
            }
        }
    }
} 