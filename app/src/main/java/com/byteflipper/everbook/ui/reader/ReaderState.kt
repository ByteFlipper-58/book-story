/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.ui.reader

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Immutable
import com.byteflipper.everbook.domain.library.book.Book
import com.byteflipper.everbook.domain.reader.Checkpoint
import com.byteflipper.everbook.domain.reader.ReaderText
import com.byteflipper.everbook.domain.reader.ReaderText.Chapter
import com.byteflipper.everbook.domain.ui.UIText
import com.byteflipper.everbook.domain.util.BottomSheet
import com.byteflipper.everbook.domain.util.Drawer
import com.byteflipper.everbook.presentation.core.constants.provideEmptyBook

@Immutable
data class ReaderState(
    val book: Book = provideEmptyBook(),
    val originalText: List<ReaderText> = emptyList(),
    val text: List<ReaderText> = emptyList(),
    val chapters: List<Chapter> = emptyList(),
    val chapterIndexes: List<Int> = emptyList(),
    val listState: LazyListState = LazyListState(),

    val currentChapter: Chapter? = null,
    val currentChapterProgress: Float = 0f,

    val errorMessage: UIText? = null,
    val isLoading: Boolean = true,
    val isParsing: Boolean = false,
    val pdfTextModeUnavailable: Boolean = false,

    val showMenu: Boolean = false,
    val checkpoint: Checkpoint = Checkpoint(0, 0),
    val lockMenu: Boolean = false,

    val bottomSheet: BottomSheet? = null,
    val drawer: Drawer? = null,

    val translation: ReaderTranslationState = ReaderTranslationState(),
    val bookTranslation: ReaderBookTranslationState = ReaderBookTranslationState()
)
