/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.reader

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.byteflipper.everbook.domain.reader.HighlightPalette

/**
 * Palette backing text highlights (Bookmark.colorArgb). Colors are stored opaque in the database;
 * the reader paints the running text with [backgroundAlpha] applied so the underlying glyphs stay
 * legible on both light and dark backgrounds, while the drawer swatches show the color at full
 * strength.
 */
object ReaderHighlightColors {

    /** Opacity used when painting a highlight behind reader text. */
    const val backgroundAlpha: Float = 0.4f

    val defaultPalette: List<Color> = HighlightPalette.defaultArgbPalette.map(::Color)

    val palette: List<Color> = defaultPalette

    val defaultArgb: Int = palette.first().toArgb()
    val defaultArgbPalette: List<Int> = defaultPalette.map { it.toArgb() }

    /** Resolves a stored ARGB value to a palette [Color], falling back to the first swatch. */
    fun colorFor(argb: Int?): Color = argb?.let { Color(it) } ?: palette.first()
}
