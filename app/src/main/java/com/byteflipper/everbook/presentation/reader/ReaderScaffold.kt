/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.reader
import androidx.compose.ui.res.painterResource

import android.annotation.SuppressLint
import android.view.View
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.remember
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import kotlinx.coroutines.delay

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ReaderScaffold(
    isAutoScrolling: Boolean,
    autoScrollSpeed: Float,
    isAutoScrollPaused: Boolean,
    autoScrollChipAlignment: String,
    autoScrollChipOpacity: Int,
    autoScrollChipOpacityEnabled: Boolean,
    autoScrollChipPlayPause: Boolean,
    onSetAutoScrolling: (ReaderEvent.OnSetAutoScrolling) -> Unit,
    onSetAutoScrollPaused: (ReaderEvent.OnSetAutoScrollPaused) -> Unit,
    onChangeAutoScrollSpeed: (Float) -> Unit,
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
                visible = showMenu && !isAutoScrolling,
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
                visible = showMenu && !isAutoScrolling,
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
                        onSetAutoScrolling = onSetAutoScrolling,
                        menuVisibility = menuVisibility,
                        fullscreenMode = fullscreenMode,
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
                isAutoScrolling = isAutoScrolling,
                autoScrollSpeed = autoScrollSpeed,
                isAutoScrollPaused = isAutoScrollPaused,
                onSetAutoScrolling = onSetAutoScrolling,
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

            // FLOATING AUTO-SCROLL CONTROLS
            var isPanelCollapsed by rememberSaveable(book.id) { mutableStateOf(false) }
            var lastInteractionTime by remember { mutableStateOf(System.currentTimeMillis()) }

            LaunchedEffect(isAutoScrolling, isPanelCollapsed, lastInteractionTime) {
                if (isAutoScrolling && !isPanelCollapsed) {
                    delay(3000)
                    isPanelCollapsed = true
                }
            }

            LaunchedEffect(isAutoScrolling) {
                if (!isAutoScrolling) {
                    isPanelCollapsed = false
                }
            }

            AnimatedVisibility(
                visible = isAutoScrolling,
                enter = fadeIn() + slideInVertically { it },
                exit = fadeOut() + slideOutVertically { it },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
            ) {
                val springSpec = spring<Dp>(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMedium
                )
                val springFloatSpec = spring<Float>(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMedium
                )

                val panelHeight by animateDpAsState(
                    targetValue = if (isPanelCollapsed) 40.dp else 120.dp,
                    animationSpec = springSpec
                )
                val panelWidthFraction by animateFloatAsState(
                    targetValue = if (isPanelCollapsed) {
                        if (autoScrollChipPlayPause) 0.32f else 0.22f
                    } else 1f,
                    animationSpec = springFloatSpec
                )
                val panelAlignment = when (autoScrollChipAlignment) {
                    "BOTTOM_LEFT" -> Alignment.BottomStart
                    "BOTTOM_CENTER" -> Alignment.BottomCenter
                    else -> Alignment.BottomEnd
                }
                val panelCorner by animateDpAsState(
                    targetValue = if (isPanelCollapsed) 20.dp else 28.dp,
                    animationSpec = springSpec
                )

                val targetAlpha = if (isPanelCollapsed && autoScrollChipOpacityEnabled) {
                    autoScrollChipOpacity / 100f
                } else {
                    1f
                }
                val chipAlpha by animateFloatAsState(
                    targetValue = targetAlpha,
                    animationSpec = if (isPanelCollapsed) {
                        tween(durationMillis = 300, delayMillis = 300)
                    } else {
                        tween(durationMillis = 150)
                    }
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    contentAlignment = panelAlignment
                ) {
                    Card(
                        modifier = Modifier
                            .graphicsLayer { alpha = chipAlpha }
                            .fillMaxWidth(panelWidthFraction)
                            .height(panelHeight)
                            .noRippleClickable {
                                if (isPanelCollapsed) {
                                    isPanelCollapsed = false
                                }
                                lastInteractionTime = System.currentTimeMillis()
                            },
                        shape = RoundedCornerShape(panelCorner),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                    ) {
                        if (!isPanelCollapsed) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_speed),
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = stringResource(id = R.string.auto_scroll_reader_settings),
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                color = MaterialTheme.colorScheme.secondaryContainer,
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "${String.format(java.util.Locale.US, "%.1f", autoScrollSpeed)}x",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    }
                                }

                                BoxWithConstraints(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(28.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                        .pointerInput(Unit) {
                                            detectTapGestures { offset ->
                                                lastInteractionTime = System.currentTimeMillis()
                                                val insetPx = 14.dp.toPx()
                                                val activeWidthPx = (size.width.toFloat() - insetPx * 2).coerceAtLeast(1f)
                                                val fraction = ((offset.x - insetPx) / activeWidthPx).coerceIn(0f, 1f)
                                                val rawSpeed = 1.0f + fraction * 9.0f
                                                val roundedSpeed = (Math.round(rawSpeed * 2.0) / 2.0).toFloat()
                                                onChangeAutoScrollSpeed(roundedSpeed)
                                            }
                                        }
                                        .pointerInput(Unit) {
                                            detectHorizontalDragGestures { change, _ ->
                                                lastInteractionTime = System.currentTimeMillis()
                                                change.consume()
                                                val insetPx = 14.dp.toPx()
                                                val activeWidthPx = (size.width.toFloat() - insetPx * 2).coerceAtLeast(1f)
                                                val fraction = ((change.position.x - insetPx) / activeWidthPx).coerceIn(0f, 1f)
                                                val rawSpeed = 1.0f + fraction * 9.0f
                                                val roundedSpeed = (Math.round(rawSpeed * 2.0) / 2.0).toFloat()
                                                onChangeAutoScrollSpeed(roundedSpeed)
                                            }
                                        },
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    val fraction = (autoScrollSpeed - 1.0f) / 9.0f
                                    val activeWidth = when {
                                        autoScrollSpeed <= 1.0f -> 0.dp
                                        autoScrollSpeed >= 10.0f -> maxWidth
                                        else -> (14.dp + ((maxWidth - 28.dp) * fraction))
                                            .coerceAtLeast(0.dp)
                                    }

                                    if (activeWidth > 0.dp) {
                                        Box(
                                            modifier = Modifier
                                                .width(activeWidth)
                                                .fillMaxHeight()
                                                .background(MaterialTheme.colorScheme.primary)
                                        )
                                    }

                                    for (i in 0..18) {
                                        val dotFraction = i / 18f
                                        val xOffset = (14.dp + ((maxWidth - 28.dp) * dotFraction) - 1.5.dp).coerceAtLeast(0.dp)
                                        val isCovered = (i * 0.5f + 1.0f) <= autoScrollSpeed && autoScrollSpeed > 1.0f
                                        val isSelected = (i * 0.5f + 1.0f) <= autoScrollSpeed
                                        val dotColor = if (isCovered) {
                                            MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                                        } else {
                                            if (isSelected) {
                                                MaterialTheme.colorScheme.primary
                                            } else {
                                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                                            }
                                        }

                                        Box(
                                            modifier = Modifier
                                                .offset(x = xOffset, y = 0.dp)
                                                .size(3.dp)
                                                .clip(androidx.compose.foundation.shape.CircleShape)
                                                .background(dotColor)
                                                .align(Alignment.CenterStart)
                                        )
                                    }
                                }

                                Text(
                                    text = stringResource(id = R.string.auto_scroll_tap_to_stop),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 9.sp,
                                        lineHeight = 11.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                if (autoScrollChipPlayPause) {
                                    IconButton(
                                        onClick = {
                                            onSetAutoScrollPaused(ReaderEvent.OnSetAutoScrollPaused(!isAutoScrollPaused))
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            painter = painterResource(
                                                id = if (isAutoScrollPaused) R.drawable.ic_play_arrow_rounded_24px
                                                     else R.drawable.ic_pause_rounded_24px
                                            ),
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                }

                                Icon(
                                    painter = painterResource(id = R.drawable.ic_speed),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${String.format(java.util.Locale.US, "%.1f", autoScrollSpeed)}x",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
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
                    painter = if (rateLimitedTranslation != null) {
                        painterResource(R.drawable.ic_warning_rounded_24px)
                    } else {
                        painterResource(R.drawable.ic_translate_rounded_24px)
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
                    painter = painterResource(R.drawable.ic_close_rounded_24px),
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
