/*
 * EverBook - a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.domain.reader

fun List<ReaderText>.hasReadableReaderText(requireChapter: Boolean = true): Boolean {
    var hasText = false
    var hasChapter = !requireChapter

    for (entry in this) {
        when (entry) {
            is ReaderText.Text -> hasText = true
            is ReaderText.Chapter -> hasChapter = true
            else -> Unit
        }

        if (hasText && hasChapter) return true
    }

    return false
}
