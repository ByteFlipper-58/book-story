/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.domain.reader

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.AnnotatedString
import java.util.UUID

@Immutable
sealed class ReaderText {
    @Immutable
    data class Chapter(
        val id: UUID = UUID.randomUUID(),
        val title: String,
        val nested: Boolean
    ) : ReaderText()

    @Immutable
    data class Text(val line: AnnotatedString) : ReaderText()

    @Immutable
    data object Separator : ReaderText()

    @Immutable
    data class Image(
        val imageBitmap: ImageBitmap
    ) : ReaderText()

    /**
     * Представление LaTeX-формулы как отдельного элемента текста.
     * Поскольку формула будет рендериться кастомным Canvas, здесь хранится
     * только исходная строка LaTeX без ограждающих `$`.
     */
    @Immutable
    data class Math(val latex: String) : ReaderText()
}