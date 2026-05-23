/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.ui.pdf_reader

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Immutable
import com.byteflipper.everbook.domain.reader.PdfReadingMode

@Immutable
sealed class PdfReaderEvent {
    data class OnMenuVisibility(
        val show: Boolean,
        val fullscreenMode: Boolean,
        val activity: ComponentActivity
    ) : PdfReaderEvent()

    data class OnChangeProgress(
        val pageIndex: Int,
        val pageOffset: Int
    ) : PdfReaderEvent()

    data class OnScrollToPage(
        val pageIndex: Int
    ) : PdfReaderEvent()

    data class OnChangeZoom(
        val pageIndex: Int,
        val zoom: Float
    ) : PdfReaderEvent()

    data class OnChangePdfReadingMode(
        val mode: PdfReadingMode
    ) : PdfReaderEvent()

    data object OnShowPdfReadingModeBottomSheet : PdfReaderEvent()

    data object OnShowSettingsBottomSheet : PdfReaderEvent()

    data object OnDismissBottomSheet : PdfReaderEvent()

    data class OnLeave(
        val activity: ComponentActivity,
        val navigate: () -> Unit
    ) : PdfReaderEvent()
}
