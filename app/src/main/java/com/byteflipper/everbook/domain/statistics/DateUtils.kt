/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.domain.statistics

import java.time.LocalDate

fun alignToWeekStart(date: LocalDate, weekStartsMonday: Boolean): LocalDate {
    val back = if (weekStartsMonday) date.dayOfWeek.value - 1 else date.dayOfWeek.value % 7
    return date.minusDays(back.toLong())
}
