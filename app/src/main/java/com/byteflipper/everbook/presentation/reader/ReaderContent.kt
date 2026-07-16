/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.reader

import android.view.View
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.platform.LocalDensity
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
import kotlinx.coroutines.delay
import kotlin.math.abs

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
    bookmarks: List<com.byteflipper.everbook.domain.reader.Bookmark>,
    highlightsByParagraph: Map<Int, List<com.byteflipper.everbook.domain.reader.Bookmark>>,
    editingAnnotation: com.byteflipper.everbook.domain.reader.Bookmark?,
    pendingAnnotationText: String?,
    pendingAnnotationColorArgb: Int?,
    highlightPaletteText: String?,
    highlightPaletteAnnotation: com.byteflipper.everbook.domain.reader.Bookmark?,
    highlightPaletteAnchorX: Int,
    highlightPaletteAnchorY: Int,
    highlightColors: List<Int>,
    showHighlightPaletteEditor: Boolean,
    focusedBookmarkId: Int?,
    pendingBookmarkNavigation: com.byteflipper.everbook.domain.reader.Bookmark?,
    pendingBookmarkDisplayIndex: Int?,
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
    showBookmarksDrawer: (ReaderEvent.OnShowBookmarksDrawer) -> Unit,
    scrollToBookmark: (ReaderEvent.OnScrollToBookmark) -> Unit,
    bookmarkScrollFinished: (ReaderEvent.OnBookmarkScrollFinished) -> Unit,
    deleteBookmark: (ReaderEvent.OnDeleteBookmark) -> Unit,
    createBookmark: (ReaderEvent.OnCreateBookmark) -> Unit,
    showHighlightPalette: (ReaderEvent.OnShowHighlightPalette) -> Unit,
    createHighlight: (ReaderEvent.OnCreateHighlight) -> Unit,
    applyHighlightPaletteColor: (ReaderEvent.OnApplyHighlightPaletteColor) -> Unit,
    requestAnnotationEditor: (ReaderEvent.OnRequestAnnotationEditor) -> Unit,
    changeHighlightColor: (ReaderEvent.OnChangeHighlightColor) -> Unit,
    clearHighlightColor: (ReaderEvent.OnClearHighlightColor) -> Unit,
    editAnnotation: (ReaderEvent.OnEditAnnotation) -> Unit,
    saveAnnotation: (ReaderEvent.OnSaveAnnotation) -> Unit,
    dismissAnnotationEditor: (ReaderEvent.OnDismissAnnotationEditor) -> Unit,
    dismissHighlightPalette: (ReaderEvent.OnDismissHighlightPalette) -> Unit,
    showPaletteEditor: (ReaderEvent.OnShowHighlightPaletteEditor) -> Unit,
    dismissPaletteEditor: (ReaderEvent.OnDismissHighlightPaletteEditor) -> Unit,
    updateHighlightPalette: (ReaderEvent.OnUpdateHighlightPalette) -> Unit,
    dismissDrawer: (ReaderEvent.OnDismissDrawer) -> Unit,
    changePdfReadingMode: (ReaderEvent.OnChangePdfReadingMode) -> Unit,
    changePdfDefaultReadingMode: (PdfReadingMode) -> Unit,
    navigateToBookInfo: (changePath: Boolean) -> Unit,
    navigateBack: () -> Unit
) {
    val annotationTopOffsets = remember { mutableStateMapOf<Int, Float>() }
    val paragraphSpacingPx = with(LocalDensity.current) { paragraphHeight.toPx() }

    LaunchedEffect(pendingBookmarkNavigation?.id, pendingBookmarkDisplayIndex) {
        val bookmark = pendingBookmarkNavigation ?: return@LaunchedEffect
        val displayIndex = pendingBookmarkDisplayIndex ?: return@LaunchedEffect
        val currentIndex = listState.firstVisibleItemIndex
        val distance = abs(displayIndex - currentIndex)
        if (distance > 12) {
            val direction = if (displayIndex > currentIndex) 1 else -1
            val approachIndex = (displayIndex - direction * 8).coerceAtLeast(0)
            listState.animateScrollToItem(approachIndex)
        }
        dismissDrawer(ReaderEvent.OnDismissDrawer)
        delay(220)
        listState.animateScrollToItem(displayIndex)

        // Wait for the target paragraph's text layout so a long paragraph can be positioned by
        // the annotation range, not merely by the paragraph's first line.
        delay(32)
        var item = listState.layoutInfo.visibleItemsInfo
            .firstOrNull { it.index == displayIndex }
        if (item == null) {
            repeat(3) {
                delay(16)
                item = listState.layoutInfo.visibleItemsInfo
                    .firstOrNull { it.index == displayIndex }
            }
        }
        item?.let { targetItem ->
            val viewportCenter = (
                listState.layoutInfo.viewportStartOffset +
                        listState.layoutInfo.viewportEndOffset
                ) / 2f
            val annotationTop = annotationTopOffsets[bookmark.id]
            val targetOffset = annotationTop?.let { top ->
                targetItem.offset + if (displayIndex == 0) 0f else paragraphSpacingPx + top
            } ?: (targetItem.offset + targetItem.size / 2f)
            listState.animateScrollBy(
                value = targetOffset - viewportCenter,
                animationSpec = tween(durationMillis = 420, easing = FastOutSlowInEasing)
            )
        }
        bookmarkScrollFinished(ReaderEvent.OnBookmarkScrollFinished(bookmark))
    }

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
            highlightsByParagraph = highlightsByParagraph,
            focusedBookmarkId = focusedBookmarkId,
            onAnnotationTextLayout = { bookmarkId, topOffsetPx ->
                annotationTopOffsets[bookmarkId] = topOffsetPx
            },
            editAnnotation = editAnnotation,
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
            createBookmark = createBookmark,
            createHighlight = createHighlight,
            showHighlightPalette = showHighlightPalette,
            highlightColors = highlightColors,
            showPaletteEditor = showPaletteEditor,
            requestAnnotationEditor = requestAnnotationEditor,
            showPdfReadingModeBottomSheet = showPdfReadingModeBottomSheet,
            showBookTranslationBottomSheet = showBookTranslationBottomSheet,
            showSettingsBottomSheet = showSettingsBottomSheet,
            showChaptersDrawer = showChaptersDrawer,
            showBookmarksDrawer = showBookmarksDrawer,
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
        bookmarks = bookmarks,
        highlightColors = highlightColors,
        scrollToChapter = scrollToChapter,
        scrollToBookmark = scrollToBookmark,
        deleteBookmark = deleteBookmark,
        changeHighlightColor = changeHighlightColor,
        editAnnotation = editAnnotation,
        dismissDrawer = dismissDrawer
    )

    ReaderAnnotationDialog(
        selectedText = pendingAnnotationText,
        editingAnnotation = editingAnnotation,
        initialColorArgb = pendingAnnotationColorArgb,
        colors = highlightColors,
        saveAnnotation = saveAnnotation,
        managePalette = {
            dismissAnnotationEditor(ReaderEvent.OnDismissAnnotationEditor)
            showPaletteEditor(ReaderEvent.OnShowHighlightPaletteEditor())
        },
        dismiss = dismissAnnotationEditor
    )

    ReaderHighlightPalette(
        selectedText = highlightPaletteText,
        annotation = highlightPaletteAnnotation,
        anchorX = highlightPaletteAnchorX,
        anchorY = highlightPaletteAnchorY,
        colors = highlightColors,
        createHighlight = createHighlight,
        createBookmark = createBookmark,
        changeHighlightColor = changeHighlightColor,
        clearHighlightColor = clearHighlightColor,
        openNoteEditor = requestAnnotationEditor,
        editAnnotation = editAnnotation,
        deleteBookmark = deleteBookmark,
        managePalette = showPaletteEditor,
        dismiss = dismissHighlightPalette
    )

    ReaderHighlightPaletteEditor(
        visible = showHighlightPaletteEditor,
        colors = highlightColors,
        updateColors = { updateHighlightPalette(ReaderEvent.OnUpdateHighlightPalette(it)) },
        dismiss = { colorArgb ->
            applyHighlightPaletteColor(ReaderEvent.OnApplyHighlightPaletteColor(colorArgb))
            dismissPaletteEditor(ReaderEvent.OnDismissHighlightPaletteEditor)
        }
    )

    ReaderBackHandler(
        leave = leave,
        navigateBack = navigateBack
    )
}
