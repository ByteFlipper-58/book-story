/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.reader

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import com.byteflipper.everbook.domain.library.book.Book
import com.byteflipper.everbook.domain.reader.Checkpoint
import com.byteflipper.everbook.domain.reader.FontWithName
import com.byteflipper.everbook.domain.reader.ReaderFontThickness
import com.byteflipper.everbook.domain.reader.ReaderHorizontalGesture
import com.byteflipper.everbook.domain.reader.ReaderText
import com.byteflipper.everbook.domain.reader.ReaderText.Chapter
import com.byteflipper.everbook.domain.reader.ReaderTextAlignment
import com.byteflipper.everbook.domain.ui.UIText
import com.byteflipper.everbook.domain.util.BottomSheet
import com.byteflipper.everbook.domain.util.Drawer
import com.byteflipper.everbook.domain.util.HorizontalAlignment
import com.byteflipper.everbook.ui.reader.ReaderEvent
import com.byteflipper.everbook.ui.settings.SettingsEvent

@Composable
fun ReaderContent(
    book: Book,
    text: List<ReaderText>,
    bottomSheet: BottomSheet?,
    drawer: Drawer?,
    listState: LazyListState,
    currentChapter: Chapter?,
    nestedScrollConnection: NestedScrollConnection,
    fastColorPresetChange: Boolean,
    perceptionExpander: Boolean,
    perceptionExpanderPadding: Dp,
    perceptionExpanderThickness: Dp,
    currentChapterProgress: Float,
    isLoading: Boolean,
    errorMessage: UIText?,
    checkpoint: Checkpoint,
    showMenu: Boolean,
    lockMenu: Boolean,
    contentPadding: PaddingValues,
    verticalPadding: Dp,
    horizontalGesture: ReaderHorizontalGesture,
    horizontalGestureScroll: Float,
    horizontalGestureSensitivity: Dp,
    horizontalGestureAlphaAnim: Float,
    highlightedReading: Boolean,
    highlightedReadingThickness: FontWeight,
    progress: String,
    progressBar: Boolean,
    progressBarPadding: Dp,
    progressBarAlignment: HorizontalAlignment,
    progressBarFontSize: TextUnit,
    paragraphHeight: Dp,
    sidePadding: Dp,
    bottomBarPadding: Dp,
    backgroundColor: Color,
    fontColor: Color,
    images: Boolean,
    imagesCornersRoundness: Dp,
    imagesAlignment: HorizontalAlignment,
    imagesWidth: Float,
    imagesColorEffects: ColorFilter?,
    fontFamily: FontWithName,
    lineHeight: TextUnit,
    fontThickness: ReaderFontThickness,
    fontStyle: FontStyle,
    chapterTitleAlignment: ReaderTextAlignment,
    textAlignment: ReaderTextAlignment,
    horizontalAlignment: Alignment.Horizontal,
    fontSize: TextUnit,
    letterSpacing: TextUnit,
    paragraphIndentation: TextUnit,
    doubleClickTranslation: Boolean,
    fullscreenMode: Boolean,
    selectPreviousPreset: (SettingsEvent.OnSelectPreviousPreset) -> Unit,
    selectNextPreset: (SettingsEvent.OnSelectNextPreset) -> Unit,
    menuVisibility: (ReaderEvent.OnMenuVisibility) -> Unit,
    leave: (ReaderEvent.OnLeave) -> Unit,
    restoreCheckpoint: (ReaderEvent.OnRestoreCheckpoint) -> Unit,
    scroll: (ReaderEvent.OnScroll) -> Unit,
    changeProgress: (ReaderEvent.OnChangeProgress) -> Unit,
    openShareApp: (ReaderEvent.OnOpenShareApp) -> Unit,
    openWebBrowser: (ReaderEvent.OnOpenWebBrowser) -> Unit,
    openTranslator: (ReaderEvent.OnOpenTranslator) -> Unit,
    openDictionary: (ReaderEvent.OnOpenDictionary) -> Unit,
    scrollToChapter: (ReaderEvent.OnScrollToChapter) -> Unit,
    showSettingsBottomSheet: (ReaderEvent.OnShowSettingsBottomSheet) -> Unit,
    dismissBottomSheet: (ReaderEvent.OnDismissBottomSheet) -> Unit,
    showChaptersDrawer: (ReaderEvent.OnShowChaptersDrawer) -> Unit,
    dismissDrawer: (ReaderEvent.OnDismissDrawer) -> Unit,
    navigateToBookInfo: (changePath: Boolean) -> Unit,
    navigateBack: () -> Unit
) {
    ReaderBottomSheet(
        bottomSheet = bottomSheet,
        fullscreenMode = fullscreenMode,
        menuVisibility = menuVisibility,
        dismissBottomSheet = dismissBottomSheet
    )

    if (isLoading || errorMessage == null) {
        ReaderScaffold(
            book = book,
            text = text,
            listState = listState,
            currentChapter = currentChapter,
            nestedScrollConnection = nestedScrollConnection,
            fastColorPresetChange = fastColorPresetChange,
            perceptionExpander = perceptionExpander,
            perceptionExpanderPadding = perceptionExpanderPadding,
            perceptionExpanderThickness = perceptionExpanderThickness,
            currentChapterProgress = currentChapterProgress,
            isLoading = isLoading,
            checkpoint = checkpoint,
            showMenu = showMenu,
            lockMenu = lockMenu,
            contentPadding = contentPadding,
            verticalPadding = verticalPadding,
            horizontalGesture = horizontalGesture,
            horizontalGestureScroll = horizontalGestureScroll,
            horizontalGestureSensitivity = horizontalGestureSensitivity,
            horizontalGestureAlphaAnim = horizontalGestureAlphaAnim,
            highlightedReading = highlightedReading,
            highlightedReadingThickness = highlightedReadingThickness,
            progress = progress,
            progressBar = progressBar,
            progressBarPadding = progressBarPadding,
            progressBarAlignment = progressBarAlignment,
            progressBarFontSize = progressBarFontSize,
            paragraphHeight = paragraphHeight,
            sidePadding = sidePadding,
            bottomBarPadding = bottomBarPadding,
            backgroundColor = backgroundColor,
            fontColor = fontColor,
            images = images,
            imagesCornersRoundness = imagesCornersRoundness,
            imagesAlignment = imagesAlignment,
            imagesWidth = imagesWidth,
            imagesColorEffects = imagesColorEffects,
            fontFamily = fontFamily,
            lineHeight = lineHeight,
            fontThickness = fontThickness,
            fontStyle = fontStyle,
            chapterTitleAlignment = chapterTitleAlignment,
            textAlignment = textAlignment,
            horizontalAlignment = horizontalAlignment,
            fontSize = fontSize,
            letterSpacing = letterSpacing,
            paragraphIndentation = paragraphIndentation,
            doubleClickTranslation = doubleClickTranslation,
            fullscreenMode = fullscreenMode,
            selectPreviousPreset = selectPreviousPreset,
            selectNextPreset = selectNextPreset,
            menuVisibility = menuVisibility,
            leave = leave,
            restoreCheckpoint = restoreCheckpoint,
            scroll = scroll,
            changeProgress = changeProgress,
            openShareApp = openShareApp,
            openWebBrowser = openWebBrowser,
            openTranslator = openTranslator,
            openDictionary = openDictionary,
            showSettingsBottomSheet = showSettingsBottomSheet,
            showChaptersDrawer = showChaptersDrawer,
            navigateBack = navigateBack,
            navigateToBookInfo = navigateToBookInfo
        )
    } else {
        ReaderErrorPlaceholder(
            errorMessage = errorMessage,
            leave = leave,
            navigateToBookInfo = navigateToBookInfo,
            navigateBack = navigateBack
        )
    }

    ReaderDrawer(
        drawer = drawer,
        chapters = remember(text) { text.filterIsInstance<Chapter>() },
        currentChapter = currentChapter,
        currentChapterProgress = currentChapterProgress,
        scrollToChapter = scrollToChapter,
        dismissDrawer = dismissDrawer
    )

    ReaderBackHandler(
        leave = leave,
        navigateBack = navigateBack
    )
}