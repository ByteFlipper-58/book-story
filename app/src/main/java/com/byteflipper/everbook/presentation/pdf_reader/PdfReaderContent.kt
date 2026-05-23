/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.pdf_reader

import android.graphics.Bitmap
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.byteflipper.everbook.domain.library.book.Book
import com.byteflipper.everbook.domain.reader.PdfPageDisplayMode
import com.byteflipper.everbook.domain.reader.PdfReadingMode
import com.byteflipper.everbook.domain.ui.UIText
import com.byteflipper.everbook.domain.util.BottomSheet
import com.byteflipper.everbook.ui.pdf_reader.PdfReaderEvent

@Composable
fun PdfReaderContent(
    book: Book,
    pageCount: Int,
    listState: LazyListState,
    isLoading: Boolean,
    errorMessage: UIText?,
    showMenu: Boolean,
    bottomSheet: BottomSheet?,
    zoom: Float,
    zoomPageIndex: Int,
    zoomSessionId: Long,
    pdfTextModeUnavailable: Boolean,
    contentPadding: PaddingValues,
    bottomBarPadding: Dp,
    backgroundColor: Color,
    pageDisplayMode: PdfPageDisplayMode,
    showZoomControls: Boolean,
    pinchZoom: Boolean,
    fullscreenMode: Boolean,
    renderPage: suspend (pageIndex: Int, targetWidth: Int) -> Bitmap?,
    menuVisibility: (PdfReaderEvent.OnMenuVisibility) -> Unit,
    scrollToPage: (PdfReaderEvent.OnScrollToPage) -> Unit,
    changeZoom: (PdfReaderEvent.OnChangeZoom) -> Unit,
    changePdfReadingMode: (PdfReaderEvent.OnChangePdfReadingMode) -> Unit,
    changePdfDefaultReadingMode: (PdfReadingMode) -> Unit,
    showPdfReadingModeBottomSheet: (PdfReaderEvent.OnShowPdfReadingModeBottomSheet) -> Unit,
    showSettingsBottomSheet: (PdfReaderEvent.OnShowSettingsBottomSheet) -> Unit,
    dismissBottomSheet: (PdfReaderEvent.OnDismissBottomSheet) -> Unit,
    leave: (PdfReaderEvent.OnLeave) -> Unit,
    navigateToBookInfo: () -> Unit,
    navigateBack: () -> Unit
) {
    PdfReaderBottomSheet(
        book = book,
        bottomSheet = bottomSheet,
        fullscreenMode = fullscreenMode,
        pdfTextModeUnavailable = pdfTextModeUnavailable,
        changePdfReadingMode = changePdfReadingMode,
        changePdfDefaultReadingMode = changePdfDefaultReadingMode,
        menuVisibility = menuVisibility,
        dismissBottomSheet = dismissBottomSheet
    )

    if (isLoading || errorMessage == null) {
        PdfReaderScaffold(
            book = book,
            pageCount = pageCount,
            listState = listState,
            isLoading = isLoading,
            showMenu = showMenu,
            zoom = zoom,
            zoomPageIndex = zoomPageIndex,
            zoomSessionId = zoomSessionId,
            contentPadding = contentPadding,
            bottomBarPadding = bottomBarPadding,
            backgroundColor = backgroundColor,
            pageDisplayMode = pageDisplayMode,
            showZoomControls = showZoomControls,
            pinchZoom = pinchZoom,
            fullscreenMode = fullscreenMode,
            renderPage = renderPage,
            menuVisibility = menuVisibility,
            scrollToPage = scrollToPage,
            changeZoom = changeZoom,
            showPdfReadingModeBottomSheet = showPdfReadingModeBottomSheet,
            showSettingsBottomSheet = showSettingsBottomSheet,
            leave = leave,
            navigateToBookInfo = navigateToBookInfo,
            navigateBack = navigateBack
        )
    } else {
        PdfReaderErrorPlaceholder(
            errorMessage = errorMessage,
            leave = leave,
            navigateToBookInfo = navigateToBookInfo,
            navigateBack = navigateBack
        )
    }

    PdfReaderBackHandler(
        leave = leave,
        navigateBack = navigateBack
    )
}
