/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.pdf_reader

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.launch
import com.byteflipper.everbook.presentation.core.components.common.LazyColumnWithScrollbar
import com.byteflipper.everbook.presentation.core.components.modal_bottom_sheet.ModalBottomSheet
import com.byteflipper.everbook.presentation.settings.reader.pdf.PdfColorsSubcategory
import com.byteflipper.everbook.presentation.settings.reader.pdf.PdfDisplaySubcategory
import com.byteflipper.everbook.presentation.settings.reader.pdf.PdfPaddingSubcategory
import com.byteflipper.everbook.presentation.settings.reader.pdf.PdfSystemSubcategory

private var pdfSettingsInitialPage = 0

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfReaderSettingsBottomSheet(
    fullscreenMode: Boolean,
    menuVisibility: (show: Boolean, fullscreenMode: Boolean) -> Unit,
    dismissBottomSheet: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pdfSettingsInitialPage.coerceIn(0, 1)) { 2 }
    DisposableEffect(Unit) { onDispose { pdfSettingsInitialPage = pagerState.currentPage } }

    val animatedScrimColor by animateColorAsState(
        targetValue = if (pagerState.currentPage == 1) Color.Transparent
        else BottomSheetDefaults.ScrimColor,
        animationSpec = tween(300)
    )
    val animatedHeight by animateFloatAsState(
        targetValue = if (pagerState.currentPage == 1) 0.6f else 0.7f,
        animationSpec = tween(300)
    )

    LaunchedEffect(pagerState.currentPage) {
        menuVisibility(
            pagerState.currentPage != 1,
            fullscreenMode
        )
    }

    ModalBottomSheet(
        hasFixedHeight = true,
        scrimColor = animatedScrimColor,
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(animatedHeight),
        dragHandle = {},
        onDismissRequest = {
            dismissBottomSheet()
        },
        sheetGesturesEnabled = false
    ) {
        PdfReaderSettingsBottomSheetTabRow(
            currentPage = pagerState.currentPage,
            scrollToPage = {
                scope.launch {
                    pagerState.animateScrollToPage(it)
                }
            }
        )

        HorizontalPager(state = pagerState) { page ->
            when (page) {
                0 -> {
                    LazyColumnWithScrollbar(Modifier.fillMaxSize()) {
                        PdfDisplaySubcategory(
                            titleColor = { MaterialTheme.colorScheme.onSurface }
                        )
                        PdfPaddingSubcategory(
                            titleColor = { MaterialTheme.colorScheme.onSurface }
                        )
                        PdfSystemSubcategory(
                            titleColor = { MaterialTheme.colorScheme.onSurface },
                            showDivider = false
                        )
                    }
                }

                1 -> {
                    LazyColumnWithScrollbar(Modifier.fillMaxSize()) {
                        PdfColorsSubcategory(
                            showTitle = false,
                            showDivider = false,
                            backgroundColor = { MaterialTheme.colorScheme.surfaceContainer }
                        )
                    }
                }
            }
        }
    }
}
