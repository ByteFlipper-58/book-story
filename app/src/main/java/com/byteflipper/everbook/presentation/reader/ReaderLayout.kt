/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.reader

import android.os.Build
import android.view.View
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import com.byteflipper.everbook.R
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.runtime.withFrameMillis
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.MutatePriority
import kotlin.coroutines.cancellation.CancellationException
import com.byteflipper.everbook.domain.reader.Bookmark
import com.byteflipper.everbook.domain.reader.FontWithName
import com.byteflipper.everbook.domain.reader.ReaderFontThickness
import com.byteflipper.everbook.domain.reader.ReaderHorizontalGesture
import com.byteflipper.everbook.domain.reader.ReaderText
import com.byteflipper.everbook.domain.reader.ReaderTextAlignment
import com.byteflipper.everbook.domain.util.HorizontalAlignment
import com.byteflipper.everbook.presentation.core.components.common.AnimatedVisibility
import com.byteflipper.everbook.presentation.core.components.common.LazyColumnWithScrollbar
import com.byteflipper.everbook.presentation.core.components.common.ReaderInlineContent
import com.byteflipper.everbook.presentation.core.components.common.SelectionContainer
import com.byteflipper.everbook.presentation.core.components.common.SpacedItem
import com.byteflipper.everbook.presentation.core.util.LocalActivity
import com.byteflipper.everbook.presentation.core.util.noRippleClickable
import com.byteflipper.everbook.presentation.core.util.showToast
import com.byteflipper.everbook.ui.reader.ReaderEvent
import com.byteflipper.everbook.ui.reader.ReaderTranslationState
import kotlinx.coroutines.delay

private const val TRANSLATION_DISMISS_ANIMATION_MS = 260

