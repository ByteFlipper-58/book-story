/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.domain.reader

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.style.TextAlign

@Immutable
enum class ReaderTextAlignment(val textAlignment: TextAlign) {
    START(TextAlign.Start),
    JUSTIFY(TextAlign.Justify),
    CENTER(TextAlign.Center),
    END(TextAlign.End)
}

fun String.toTextAlignment(): ReaderTextAlignment {
    return ReaderTextAlignment.valueOf(this)
}