/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.reader

import android.view.View
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
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
import com.byteflipper.everbook.domain.reader.PdfReadingMode
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
import com.byteflipper.everbook.ui.reader.ReaderBookTranslationState
import com.byteflipper.everbook.ui.settings.SettingsEvent

@Composable
fun ReaderContent(
    book: Book,
    text: List<ReaderText>,
    displayContent: ReaderDisplayContent,
    chapters: List<Chapter>,
    bottomSheet: BottomSheet?,
    translation: com.byteflipper.everbook.ui.reader.ReaderTranslationState,
    bookTranslation: ReaderBookTranslationState,
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
    isParsing: Boolean,
    errorMessage: UIText?,
    pdfTextModeUnavailable: Boolean,
    checkpoint: Checkpoint,
    showMenu: Boolean,
    lockMenu: Boolean,
    contentPadding: PaddingValues,
    verticalPadding: Dp,
    horizontalGesture: ReaderHorizontalGesture,
    horizontalGestureScroll: Float,
    horizontalGestureSensitivity: Dp,
    horizontalGestureAlphaAnim: Boolean,
    horizontalGesturePullAnim: Boolean,
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
    translationProviderMode: String,
    translationSourceLanguage: String,
    translationTargetLanguage: String,
    translationWifiOnly: Boolean,
    fullscreenMode: Boolean,
    createInlineContentView: (Long) -> View?,
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
    translateText: (ReaderEvent.OnTranslateText) -> Unit,
    openExternalTranslator: (ReaderEvent.OnOpenExternalTranslator) -> Unit,
    dismissTranslation: (ReaderEvent.OnDismissTranslation) -> Unit,
    toggleTranslationOriginal: (ReaderEvent.OnToggleTranslationOriginal) -> Unit,
    openDictionary: (ReaderEvent.OnOpenDictionary) -> Unit,
    scrollToChapter: (ReaderEvent.OnScrollToChapter) -> Unit,
    showPdfReadingModeBottomSheet: (ReaderEvent.OnShowPdfReadingModeBottomSheet) -> Unit,
    showBookTranslationBottomSheet: (ReaderEvent.OnShowBookTranslationBottomSheet) -> Unit,
    showSettingsBottomSheet: (ReaderEvent.OnShowSettingsBottomSheet) -> Unit,
    dismissBottomSheet: (ReaderEvent.OnDismissBottomSheet) -> Unit,
    startBookTranslation: (ReaderEvent.OnStartBookTranslation) -> Unit,
    showTranslatedBook: (ReaderEvent.OnShowTranslatedBook) -> Unit,
    showOriginalBook: (ReaderEvent.OnShowOriginalBook) -> Unit,
    confirmBookTranslationGoogleWarning: (ReaderEvent.OnConfirmBookTranslationGoogleWarning) -> Unit,
    dismissBookTranslationGoogleWarning: (ReaderEvent.OnDismissBookTranslationGoogleWarning) -> Unit,
    cancelBookTranslation: (ReaderEvent.OnCancelBookTranslation) -> Unit,
    pauseBookTranslation: (ReaderEvent.OnPauseBookTranslation) -> Unit,
    resumeBookTranslation: (ReaderEvent.OnResumeBookTranslation) -> Unit,
    retryBookTranslation: (ReaderEvent.OnRetryBookTranslation) -> Unit,
    changeBookTranslationProviderMode: (ReaderEvent.OnChangeBookTranslationProviderMode) -> Unit,
    changeBookTranslationSourceLanguage: (ReaderEvent.OnChangeBookTranslationSourceLanguage) -> Unit,
    changeBookTranslationTargetLanguage: (ReaderEvent.OnChangeBookTranslationTargetLanguage) -> Unit,
    swapBookTranslationLanguages: (ReaderEvent.OnSwapBookTranslationLanguages) -> Unit,
    changeBookTranslationWifiOnly: (ReaderEvent.OnChangeBookTranslationWifiOnly) -> Unit,
    dismissBookTranslationError: (ReaderEvent.OnDismissBookTranslationError) -> Unit,
    showChaptersDrawer: (ReaderEvent.OnShowChaptersDrawer) -> Unit,
    dismissDrawer: (ReaderEvent.OnDismissDrawer) -> Unit,
    changePdfReadingMode: (ReaderEvent.OnChangePdfReadingMode) -> Unit,
    changePdfDefaultReadingMode: (PdfReadingMode) -> Unit,
    navigateToBookInfo: (changePath: Boolean) -> Unit,
    navigateBack: () -> Unit
) {
    ReaderBottomSheet(
        book = book,
        bottomSheet = bottomSheet,
        translation = translation,
        bookTranslation = bookTranslation,
        fullscreenMode = fullscreenMode,
        pdfTextModeUnavailable = pdfTextModeUnavailable,
        changePdfReadingMode = changePdfReadingMode,
        changePdfDefaultReadingMode = changePdfDefaultReadingMode,
        menuVisibility = menuVisibility,
        openExternalTranslator = openExternalTranslator,
        toggleTranslationOriginal = toggleTranslationOriginal,
        dismissTranslation = dismissTranslation,
        startBookTranslation = startBookTranslation,
        showTranslatedBook = showTranslatedBook,
        showOriginalBook = showOriginalBook,
        confirmBookTranslationGoogleWarning = confirmBookTranslationGoogleWarning,
        dismissBookTranslationGoogleWarning = dismissBookTranslationGoogleWarning,
        cancelBookTranslation = cancelBookTranslation,
        pauseBookTranslation = pauseBookTranslation,
        resumeBookTranslation = resumeBookTranslation,
        retryBookTranslation = retryBookTranslation,
        changeBookTranslationProviderMode = changeBookTranslationProviderMode,
        changeBookTranslationSourceLanguage = changeBookTranslationSourceLanguage,
        changeBookTranslationTargetLanguage = changeBookTranslationTargetLanguage,
        swapBookTranslationLanguages = swapBookTranslationLanguages,
        changeBookTranslationWifiOnly = changeBookTranslationWifiOnly,
        dismissBookTranslationError = dismissBookTranslationError,
        dismissBottomSheet = dismissBottomSheet
    )

    if (isLoading || errorMessage == null) {
        ReaderScaffold(
            book = book,
            text = text,
            displayContent = displayContent,
            listState = listState,
            currentChapter = currentChapter,
            translation = translation,
            bookTranslation = bookTranslation,
            nestedScrollConnection = nestedScrollConnection,
            fastColorPresetChange = fastColorPresetChange,
            perceptionExpander = perceptionExpander,
            perceptionExpanderPadding = perceptionExpanderPadding,
            perceptionExpanderThickness = perceptionExpanderThickness,
            currentChapterProgress = currentChapterProgress,
            isLoading = isLoading,
            isParsing = isParsing,
            checkpoint = checkpoint,
            showMenu = showMenu,
            lockMenu = lockMenu,
            contentPadding = contentPadding,
            verticalPadding = verticalPadding,
            horizontalGesture = horizontalGesture,
            horizontalGestureScroll = horizontalGestureScroll,
            horizontalGestureSensitivity = horizontalGestureSensitivity,
            horizontalGestureAlphaAnim = horizontalGestureAlphaAnim,
            horizontalGesturePullAnim = horizontalGesturePullAnim,
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
            translationProviderMode = translationProviderMode,
            translationSourceLanguage = translationSourceLanguage,
            translationTargetLanguage = translationTargetLanguage,
            translationWifiOnly = translationWifiOnly,
            fullscreenMode = fullscreenMode,
            createInlineContentView = createInlineContentView,
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
            translateText = translateText,
            openExternalTranslator = openExternalTranslator,
            dismissTranslation = dismissTranslation,
            toggleTranslationOriginal = toggleTranslationOriginal,
            openDictionary = openDictionary,
            showPdfReadingModeBottomSheet = showPdfReadingModeBottomSheet,
            showBookTranslationBottomSheet = showBookTranslationBottomSheet,
            showSettingsBottomSheet = showSettingsBottomSheet,
            showChaptersDrawer = showChaptersDrawer,
            showTranslatedBook = showTranslatedBook,
            showOriginalBook = showOriginalBook,
            changePdfReadingMode = changePdfReadingMode,
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
        chapters = chapters,
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
