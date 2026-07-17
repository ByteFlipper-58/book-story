/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.reader

import android.annotation.SuppressLint
import android.view.View
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.byteflipper.everbook.R
import com.byteflipper.everbook.domain.library.book.Book
import com.byteflipper.everbook.domain.reader.Bookmark
import com.byteflipper.everbook.domain.reader.Checkpoint
import com.byteflipper.everbook.domain.reader.FontWithName
import com.byteflipper.everbook.domain.reader.ReaderFontThickness
import com.byteflipper.everbook.domain.reader.ReaderHorizontalGesture
import com.byteflipper.everbook.domain.reader.ReaderText
import com.byteflipper.everbook.domain.reader.ReaderText.Chapter
import com.byteflipper.everbook.domain.reader.ReaderTextAlignment
import com.byteflipper.everbook.domain.translation.TranslationProviderMode
import com.byteflipper.everbook.domain.util.HorizontalAlignment
import com.byteflipper.everbook.presentation.core.components.common.AnimatedVisibility
import com.byteflipper.everbook.presentation.core.components.common.StyledText
import com.byteflipper.everbook.presentation.core.util.noRippleClickable
import com.byteflipper.everbook.ui.reader.ReaderBookTranslationState
import com.byteflipper.everbook.ui.reader.ReaderEvent
import com.byteflipper.everbook.ui.reader.ReaderTranslationState
import com.byteflipper.everbook.ui.settings.SettingsEvent
import com.byteflipper.everbook.ui.theme.readerBarsColor

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ReaderScaffold(
    book: Book,
    text: List<ReaderText>,
    displayContent: ReaderDisplayContent,
    highlightsByParagraph: Map<Int, List<Bookmark>>,
    focusedBookmarkId: Int?,
    onAnnotationTextLayout: (bookmarkId: Int, topOffsetPx: Float) -> Unit,
    listState: LazyListState,
    currentChapter: Chapter?,
    translation: ReaderTranslationState,
    bookTranslation: ReaderBookTranslationState,
    nestedScrollConnection: NestedScrollConnection,
    fastColorPresetChange: Boolean,
    perceptionExpander: Boolean,
    perceptionExpanderPadding: Dp,
    perceptionExpanderThickness: Dp,
    currentChapterProgress: Float,
    isLoading: Boolean,
    isParsing: Boolean,
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
    customScreenBrightness: Boolean,
    screenBrightness: Float,
    changeScreenBrightness: (Float) -> Unit,
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
    createBookmark: (ReaderEvent.OnCreateBookmark) -> Unit,
    createHighlight: (ReaderEvent.OnCreateHighlight) -> Unit,
    showHighlightPalette: (ReaderEvent.OnShowHighlightPalette) -> Unit,
    highlightColors: List<Int>,
    showPaletteEditor: (ReaderEvent.OnShowHighlightPaletteEditor) -> Unit,
    requestAnnotationEditor: (ReaderEvent.OnRequestAnnotationEditor) -> Unit,
    editAnnotation: (ReaderEvent.OnEditAnnotation) -> Unit,
    showPdfReadingModeBottomSheet: (ReaderEvent.OnShowPdfReadingModeBottomSheet) -> Unit,
    showBookTranslationBottomSheet: (ReaderEvent.OnShowBookTranslationBottomSheet) -> Unit,
    showSettingsBottomSheet: (ReaderEvent.OnShowSettingsBottomSheet) -> Unit,
    showChaptersDrawer: (ReaderEvent.OnShowChaptersDrawer) -> Unit,
    showBookmarksDrawer: (ReaderEvent.OnShowBookmarksDrawer) -> Unit,
    showTranslatedBook: (ReaderEvent.OnShowTranslatedBook) -> Unit,
    showOriginalBook: (ReaderEvent.OnShowOriginalBook) -> Unit,
    changePdfReadingMode: (ReaderEvent.OnChangePdfReadingMode) -> Unit,
    navigateToBookInfo: (changePath: Boolean) -> Unit,
    navigateBack: () -> Unit
) {
    var isDetachedTranslationStatusDismissed by rememberSaveable(book.id) {
        mutableStateOf(false)
    }
    LaunchedEffect(
        bookTranslation.currentTranslation?.id,
        bookTranslation.currentTranslation?.status,
        bookTranslation.displayMode,
        bookTranslation.isStarting,
        bookTranslation.isApplyingTranslation
    ) {
        isDetachedTranslationStatusDismissed = false
    }

    val toggleBookTranslationDisplay = {
        if (bookTranslation.isTranslatedBookVisible) {
            showOriginalBook(ReaderEvent.OnShowOriginalBook)
        } else {
            showTranslatedBook(ReaderEvent.OnShowTranslatedBook)
        }
    }
    val openBookTranslationPanel = {
        showBookTranslationBottomSheet(ReaderEvent.OnShowBookTranslationBottomSheet)
    }

    Scaffold(
        Modifier
            .fillMaxSize()
            .nestedScroll(nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            AnimatedVisibility(
                visible = showMenu,
                enter = slideInVertically { -it },
                exit = slideOutVertically { -it }
            ) {
                ReaderTopBar(
                    book = book,
                    currentChapter = currentChapter,
                    fastColorPresetChange = fastColorPresetChange,
                    currentChapterProgress = currentChapterProgress,
                    isLoading = isLoading,
                    lockMenu = lockMenu,
                    leave = leave,
                    selectPreviousPreset = selectPreviousPreset,
                    selectNextPreset = selectNextPreset,
                    showPdfReadingModeBottomSheet = showPdfReadingModeBottomSheet,
                    showBookTranslationBottomSheet = showBookTranslationBottomSheet,
                    showSettingsBottomSheet = showSettingsBottomSheet,
                    showChaptersDrawer = showChaptersDrawer,
                    showBookmarksDrawer = showBookmarksDrawer,
                    navigateBack = navigateBack,
                    navigateToBookInfo = navigateToBookInfo
                )
            }
        },
        bottomBar = {
            AnimatedVisibility(
                modifier = Modifier.fillMaxWidth(),
                visible = showMenu,
                enter = slideInVertically { it },
                exit = slideOutVertically { it }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.readerBarsColor)
                        .noRippleClickable(onClick = {})
                ) {
                    if (bookTranslation.shouldShowReaderBookTranslationStatus) {
                        ReaderBookTranslationStatusBar(
                            state = bookTranslation,
                            fillBackgroundProgress = true,
                            showDismissButton = false,
                            toggleTranslationDisplay = toggleBookTranslationDisplay,
                            dismissStatusBar = {},
                            openBookTranslationPanel = openBookTranslationPanel
                        )
                    }

                    ReaderBottomBar(
                        book = book,
                        progress = progress,
                        text = text,
                        listState = listState,
                        displayContent = displayContent,
                        lockMenu = lockMenu,
                        isParsing = isParsing,
                        checkpoint = checkpoint,
                        bottomBarPadding = bottomBarPadding,
                        restoreCheckpoint = restoreCheckpoint,
                        scroll = scroll,
                        changeProgress = changeProgress
                    )
                }
            }
        }
    ) {
        Box(Modifier.fillMaxSize()) {
            ReaderLayout(
                displayContent = displayContent,
                highlightsByParagraph = highlightsByParagraph,
                focusedBookmarkId = focusedBookmarkId,
                onAnnotationTextLayout = onAnnotationTextLayout,
                editAnnotation = editAnnotation,
                listState = listState,
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
                translation = translation,
                paragraphHeight = paragraphHeight,
                sidePadding = sidePadding,
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
                isLoading = isLoading,
                showMenu = showMenu,
                createInlineContentView = createInlineContentView,
                menuVisibility = menuVisibility,
                openShareApp = openShareApp,
                openWebBrowser = openWebBrowser,
                openTranslator = openTranslator,
                translateText = translateText,
                openExternalTranslator = openExternalTranslator,
                dismissTranslation = dismissTranslation,
                toggleTranslationOriginal = toggleTranslationOriginal,
                openDictionary = openDictionary,
                annotationActionsEnabled = !bookTranslation.isTranslatedBookVisible,
                createBookmark = createBookmark,
                createHighlight = createHighlight,
                showHighlightPalette = showHighlightPalette,
                highlightColors = highlightColors,
                showPaletteEditor = showPaletteEditor,
                requestAnnotationEditor = requestAnnotationEditor
            )

            ReaderPerceptionExpander(
                perceptionExpander = perceptionExpander,
                perceptionExpanderPadding = perceptionExpanderPadding,
                perceptionExpanderThickness = perceptionExpanderThickness,
                perceptionExpanderColor = fontColor
            )

            ReaderBrightnessGesture(
                enabled = customScreenBrightness && !isLoading && !showMenu,
                brightness = screenBrightness,
                onBrightnessChange = changeScreenBrightness
            )

            if (isLoading && text.isEmpty()) {
                ReaderLoadingPlaceholder()
            }

            AnimatedVisibility(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
                visible = bookTranslation.shouldShowReaderBookTranslationStatus &&
                        !showMenu &&
                        !isDetachedTranslationStatusDismissed,
                enter = slideInVertically { it },
                exit = slideOutVertically { it }
            ) {
                ReaderBookTranslationStatusBar(
                    state = bookTranslation,
                    applyNavigationPadding = true,
                    fillBackgroundProgress = false,
                    showDismissButton = true,
                    toggleTranslationDisplay = toggleBookTranslationDisplay,
                    dismissStatusBar = {
                        isDetachedTranslationStatusDismissed = true
                    },
                    openBookTranslationPanel = openBookTranslationPanel
                )
            }
        }
    }
}

