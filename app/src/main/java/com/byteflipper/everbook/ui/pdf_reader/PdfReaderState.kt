/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.ui.pdf_reader

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Immutable
import com.byteflipper.everbook.domain.library.book.Book
import com.byteflipper.everbook.domain.ui.UIText
import com.byteflipper.everbook.domain.util.BottomSheet
import com.byteflipper.everbook.presentation.core.constants.provideEmptyBook

@Immutable
data class PdfReaderState(
    val book: Book = provideEmptyBook(),
    val pageCount: Int = 0,
    val listState: LazyListState = LazyListState(),
    val isLoading: Boolean = true,
    val errorMessage: UIText? = null,
    val showMenu: Boolean = false,
    val bottomSheet: BottomSheet? = null,
    val zoom: Float = 1f,
    val zoomPageIndex: Int = 0,
    val zoomSessionId: Long = 0L
)
