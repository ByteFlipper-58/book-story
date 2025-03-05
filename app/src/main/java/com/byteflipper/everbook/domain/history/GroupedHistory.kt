/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.domain.history

import androidx.compose.runtime.Immutable

@Immutable
data class GroupedHistory(
    val title: String,
    val history: List<History>
)
