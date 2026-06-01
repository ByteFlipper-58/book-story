/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.pdf_reader

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.View
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.byteflipper.everbook.domain.distribution.ReaderInlineContentPlacement
import com.byteflipper.everbook.domain.library.book.Book
import com.byteflipper.everbook.domain.reader.PdfPageDisplayMode
import com.byteflipper.everbook.presentation.core.components.common.AnimatedVisibility
import com.byteflipper.everbook.ui.pdf_reader.PdfReaderEvent
import com.byteflipper.everbook.ui.pdf_reader.PdfReaderModel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun PdfReaderScaffold(
    book: Book,
    pageCount: Int,
    listState: LazyListState,
    isLoading: Boolean,
    showMenu: Boolean,
    zoom: Float,
    zoomPageIndex: Int,
    zoomSessionId: Long,
    contentPadding: PaddingValues,
    bottomBarPadding: Dp,
    backgroundColor: Color,
    pageDisplayMode: PdfPageDisplayMode,
    showZoomControls: Boolean,
    pinchZoom: Boolean,
    fullscreenMode: Boolean,
    inlineContentPlacements: List<ReaderInlineContentPlacement>,
    createInlineContentView: (Long) -> View?,
    renderPage: suspend (pageIndex: Int, targetWidth: Int) -> Bitmap?,
    menuVisibility: (PdfReaderEvent.OnMenuVisibility) -> Unit,
    scrollToPage: (PdfReaderEvent.OnScrollToPage) -> Unit,
    changeZoom: (PdfReaderEvent.OnChangeZoom) -> Unit,
    showPdfReadingModeBottomSheet: (PdfReaderEvent.OnShowPdfReadingModeBottomSheet) -> Unit,
    showSettingsBottomSheet: (PdfReaderEvent.OnShowSettingsBottomSheet) -> Unit,
    leave: (PdfReaderEvent.OnLeave) -> Unit,
    navigateToBookInfo: () -> Unit,
    navigateBack: () -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            AnimatedVisibility(
                visible = showMenu,
                enter = slideInVertically { -it },
                exit = slideOutVertically { -it }
            ) {
                PdfReaderTopBar(
                    book = book,
                    showPdfReadingModeBottomSheet = showPdfReadingModeBottomSheet,
                    showSettingsBottomSheet = showSettingsBottomSheet,
                    leave = leave,
                    navigateBack = navigateBack,
                    navigateToBookInfo = navigateToBookInfo
                )
            }
        },
        bottomBar = {
            AnimatedVisibility(
                modifier = Modifier.fillMaxWidth(),
                visible = showMenu && pageCount > 0,
                enter = slideInVertically { it },
                exit = slideOutVertically { it }
            ) {
                PdfReaderBottomBar(
                    pageIndex = listState.firstVisibleItemIndex,
                    pageCount = pageCount,
                    zoom = if (zoomPageIndex == listState.firstVisibleItemIndex) {
                        zoom
                    } else PdfReaderModel.MIN_ZOOM,
                    bottomBarPadding = bottomBarPadding,
                    showZoomControls = showZoomControls,
                    scrollToPage = scrollToPage,
                    changeZoom = changeZoom
                )
            }
        }
    ) {
        if (isLoading) {
            PdfReaderLoadingPlaceholder()
        } else {
            PdfReaderLayout(
                pageCount = pageCount,
                listState = listState,
                showMenu = showMenu,
                zoom = zoom,
                zoomPageIndex = zoomPageIndex,
                zoomSessionId = zoomSessionId,
                contentPadding = contentPadding,
                backgroundColor = backgroundColor,
                pageDisplayMode = pageDisplayMode,
                pinchZoom = pinchZoom,
                fullscreenMode = fullscreenMode,
                inlineContentPlacements = inlineContentPlacements,
                createInlineContentView = createInlineContentView,
                renderPage = renderPage,
                changeZoom = changeZoom,
                menuVisibility = menuVisibility
            )
        }
    }
}
