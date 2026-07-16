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
import com.byteflipper.everbook.domain.reader.Bookmark
import com.byteflipper.everbook.domain.reader.Checkpoint
import com.byteflipper.everbook.domain.reader.HighlightPalette
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

    val bookmarks: List<Bookmark> = emptyList(),
    val editingAnnotation: Bookmark? = null,
    val pendingAnnotationText: String? = null,
    val pendingAnnotationColorArgb: Int? = null,
    val highlightPaletteText: String? = null,
    val highlightPaletteAnnotation: Bookmark? = null,
    val highlightPaletteAnchorX: Int = 0,
    val highlightPaletteAnchorY: Int = 0,
    val highlightColors: List<Int> = HighlightPalette.defaultArgbPalette,
    val showHighlightPaletteEditor: Boolean = false,
    val highlightPaletteEditorTarget: HighlightPaletteTarget? = null,
    // Bookmark temporarily emphasized after navigation from the annotations drawer.
    val focusedBookmarkId: Int? = null,
    // Navigation is animated by ReaderContent, whose coroutine owns a Compose frame clock.
    val pendingBookmarkNavigation: Bookmark? = null,
    val pendingBookmarkDisplayIndex: Int? = null,

    val translation: ReaderTranslationState = ReaderTranslationState(),
    val bookTranslation: ReaderBookTranslationState = ReaderBookTranslationState()
)

data class HighlightPaletteTarget(
    val selectedText: String,
    val annotation: Bookmark?
)
