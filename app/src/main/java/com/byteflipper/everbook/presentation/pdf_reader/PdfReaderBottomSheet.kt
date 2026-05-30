/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.pdf_reader

import androidx.compose.runtime.Composable
import com.byteflipper.everbook.domain.library.book.Book
import com.byteflipper.everbook.domain.reader.PdfReadingMode
import com.byteflipper.everbook.domain.util.BottomSheet
import com.byteflipper.everbook.presentation.core.util.LocalActivity
import com.byteflipper.everbook.presentation.reader.PdfReadingModeBottomSheet
import com.byteflipper.everbook.ui.pdf_reader.PdfReaderEvent
import com.byteflipper.everbook.ui.reader.ReaderScreen

@Composable
fun PdfReaderBottomSheet(
    book: Book,
    bottomSheet: BottomSheet?,
    fullscreenMode: Boolean,
    pdfTextModeUnavailable: Boolean,
    changePdfReadingMode: (PdfReaderEvent.OnChangePdfReadingMode) -> Unit,
    changePdfDefaultReadingMode: (PdfReadingMode) -> Unit,
    menuVisibility: (PdfReaderEvent.OnMenuVisibility) -> Unit,
    dismissBottomSheet: (PdfReaderEvent.OnDismissBottomSheet) -> Unit
) {
    val activity = LocalActivity.current

    when (bottomSheet) {
        ReaderScreen.SETTINGS_BOTTOM_SHEET -> {
            PdfReaderSettingsBottomSheet(
                fullscreenMode = fullscreenMode,
                menuVisibility = { show, fullscreen ->
                    menuVisibility(
                        PdfReaderEvent.OnMenuVisibility(
                            show = show,
                            fullscreenMode = fullscreen,
                            activity = activity
                        )
                    )
                },
                dismissBottomSheet = {
                    dismissBottomSheet(PdfReaderEvent.OnDismissBottomSheet)
                }
            )
        }

        ReaderScreen.PDF_READING_MODE_BOTTOM_SHEET -> {
            PdfReadingModeBottomSheet(
                book = book,
                pdfTextModeUnavailable = pdfTextModeUnavailable,
                changePdfReadingMode = {
                    changePdfReadingMode(PdfReaderEvent.OnChangePdfReadingMode(it))
                },
                changePdfDefaultReadingMode = changePdfDefaultReadingMode,
                dismissBottomSheet = {
                    dismissBottomSheet(PdfReaderEvent.OnDismissBottomSheet)
                }
            )
        }
    }
}
