/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.ui.book_info

import androidx.compose.runtime.Immutable
import com.byteflipper.everbook.domain.library.book.Book
import com.byteflipper.everbook.domain.util.BottomSheet
import com.byteflipper.everbook.domain.util.Dialog
import com.byteflipper.everbook.presentation.core.constants.provideEmptyBook

@Immutable
data class BookInfoState(
    val book: Book = provideEmptyBook(),

    val canResetCover: Boolean = false,

    val dialog: Dialog? = null,
    val bottomSheet: BottomSheet? = null
)