@Composable
fun ReaderLayout(
    isAutoScrolling: Boolean,
    autoScrollSpeed: Float,
    isAutoScrollPaused: Boolean,
    onSetAutoScrolling: (ReaderEvent.OnSetAutoScrolling) -> Unit,
    displayContent: ReaderDisplayContent,
    ttsTextIndex: Int,
    ttsSentenceRange: IntRange?,
    ttsFollowsText: Boolean,
    highlightsByParagraph: Map<Int, List<Bookmark>>,
    focusedBookmarkId: Int?,
    onAnnotationTextLayout: (bookmarkId: Int, topOffsetPx: Float) -> Unit,
    editAnnotation: (ReaderEvent.OnEditAnnotation) -> Unit,
    listState: LazyListState,
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
    translation: ReaderTranslationState,
    paragraphHeight: Dp,
    sidePadding: Dp,
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
    isLoading: Boolean,
    showMenu: Boolean,
    createInlineContentView: (Long) -> View?,
    menuVisibility: (ReaderEvent.OnMenuVisibility) -> Unit,
    openShareApp: (ReaderEvent.OnOpenShareApp) -> Unit,
    openWebBrowser: (ReaderEvent.OnOpenWebBrowser) -> Unit,
    openTranslator: (ReaderEvent.OnOpenTranslator) -> Unit,
    translateText: (ReaderEvent.OnTranslateText) -> Unit,
    openExternalTranslator: (ReaderEvent.OnOpenExternalTranslator) -> Unit,
    dismissTranslation: (ReaderEvent.OnDismissTranslation) -> Unit,
    toggleTranslationOriginal: (ReaderEvent.OnToggleTranslationOriginal) -> Unit,
    openDictionary: (ReaderEvent.OnOpenDictionary) -> Unit,
    annotationActionsEnabled: Boolean,
    createBookmark: (ReaderEvent.OnCreateBookmark) -> Unit,
    createHighlight: (ReaderEvent.OnCreateHighlight) -> Unit,
    showHighlightPalette: (ReaderEvent.OnShowHighlightPalette) -> Unit,
    highlightColors: List<Int>,
    showPaletteEditor: (ReaderEvent.OnShowHighlightPaletteEditor) -> Unit,
    requestAnnotationEditor: (ReaderEvent.OnRequestAnnotationEditor) -> Unit
) {
    val activity = LocalActivity.current
    var closingTranslationReaderIndex by remember {
        mutableStateOf<Int?>(null)
    }

    var isUserTouching by remember { mutableStateOf(false) }
    val density = LocalDensity.current

    val localMenuVisibility = remember(isAutoScrolling) {
        { event: ReaderEvent.OnMenuVisibility ->
            if (isAutoScrolling) {
                onSetAutoScrolling(ReaderEvent.OnSetAutoScrolling(false))
                menuVisibility(
                    ReaderEvent.OnMenuVisibility(
                        show = true,
                        fullscreenMode = event.fullscreenMode,
                        saveCheckpoint = event.saveCheckpoint,
                        activity = event.activity
                    )
                )
            } else {
                menuVisibility(event)
            }
        }
    }

    LaunchedEffect(isAutoScrolling) {
        if (!isAutoScrolling) {
            isUserTouching = false
        }
    }

    LaunchedEffect(isAutoScrolling, autoScrollSpeed, isAutoScrollPaused) {
        if (isAutoScrolling) {
            val targetSpeedDpSec = 10f + autoScrollSpeed * 15f
            var lastFrameTime = 0L
            var isResuming = false
            var resumeTime = 0L
            while (listState.canScrollForward) {
                if (isAutoScrollPaused) {
                    lastFrameTime = 0L
                    delay(100)
                    continue
                }

                if (isUserTouching || listState.isScrollInProgress) {
                    isResuming = true
                    lastFrameTime = 0L
                    delay(100)
                    continue
                }

                if (isResuming) {
                    delay(1500)
                    isResuming = false
                    lastFrameTime = withFrameMillis { it }
                    resumeTime = lastFrameTime
                    continue
                }

                if (lastFrameTime == 0L) {
                    lastFrameTime = withFrameMillis { it }
                    resumeTime = lastFrameTime
                }

                val currentFrameTime = withFrameMillis { it }
                val deltaMs = currentFrameTime - lastFrameTime
                val elapsedSeconds = deltaMs / 1000f
                lastFrameTime = currentFrameTime

                val timeSinceResume = currentFrameTime - resumeTime
                val rampFactor = (timeSinceResume / 1000f).coerceIn(0f, 1f)

                val speedPixels = density.run { targetSpeedDpSec.dp.toPx() }
                val scrollAmount = speedPixels * elapsedSeconds * rampFactor

                if (scrollAmount > 0f) {
                    try {
                        listState.scroll(MutatePriority.Default) {
                            scrollBy(scrollAmount)
                        }
                    } catch (e: CancellationException) {
                        isResuming = true
                        lastFrameTime = 0L
                        delay(100)
                    }
                }
            }

            onSetAutoScrolling(ReaderEvent.OnSetAutoScrolling(false))
        }
    }

    LaunchedEffect(ttsTextIndex, ttsFollowsText) {
        if (!ttsFollowsText || ttsTextIndex < 0 || isUserTouching) return@LaunchedEffect

        val displayIndex = displayContent.textIndexToDisplayIndex(ttsTextIndex)
        val layoutInfo = listState.layoutInfo
        val item = layoutInfo.visibleItemsInfo.firstOrNull { it.index == displayIndex }
        // Only chase the spoken paragraph once it leaves the viewport, so reading ahead by hand
        // is not fought by the follow-along scrolling.
        val isFullyVisible = item != null &&
                item.offset >= layoutInfo.viewportStartOffset &&
                item.offset + item.size <= layoutInfo.viewportEndOffset
        if (!isFullyVisible) {
            listState.animateScrollToItem(displayIndex)
        }
    }

    LaunchedEffect(translation.readerTextIndex, translation.text) {
        closingTranslationReaderIndex = null
    }

    LaunchedEffect(closingTranslationReaderIndex) {
        if (closingTranslationReaderIndex != null) {
            delay(TRANSLATION_DISMISS_ANIMATION_MS.toLong())
            dismissTranslation(ReaderEvent.OnDismissTranslation)
        }
    }

    val closeInlineTranslation = {
        if (
            translation.readerTextIndex != null &&
            closingTranslationReaderIndex != translation.readerTextIndex
        ) {
            closingTranslationReaderIndex = translation.readerTextIndex
        }
    }

    SelectionContainer(
        onCopyRequested = {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
                activity.getString(R.string.copied)
                    .showToast(context = activity, longToast = false)
            }
        },
        onShareRequested = { textToShare ->
            openShareApp(
                ReaderEvent.OnOpenShareApp(
                    textToShare = textToShare,
                    activity = activity
                )
            )
        },
        onWebSearchRequested = { textToSearch ->
            openWebBrowser(
                ReaderEvent.OnOpenWebBrowser(
                    textToSearch = textToSearch,
                    activity = activity
                )
            )
        },
        onTranslateRequested = { textToTranslate ->
            translateText(
                ReaderEvent.OnTranslateText(
                    textToTranslate = textToTranslate,
                    sourceLanguageCode = translationSourceLanguage,
                    targetLanguageCode = translationTargetLanguage,
                    providerMode = translationProviderMode,
                    requireWifi = translationWifiOnly,
                    activity = activity,
                    translateWholeParagraph = false,
                    readerTextIndex = null
                )
            )
        },
        onDictionaryRequested = { textToDefine ->
            openDictionary(
                ReaderEvent.OnOpenDictionary(
                    textToDefine,
                    activity = activity
                )
            )
        },
        onBookmarkRequested = if (annotationActionsEnabled) {
            { selectedText ->
                createBookmark(ReaderEvent.OnCreateBookmark(selectedText))
            }
        } else null,
        onHighlightRequested = if (annotationActionsEnabled) {
            { selectedText, rect ->
                showHighlightPalette(
                    ReaderEvent.OnShowHighlightPalette(
                        selectedText = selectedText,
                        anchorX = rect.center.x.toInt(),
                        anchorY = rect.top.toInt()
                    )
                )
            }
        } else null,
        onNoteRequested = if (annotationActionsEnabled) {
            { selectedText ->
                requestAnnotationEditor(ReaderEvent.OnRequestAnnotationEditor(selectedText))
            }
        } else null
    ) { toolbarHidden ->
        Column(
            Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .then(
                    if (!isLoading && toolbarHidden) {
                        Modifier.noRippleClickable(
                            onClick = {
                                localMenuVisibility(
                                    ReaderEvent.OnMenuVisibility(
                                        show = !showMenu,
                                        fullscreenMode = fullscreenMode,
                                        saveCheckpoint = true,
                                        activity = activity
                                    )
                                )
                            }
                        )
                    } else Modifier
                )
                .padding(contentPadding)
                .padding(vertical = verticalPadding)
                .readerHorizontalGesture(
                    listState = listState,
                    horizontalGesture = horizontalGesture,
                    horizontalGestureScroll = horizontalGestureScroll,
                    horizontalGestureSensitivity = horizontalGestureSensitivity,
                    horizontalGestureAlphaAnim = horizontalGestureAlphaAnim,
                    horizontalGesturePullAnim = horizontalGesturePullAnim,
                    isLoading = isLoading
                )
        ) {
            LazyColumnWithScrollbar(
                state = listState,
                enableScrollbar = false,
                parentModifier = Modifier.weight(1f),
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent()
                                isUserTouching = event.changes.any { it.pressed }
                            }
                        }
                    },
                contentPadding = PaddingValues(
                    top = (WindowInsets.displayCutout.asPaddingValues()
                        .calculateTopPadding() + paragraphHeight)
                        .coerceAtLeast(18.dp),
                    bottom = (WindowInsets.displayCutout.asPaddingValues()
                        .calculateBottomPadding() + paragraphHeight)
                        .coerceAtLeast(18.dp),
                )
            ) {
                itemsIndexed(
                    displayContent.rows,
                    key = { index, row ->
                        when (row) {
                            is ReaderDisplayRow.Text -> "text_${row.readerIndex}"
                            is ReaderDisplayRow.InlineContent -> "inline_${row.placement.id}"
                        }
                    }
                ) { index, row ->
                    when (row) {
                        is ReaderDisplayRow.InlineContent -> {
                            SpacedItem(
                                index = index,
                                spacing = paragraphHeight
                            ) {
                                ReaderInlineContent(
                                    modifier = Modifier.padding(
                                        start = sidePadding,
                                        end = sidePadding
                                    ),
                                    createView = { createInlineContentView(row.placement.id) }
                                )
                            }
                        }

                        is ReaderDisplayRow.Text -> {
                            if (!images && row.entry is ReaderText.Image) return@itemsIndexed

                            SpacedItem(
                                index = index,
                                spacing = paragraphHeight
                            ) {
                                ReaderLayoutText(
                                    activity = activity,
                                    showMenu = showMenu,
                                    readerIndex = row.readerIndex,
                                    highlights = highlightsByParagraph[row.readerIndex].orEmpty(),
                                    spokenSentence = ttsSentenceRange.takeIf {
                                        row.readerIndex == ttsTextIndex
                                    },
                                    focusedBookmarkId = focusedBookmarkId,
                                    onAnnotationTextLayout = onAnnotationTextLayout,
                                    showHighlightPalette = showHighlightPalette,
                                    editAnnotation = editAnnotation,
                                    entry = row.entry,
                                    imagesCornersRoundness = imagesCornersRoundness,
                                    imagesAlignment = imagesAlignment,
                                    imagesWidth = imagesWidth,
                                    imagesColorEffects = imagesColorEffects,
                                    fontFamily = fontFamily,
                                    fontColor = fontColor,
                                    lineHeight = lineHeight,
                                    fontThickness = fontThickness,
                                    fontStyle = fontStyle,
                                    chapterTitleAlignment = chapterTitleAlignment,
                                    textAlignment = textAlignment,
                                    horizontalAlignment = horizontalAlignment,
                                    fontSize = fontSize,
                                    letterSpacing = letterSpacing,
                                    sidePadding = sidePadding,
                                    paragraphIndentation = paragraphIndentation,
                                    fullscreenMode = fullscreenMode,
                                    doubleClickTranslation = doubleClickTranslation,
                                    translationProviderMode = translationProviderMode,
                                    translationSourceLanguage = translationSourceLanguage,
                                    translationTargetLanguage = translationTargetLanguage,
                                    translationWifiOnly = translationWifiOnly,
                                    translation = translation,
                                    closingTranslation = closingTranslationReaderIndex == row.readerIndex,
                                    highlightedReading = highlightedReading,
                                    highlightedReadingThickness = highlightedReadingThickness,
                                    toolbarHidden = toolbarHidden,
                                    openTranslator = openTranslator,
                                    translateText = translateText,
                                    openExternalTranslator = openExternalTranslator,
                                    closeTranslation = closeInlineTranslation,
                                    toggleTranslationOriginal = toggleTranslationOriginal,
                                    menuVisibility = localMenuVisibility
                                )
                            }
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = !showMenu && progressBar,
                enter = slideInVertically { it } + expandVertically(),
                exit = slideOutVertically { it } + shrinkVertically()
            ) {
                ReaderProgressBar(
                    progress = progress,
                    progressBarPadding = progressBarPadding,
                    progressBarAlignment = progressBarAlignment,
                    progressBarFontSize = progressBarFontSize,
                    fontColor = fontColor,
                    sidePadding = sidePadding
                )
            }
        }
    }
}