@Composable
private fun ReaderBookTranslationStatusBar(
    state: ReaderBookTranslationState,
    modifier: Modifier = Modifier,
    applyNavigationPadding: Boolean = false,
    fillBackgroundProgress: Boolean,
    showDismissButton: Boolean,
    toggleTranslationDisplay: () -> Unit,
    dismissStatusBar: () -> Unit,
    openBookTranslationPanel: () -> Unit
) {
    val runningTranslation = state.runningTranslation
    val pausedTranslation = state.pausedTranslation
    val completedTranslation = state.completedTranslation
    val rateLimitedTranslation = state.rateLimitedTranslation
    val progressTranslation = runningTranslation ?: state.currentTranslation?.takeIf {
        state.isApplyingTranslation || it.canResume || it.canRead || it.isRateLimited
    }
    val statusText = when {
        state.isApplyingTranslation -> stringResource(id = R.string.book_translation_applying)
        state.isDownloadingModel &&
                state.currentTranslation?.providerMode == TranslationProviderMode.IN_APP ->
            stringResource(id = R.string.translation_downloading_model)
        runningTranslation != null -> stringResource(
            id = R.string.book_translation_progress,
            runningTranslation.completedUnits,
            runningTranslation.totalUnits
        )

        pausedTranslation != null -> stringResource(
            id = R.string.book_translation_paused_progress,
            pausedTranslation.completedUnits,
            pausedTranslation.totalUnits
        )

        state.isTranslatedBookVisible -> stringResource(id = R.string.book_translation_show_original)
        rateLimitedTranslation != null && rateLimitedTranslation.completedUnits > 0 -> stringResource(
            id = R.string.book_translation_google_rate_limited_progress,
            rateLimitedTranslation.completedUnits,
            rateLimitedTranslation.totalUnits
        )

        rateLimitedTranslation != null -> stringResource(id = R.string.book_translation_google_rate_limited)
        completedTranslation != null -> stringResource(id = R.string.book_translation_show_translation)
        state.isStarting -> stringResource(id = R.string.book_translation_notification_title)
        else -> stringResource(id = R.string.book_translation_indicator)
    }
    val canToggleTranslation = state.readableTranslation != null && !state.isApplyingTranslation
    val showSpinner = !canToggleTranslation &&
            (state.isStarting || state.isApplyingTranslation || runningTranslation != null)
    val progress = progressTranslation?.takeIf { !canToggleTranslation }?.progress?.coerceIn(0f, 1f)

    val clickHandler = if (canToggleTranslation) toggleTranslationDisplay else ({})
    val barModifier = modifier
        .fillMaxWidth()
        .background(MaterialTheme.colorScheme.readerBarsColor)
        .noRippleClickable(
            onLongClick = openBookTranslationPanel,
            onClick = clickHandler
        )
        .let { baseModifier ->
            if (applyNavigationPadding) {
                baseModifier.navigationBarsPadding()
            } else {
                baseModifier
            }
        }

    Box(
        modifier = barModifier
            .height(44.dp),
        contentAlignment = Alignment.Center
    ) {
        if (progress != null) {
            Box(
                modifier = Modifier
                    .align(if (fillBackgroundProgress) Alignment.CenterStart else Alignment.BottomStart)
                    .fillMaxWidth(progress)
                    .height(if (fillBackgroundProgress) 44.dp else 2.dp)
                    .background(
                        if (fillBackgroundProgress) {
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.16f)
                        } else {
                            MaterialTheme.colorScheme.secondary
                        }
                    )
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = if (showDismissButton) 52.dp else 36.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (showSpinner) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.secondary
                )
            } else {
                Icon(
                    imageVector = if (rateLimitedTranslation != null) {
                        Icons.Default.WarningAmber
                    } else {
                        Icons.Default.Translate
                    },
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.secondary
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            StyledText(
                text = statusText,
                modifier = Modifier
                    .weight(1f, fill = false)
                    .basicMarquee(iterations = Int.MAX_VALUE),
                style = MaterialTheme.typography.labelLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                maxLines = 1
            )
        }

        if (showDismissButton) {
            IconButton(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(44.dp),
                onClick = dismissStatusBar
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(id = R.string.close),
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

private val ReaderBookTranslationState.shouldShowReaderBookTranslationStatus: Boolean
    get() = isTranslatedBookVisible ||
            completedTranslation != null ||
            rateLimitedTranslation != null ||
            isStarting ||
            isApplyingTranslation ||
            runningTranslation != null ||
            pausedTranslation != null
