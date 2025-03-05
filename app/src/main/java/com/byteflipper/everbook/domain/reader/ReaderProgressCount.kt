/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.domain.reader

import androidx.compose.runtime.Immutable

@Immutable
enum class ReaderProgressCount {
    PERCENTAGE,
    QUANTITY
}

fun String.toProgressCount(): ReaderProgressCount {
    return ReaderProgressCount.valueOf(this)
}