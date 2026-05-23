/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.domain.reader

import androidx.compose.runtime.Immutable

@Immutable
enum class PdfReadingMode {
    PARSED_TEXT,
    ORIGINAL_PDF
}

fun String.toPdfReadingMode(): PdfReadingMode {
    return PdfReadingMode.entries.find { it.name == this } ?: PdfReadingMode.PARSED_TEXT
}
