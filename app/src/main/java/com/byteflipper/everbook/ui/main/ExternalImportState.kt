/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.ui.main

import androidx.compose.runtime.Immutable
import com.byteflipper.everbook.domain.library.book.SelectableNullableBook

@Immutable
data class ExternalImportState(
    val showAddDialog: Boolean = false,
    val loadingAddDialog: Boolean = false,
    val booksAddDialog: List<SelectableNullableBook> = emptyList()
)
