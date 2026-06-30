/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.domain.statistics

import androidx.compose.runtime.Immutable

@Immutable
enum class StatisticsRange {
    WEEK,
    MONTH,
    THREE_MONTHS,
    YEAR,
    ALL_TIME
}

fun decodeStatisticsRange(raw: String?): StatisticsRange {
    raw?.let { value ->
        StatisticsRange.entries.firstOrNull { it.name == value }?.let { return it }
    }
    return StatisticsRange.WEEK
}
