/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.ui.reader

import android.content.pm.ActivityInfo
import android.os.Parcelable
import android.view.WindowManager
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.systemBarsIgnoringVisibility
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.parcelize.Parcelize
import com.byteflipper.everbook.domain.distribution.ReaderInlineContentMode
import com.byteflipper.everbook.domain.navigator.Screen
import com.byteflipper.everbook.domain.reader.PdfPageDisplayMode
import com.byteflipper.everbook.domain.reader.PdfReadingMode
import com.byteflipper.everbook.domain.reader.ReaderColorEffects
import com.byteflipper.everbook.domain.reader.ReaderProgressCount
import com.byteflipper.everbook.domain.reader.ReaderTextAlignment
import com.byteflipper.everbook.presentation.core.constants.provideFonts
import com.byteflipper.everbook.presentation.core.util.LocalActivity
import com.byteflipper.everbook.presentation.core.util.calculateProgress
import com.byteflipper.everbook.presentation.core.util.setBrightness
import com.byteflipper.everbook.presentation.navigator.LocalNavigator
import com.byteflipper.everbook.presentation.pdf_reader.PdfReaderContent
import com.byteflipper.everbook.presentation.reader.buildReaderDisplayContent
import com.byteflipper.everbook.presentation.reader.ReaderContent
import com.byteflipper.everbook.ui.main.MainEvent
import com.byteflipper.everbook.ui.book_info.BookInfoScreen
import com.byteflipper.everbook.ui.main.MainModel
import com.byteflipper.everbook.ui.pdf_reader.PdfReaderEvent
import com.byteflipper.everbook.ui.pdf_reader.PdfReaderModel
import com.byteflipper.everbook.ui.settings.SettingsModel
import kotlin.math.roundToInt

@Parcelize
data class ReaderScreen(val bookId: Int) : Screen, Parcelable {

    companion object {
        const val CHAPTERS_DRAWER = "chapters_drawer"
        const val BOOKMARKS_DRAWER = "bookmarks_drawer"
        const val SETTINGS_BOTTOM_SHEET = "settings_bottom_sheet"
        const val PDF_READING_MODE_BOTTOM_SHEET = "pdf_reading_mode_bottom_sheet"
        const val TRANSLATION_BOTTOM_SHEET = "translation_bottom_sheet"
        const val BOOK_TRANSLATION_BOTTOM_SHEET = "book_translation_bottom_sheet"
    }

    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    override fun Content() {
        val context = LocalContext.current
        val navigator = LocalNavigator.current
        val screenModel = hiltViewModel<ReaderModel>()
        val pdfScreenModel = hiltViewModel<PdfReaderModel>()
        val readerInlineContentModel = hiltViewModel<ReaderInlineContentModel>()
        val mainModel = hiltViewModel<MainModel>()
        val settingsModel = hiltViewModel<SettingsModel>()

        val state = screenModel.state.collectAsStateWithLifecycle()
        val pdfState = pdfScreenModel.state.collectAsStateWithLifecycle()
        val inlineContentState = readerInlineContentModel.state.collectAsStateWithLifecycle()
        val mainState = mainModel.state.collectAsStateWithLifecycle()
        val settingsState = settingsModel.state.collectAsStateWithLifecycle()

        LaunchedEffect(mainState.value.translationWifiOnly) {
            screenModel.onEvent(
                ReaderEvent.OnChangeBookTranslationWifiOnly(
                    mainState.value.translationWifiOnly
                )
            )
        }

        val activity = LocalActivity.current
        val density = LocalDensity.current
        val listState = rememberSaveable(
            state.value.listState,
            saver = LazyListState.Saver
        ) {
            state.value.listState
        }
        val pdfListState = rememberSaveable(
            pdfState.value.listState,
            saver = LazyListState.Saver
        ) {
            pdfState.value.listState
        }
        val activePdfMode = remember(
            bookId,
            state.value.book.id,
            state.value.book.pdfReadingMode,
            pdfState.value.book.id,
            pdfState.value.book.pdfReadingMode
        ) {
            when {
                // ReaderModel owns pdfReadingMode (single source of truth). pdfState is only a
                // fallback for the brief window before ReaderModel.init() loads this book.
                state.value.book.id == bookId ->
                    state.value.book.pdfReadingMode == PdfReadingMode.ORIGINAL_PDF

                pdfState.value.book.id == bookId ->
                    pdfState.value.book.pdfReadingMode == PdfReadingMode.ORIGINAL_PDF

                else -> false
            }
        }
        val readerAvailableForInlineContent = remember(
            activePdfMode,
            state.value.showMenu,
            state.value.bottomSheet,
            state.value.drawer,
            state.value.isLoading,
            state.value.isParsing,
            pdfState.value.showMenu,
            pdfState.value.bottomSheet,
            pdfState.value.isLoading,
            pdfState.value.errorMessage
        ) {
            if (activePdfMode) {
                !pdfState.value.showMenu &&
                        pdfState.value.bottomSheet == null &&
                        !pdfState.value.isLoading &&
                        pdfState.value.errorMessage == null
            } else {
                !state.value.showMenu &&
                        state.value.bottomSheet == null &&
                        state.value.drawer == null &&
                        !state.value.isLoading &&
                        !state.value.isParsing
            }
        }
        val nestedScrollConnection = remember {
            derivedStateOf {
                object : NestedScrollConnection {
                    override fun onPostScroll(
                        consumed: Offset,
                        available: Offset,
                        source: NestedScrollSource
                    ): Offset {
                        if (source != NestedScrollSource.UserInput) {
                            return super.onPostScroll(consumed, available, source)
                        }

                        consumed.y.let { velocity ->
                            if (velocity in -70f..70f) return@let
                            if (!state.value.showMenu) return@let
                            if (state.value.lockMenu) return@let
                            if (!mainState.value.hideBarsOnFastScroll) return@let

                            screenModel.onEvent(
                                ReaderEvent.OnMenuVisibility(
                                    show = false,
                                    fullscreenMode = mainState.value.fullscreen,
                                    saveCheckpoint = false,
                                    activity = activity
                                )
                            )
                        }
                        return super.onPostScroll(consumed, available, source)
                    }
                }
            }
        }

        val fontFamily = remember(mainState.value.fontFamily) {
            provideFonts(context).run {
                find {
                    it.id == mainState.value.fontFamily
                } ?: get(0)
            }
        }
        val backgroundColor = animateColorAsState(
            targetValue = settingsState.value.selectedColorPreset.backgroundColor
        )
        val fontColor = animateColorAsState(
            targetValue = settingsState.value.selectedColorPreset.fontColor
        )
        val lineHeight = remember(
            mainState.value.fontSize,
            mainState.value.lineHeight
        ) {
            (mainState.value.fontSize + mainState.value.lineHeight).sp
        }
        val letterSpacing = remember(mainState.value.letterSpacing) {
            (mainState.value.letterSpacing / 100f).em
        }
        val sidePadding = remember(mainState.value.sidePadding) {
            (mainState.value.sidePadding * 3).dp
        }
        val verticalPadding = remember(mainState.value.verticalPadding) {
            (mainState.value.verticalPadding * 4.5f).dp
        }
        val paragraphHeight = remember(
            mainState.value.paragraphHeight,
            mainState.value.lineHeight
        ) {
            ((mainState.value.paragraphHeight * 3).dp).coerceAtLeast(
                with(density) { mainState.value.lineHeight.sp.toDp().value * 0.5f }.dp
            )
        }
        val fontStyle = remember(mainState.value.isItalic) {
            when (mainState.value.isItalic) {
                true -> FontStyle.Italic
                false -> FontStyle.Normal
            }
        }
        val paragraphIndentation = remember(
            mainState.value.paragraphIndentation,
            mainState.value.textAlignment
        ) {
            if (
                mainState.value.textAlignment == ReaderTextAlignment.CENTER ||
                mainState.value.textAlignment == ReaderTextAlignment.END
            ) {
                return@remember 0.sp
            }
            (mainState.value.paragraphIndentation * 6).sp
        }
        val perceptionExpanderPadding = remember(
            sidePadding,
            mainState.value.perceptionExpanderPadding
        ) {
            sidePadding + (mainState.value.perceptionExpanderPadding * 8).dp
        }
        val perceptionExpanderThickness = remember(
            mainState.value.perceptionExpanderThickness
        ) {
            (mainState.value.perceptionExpanderThickness * 0.25f).dp
        }
        val horizontalGestureSensitivity = remember(mainState.value.horizontalGestureSensitivity) {
            (36f + mainState.value.horizontalGestureSensitivity * (4f - 36f)).dp
        }
        val highlightedReadingThickness = remember(mainState.value.highlightedReadingThickness) {
            when (mainState.value.highlightedReadingThickness) {
                2 -> FontWeight.SemiBold
                3 -> FontWeight.Bold
                else -> FontWeight.Medium
            }
        }
        val horizontalAlignment = remember(mainState.value.textAlignment) {
            when (mainState.value.textAlignment) {
                ReaderTextAlignment.START, ReaderTextAlignment.JUSTIFY -> Alignment.Start
                ReaderTextAlignment.CENTER -> Alignment.CenterHorizontally
                ReaderTextAlignment.END -> Alignment.End
            }
        }
        val imagesWidth = remember(mainState.value.imagesWidth) {
            mainState.value.imagesWidth.coerceAtLeast(0.01f)
        }
        val imagesCornersRoundness = remember(
            mainState.value.imagesCornersRoundness,
            mainState.value.imagesWidth
        ) {
            (mainState.value.imagesCornersRoundness * 3 * imagesWidth).dp
        }
        val imagesColorEffects = remember(
            mainState.value.imagesColorEffects,
            fontColor.value,
            backgroundColor.value
        ) {
            when (mainState.value.imagesColorEffects) {
                ReaderColorEffects.OFF -> null

                ReaderColorEffects.GRAYSCALE -> ColorFilter.colorMatrix(
                    ColorMatrix().apply { setToSaturation(0f) }
                )

                ReaderColorEffects.FONT -> ColorFilter.tint(
                    color = fontColor.value,
                    blendMode = BlendMode.Color
                )

                ReaderColorEffects.BACKGROUND -> ColorFilter.tint(
                    color = backgroundColor.value,
                    blendMode = BlendMode.Color
                )
            }
        }
        val progressBarPadding = remember(mainState.value.progressBarPadding) {
            (mainState.value.progressBarPadding * 3).dp
        }
        val progressBarFontSize = remember(mainState.value.progressBarFontSize) {
            (mainState.value.progressBarFontSize * 2).sp
        }
        val readerDisplayContent = remember(
            state.value.text,
            inlineContentState.value.placements
        ) {
            buildReaderDisplayContent(
                text = state.value.text,
                inlineContentPlacements = inlineContentState.value.placements
            )
        }
        // Every annotation participates in reader markers and post-navigation focus.
        val highlightsByParagraph = remember(state.value.bookmarks) {
            state.value.bookmarks
                .groupBy { it.paragraphIndex }
        }

        val layoutDirection = LocalLayoutDirection.current
        val cutoutInsets = WindowInsets.displayCutout
        val systemBarsInsets = WindowInsets.systemBarsIgnoringVisibility

        val cutoutInsetsPadding = remember(mainState.value.cutoutPadding) {
            derivedStateOf {
                cutoutInsets.asPaddingValues(density = density).run {
                    if (mainState.value.cutoutPadding) PaddingValues(
                        top = calculateTopPadding(),
                        start = calculateStartPadding(layoutDirection),
                        end = calculateEndPadding(layoutDirection),
                        bottom = calculateBottomPadding()
                    ) else PaddingValues(0.dp)
                }
            }
        }
        val systemBarsInsetsPadding = remember(mainState.value.fullscreen) {
            derivedStateOf {
                systemBarsInsets.asPaddingValues(density = density).run {
                    if (!mainState.value.fullscreen) PaddingValues(
                        top = calculateTopPadding(),
                        start = calculateStartPadding(layoutDirection),
                        end = calculateEndPadding(layoutDirection),
                        bottom = calculateBottomPadding()
                    ) else PaddingValues(0.dp)
                }
            }
        }
        val contentPadding = remember(
            cutoutInsetsPadding.value,
            systemBarsInsetsPadding.value
        ) {
            PaddingValues(
                top = systemBarsInsetsPadding.value.calculateTopPadding().run {
                    if (equals(0.dp)) return@run cutoutInsetsPadding.value
                        .calculateTopPadding()
                    this
                },
                start = systemBarsInsetsPadding.value.calculateStartPadding(layoutDirection).run {
                    if (equals(0.dp)) return@run cutoutInsetsPadding.value
                        .calculateStartPadding(layoutDirection)
                    this
                },
                end = systemBarsInsetsPadding.value.calculateEndPadding(layoutDirection).run {
                    if (equals(0.dp)) return@run cutoutInsetsPadding.value
                        .calculateEndPadding(layoutDirection)
                    this
                },
                bottom = systemBarsInsetsPadding.value.calculateBottomPadding().run {
                    if (equals(0.dp)) return@run cutoutInsetsPadding.value
                        .calculateBottomPadding()
                    this
                }
            )
        }
        val bottomBarPadding = remember(mainState.value.bottomBarPadding) {
            (mainState.value.bottomBarPadding * 4f).dp
        }

        val bookProgress = remember(
            state.value.book.progress,
            state.value.text.size,
            state.value.isParsing,
            mainState.value.progressCount
        ) {
            if (state.value.isParsing || state.value.text.isEmpty()) {
                return@remember "${state.value.book.progress.calculateProgress(2)}%"
            }

            when (mainState.value.progressCount) {
                ReaderProgressCount.PERCENTAGE -> {
                    "${state.value.book.progress.calculateProgress(2)}%"
                }

                ReaderProgressCount.QUANTITY -> {
                    val index =
                        (state.value.book.progress * state.value.text.lastIndex + 1).roundToInt()
                    "$index / ${state.value.text.size}"
                }
            }
        }
        val chapterProgress = remember(
            state.value.text.size,
            state.value.book.progress,
            state.value.currentChapter,
            state.value.currentChapterProgress,
            state.value.isParsing,
            mainState.value.progressCount
        ) {
            if (state.value.isParsing) return@remember ""
            if (state.value.currentChapter == null) return@remember ""
            when (mainState.value.progressCount) {
                ReaderProgressCount.PERCENTAGE -> {
                    " (${state.value.currentChapterProgress.calculateProgress(2)}%)"
                }

                ReaderProgressCount.QUANTITY -> {
                    val (index, length) = screenModel.findChapterIndexAndLength(
                        (state.value.book.progress * state.value.text.lastIndex).roundToInt()
                    ).apply { if (first == -1 && second == -1) return@remember "" }
                    " (${index} / ${length})"
                }
            }
        }
        val progress = remember(bookProgress, chapterProgress) {
            "${bookProgress}${chapterProgress}"
        }

        LaunchedEffect(Unit) {
            screenModel.init(
                bookId = bookId,
                fullscreenMode = mainState.value.fullscreen,
                activity = activity,
                navigateBack = {
                    navigator.pop()
                }
            )
        }
        LaunchedEffect(activePdfMode, state.value.book.id, pdfState.value.book.id) {
            if (activePdfMode && state.value.book.id == bookId) {
                pdfScreenModel.init(
                    bookId = bookId,
                    fullscreenMode = mainState.value.fullscreen,
                    activity = activity,
                    navigateBack = {
                        navigator.pop()
                    }
                )
            }

            // Switched to parsed text while the PDF model still holds this book: load/reuse the
            // text in ReaderModel, then release the renderer. activePdfMode is driven by
            // state.book.pdfReadingMode, so resetting pdfState (id = -1) can't flip it back.
            if (!activePdfMode && pdfState.value.book.id == bookId) {
                screenModel.init(
                    bookId = bookId,
                    fullscreenMode = mainState.value.fullscreen,
                    activity = activity,
                    navigateBack = {
                        navigator.pop()
                    }
                )
                pdfScreenModel.resetScreen()
            }
        }
        LaunchedEffect(mainState.value.fullscreen) {
            if (activePdfMode) {
                pdfScreenModel.onEvent(
                    PdfReaderEvent.OnMenuVisibility(
                        show = pdfState.value.showMenu,
                        fullscreenMode = mainState.value.fullscreen,
                        activity = activity
                    )
                )
            } else {
                screenModel.onEvent(
                    ReaderEvent.OnMenuVisibility(
                        show = state.value.showMenu,
                        fullscreenMode = mainState.value.fullscreen,
                        saveCheckpoint = false,
                        activity = activity
                    )
                )
            }
        }
        LaunchedEffect(listState, readerDisplayContent) {
            screenModel.updateProgress(
                listState = listState,
                displayIndexToTextIndex = readerDisplayContent::displayIndexToTextIndex,
                textIndexToDisplayIndex = readerDisplayContent::textIndexToDisplayIndex
            )
        }
        LaunchedEffect(pdfListState) {
            pdfScreenModel.updateProgress(pdfListState)
        }
        LaunchedEffect(activity) {
            readerInlineContentModel.configure(activity)
        }
        LaunchedEffect(
            listState,
            activePdfMode,
            state.value.text.size,
            readerDisplayContent,
            readerAvailableForInlineContent
        ) {
            snapshotFlow {
                val firstVisibleDisplayIndex = listState.firstVisibleItemIndex
                val visibleEndDisplayIndex = listState.layoutInfo.visibleItemsInfo
                    .maxOfOrNull { it.index } ?: firstVisibleDisplayIndex

                Triple(
                    readerDisplayContent.displayIndexToTextIndex(firstVisibleDisplayIndex),
                    readerDisplayContent.displayIndexToTextIndex(visibleEndDisplayIndex),
                    state.value.text.lastIndex
                )
            }
                .collect { (index, visibleEndIndex, lastIndex) ->
                    if (!activePdfMode && lastIndex >= 0) {
                        readerInlineContentModel.onReaderProgress(
                            activity = activity,
                            mode = ReaderInlineContentMode.TEXT,
                            progressUnit = index,
                            visibleEndProgressUnit = visibleEndIndex,
                            lastProgressUnit = lastIndex,
                            readerAvailableForInlineContent = readerAvailableForInlineContent
                        )
                    }
                }
        }
        LaunchedEffect(pdfListState, activePdfMode, pdfState.value.pageCount, readerAvailableForInlineContent) {
            snapshotFlow {
                val firstVisibleIndex = pdfListState.firstVisibleItemIndex
                val visibleEndIndex = pdfListState.layoutInfo.visibleItemsInfo
                    .maxOfOrNull { it.index } ?: firstVisibleIndex

                Triple(
                    firstVisibleIndex,
                    visibleEndIndex,
                    pdfState.value.pageCount - 1
                )
            }
                .collect { (index, visibleEndIndex, lastIndex) ->
                    if (activePdfMode && lastIndex >= 0) {
                        readerInlineContentModel.onReaderProgress(
                            activity = activity,
                            mode = ReaderInlineContentMode.PDF,
                            progressUnit = index,
                            visibleEndProgressUnit = visibleEndIndex,
                            lastProgressUnit = lastIndex,
                            readerAvailableForInlineContent = readerAvailableForInlineContent
                        )
                    }
                }
        }
        LaunchedEffect(
            pdfState.value.pageCount,
            activePdfMode,
            pdfState.value.book.pdfPageIndex,
            mainState.value.pdfPageDisplayMode
        ) {
            if (activePdfMode && pdfState.value.pageCount > 0) {
                pdfListState.scrollToItem(
                    pdfState.value.book.pdfPageIndex.coerceIn(0, pdfState.value.pageCount - 1),
                    if (mainState.value.pdfPageDisplayMode == PdfPageDisplayMode.PAGED) {
                        0
                    } else pdfState.value.book.pdfPageOffset
                )
            }
        }

        DisposableEffect(mainState.value.screenOrientation) {
            activity.requestedOrientation = mainState.value.screenOrientation.code
            onDispose {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            }
        }
        DisposableEffect(
            mainState.value.screenBrightness,
            mainState.value.customScreenBrightness
        ) {
            when (mainState.value.customScreenBrightness) {
                true -> activity.setBrightness(brightness = mainState.value.screenBrightness)
                false -> activity.setBrightness(brightness = null)
            }

            onDispose {
                activity.setBrightness(brightness = null)
            }
        }
        DisposableEffect(mainState.value.keepScreenOn) {
            when (mainState.value.keepScreenOn) {
                true -> activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                false -> activity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
            onDispose {
                activity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }

        DisposableEffect(Unit) {
            val screen = this@ReaderScreen
            onDispose {
                if (screen !in navigator.items.value) {
                    readerInlineContentModel.resetSession()
                    screenModel.resetScreen()
                    pdfScreenModel.resetScreen()
                    WindowCompat.getInsetsController(
                        activity.window,
                        activity.window.decorView
                    ).show(WindowInsetsCompat.Type.systemBars())
                }
            }
        }

        if (activePdfMode) {
            PdfReaderContent(
                book = pdfState.value.book.takeIf { it.id == bookId } ?: state.value.book,
                pageCount = pdfState.value.pageCount,
                listState = pdfListState,
                isLoading = pdfState.value.isLoading,
                errorMessage = pdfState.value.errorMessage,
                showMenu = pdfState.value.showMenu,
                bottomSheet = pdfState.value.bottomSheet,
                zoom = pdfState.value.zoom,
                zoomPageIndex = pdfState.value.zoomPageIndex,
                zoomSessionId = pdfState.value.zoomSessionId,
                pdfTextModeUnavailable = state.value.pdfTextModeUnavailable ||
                        !state.value.book.pdfTextModeAvailable,
                contentPadding = contentPadding,
                bottomBarPadding = bottomBarPadding,
                backgroundColor = backgroundColor.value,
                pageDisplayMode = mainState.value.pdfPageDisplayMode,
                showZoomControls = mainState.value.pdfShowZoomControls,
                pinchZoom = mainState.value.pdfPinchZoom,
                fullscreenMode = mainState.value.fullscreen,
                inlineContentPlacements = inlineContentState.value.placements,
                createInlineContentView = { placementId ->
                    readerInlineContentModel.createView(activity, placementId)
                },
                renderPage = pdfScreenModel::renderPage,
                menuVisibility = pdfScreenModel::onEvent,
                scrollToPage = pdfScreenModel::onEvent,
                changeZoom = pdfScreenModel::onEvent,
                changePdfReadingMode = { event ->
                    // PdfReaderModel does native cleanup (session record + renderer release);
                    // ReaderModel owns the mode flip + DB write (single source of truth).
                    pdfScreenModel.onEvent(event)
                    screenModel.onEvent(
                        ReaderEvent.OnChangePdfReadingMode(event.mode)
                    )
                },
                changePdfDefaultReadingMode = {
                    mainModel.onEvent(MainEvent.OnChangePdfDefaultReadingMode(it.name))
                },
                showPdfReadingModeBottomSheet = pdfScreenModel::onEvent,
                showSettingsBottomSheet = pdfScreenModel::onEvent,
                dismissBottomSheet = pdfScreenModel::onEvent,
                leave = pdfScreenModel::onEvent,
                navigateBack = {
                    navigator.pop()
                },
                navigateToBookInfo = {
                    navigator.push(
                        BookInfoScreen(
                            bookId = bookId,
                        ),
                        popping = true,
                        saveInBackStack = false
                    )
                }
            )
        } else {
            ReaderContent(
                book = state.value.book,
                text = state.value.text,
                displayContent = readerDisplayContent,
                chapters = state.value.chapters,
                bottomSheet = state.value.bottomSheet,
                translation = state.value.translation,
                bookTranslation = state.value.bookTranslation,
                drawer = state.value.drawer,
                bookmarks = state.value.bookmarks,
                highlightsByParagraph = highlightsByParagraph,
                editingAnnotation = state.value.editingAnnotation,
                pendingAnnotationText = state.value.pendingAnnotationText,
                pendingAnnotationColorArgb = state.value.pendingAnnotationColorArgb,
                highlightPaletteText = state.value.highlightPaletteText,
                highlightPaletteAnnotation = state.value.highlightPaletteAnnotation,
                highlightPaletteAnchorX = state.value.highlightPaletteAnchorX,
                highlightPaletteAnchorY = state.value.highlightPaletteAnchorY,
                highlightColors = state.value.highlightColors,
                showHighlightPaletteEditor = state.value.showHighlightPaletteEditor,
                focusedBookmarkId = state.value.focusedBookmarkId,
                pendingBookmarkNavigation = state.value.pendingBookmarkNavigation,
                pendingBookmarkDisplayIndex = state.value.pendingBookmarkDisplayIndex,
                listState = listState,
                currentChapter = state.value.currentChapter,
                nestedScrollConnection = nestedScrollConnection.value,
                fastColorPresetChange = mainState.value.fastColorPresetChange,
                perceptionExpander = mainState.value.perceptionExpander,
                perceptionExpanderPadding = perceptionExpanderPadding,
                perceptionExpanderThickness = perceptionExpanderThickness,
                currentChapterProgress = state.value.currentChapterProgress,
                isLoading = state.value.isLoading,
                isParsing = state.value.isParsing,
                errorMessage = state.value.errorMessage,
                pdfTextModeUnavailable = state.value.pdfTextModeUnavailable ||
                        !state.value.book.pdfTextModeAvailable,
                checkpoint = state.value.checkpoint,
                showMenu = state.value.showMenu,
                lockMenu = state.value.lockMenu,
                contentPadding = contentPadding,
                verticalPadding = verticalPadding,
                horizontalGesture = mainState.value.horizontalGesture,
                horizontalGestureScroll = mainState.value.horizontalGestureScroll,
                horizontalGestureSensitivity = horizontalGestureSensitivity,
                horizontalGestureAlphaAnim = mainState.value.horizontalGestureAlphaAnim,
                horizontalGesturePullAnim = mainState.value.horizontalGesturePullAnim,
                highlightedReading = mainState.value.highlightedReading,
                highlightedReadingThickness = highlightedReadingThickness,
                progress = progress,
                progressBar = mainState.value.progressBar,
                progressBarPadding = progressBarPadding,
                progressBarAlignment = mainState.value.progressBarAlignment,
                progressBarFontSize = progressBarFontSize,
                paragraphHeight = paragraphHeight,
                sidePadding = sidePadding,
                bottomBarPadding = bottomBarPadding,
                backgroundColor = backgroundColor.value,
                fontColor = fontColor.value,
                images = mainState.value.images,
                imagesCornersRoundness = imagesCornersRoundness,
                imagesAlignment = mainState.value.imagesAlignment,
                imagesWidth = imagesWidth,
                imagesColorEffects = imagesColorEffects,
                fontFamily = fontFamily,
                lineHeight = lineHeight,
                fontThickness = mainState.value.fontThickness,
                fontStyle = fontStyle,
                chapterTitleAlignment = mainState.value.chapterTitleAlignment,
                textAlignment = mainState.value.textAlignment,
                horizontalAlignment = horizontalAlignment,
                fontSize = mainState.value.fontSize.sp,
                letterSpacing = letterSpacing,
                paragraphIndentation = paragraphIndentation,
                doubleClickTranslation = mainState.value.doubleClickTranslation,
                translationProviderMode = mainState.value.translationProviderMode.name,
                translationSourceLanguage = mainState.value.translationSourceLanguage,
                translationTargetLanguage = mainState.value.translationTargetLanguage,
                translationWifiOnly = mainState.value.translationWifiOnly,
                fullscreenMode = mainState.value.fullscreen,
                customScreenBrightness = mainState.value.customScreenBrightness,
                screenBrightness = mainState.value.screenBrightness,
                changeScreenBrightness = { brightness ->
                    mainModel.onEvent(MainEvent.OnChangeScreenBrightness(brightness))
                },
                createInlineContentView = { placementId ->
                    readerInlineContentModel.createView(activity, placementId)
                },
                isAutoScrolling = state.value.isAutoScrolling,
                autoScrollSpeed = mainState.value.autoScrollSpeed,
                isAutoScrollPaused = state.value.isAutoScrollPaused,
                autoScrollChipAlignment = mainState.value.autoScrollChipAlignment,
                autoScrollChipOpacity = mainState.value.autoScrollChipOpacity,
                autoScrollChipOpacityEnabled = mainState.value.autoScrollChipOpacityEnabled,
                autoScrollChipPlayPause = mainState.value.autoScrollChipPlayPause,
                onSetAutoScrolling = screenModel::onEvent,
                onSetAutoScrollPaused = screenModel::onEvent,
                onChangeAutoScrollSpeed = {
                    mainModel.onEvent(MainEvent.OnChangeAutoScrollSpeed(it))
                },
                selectPreviousPreset = settingsModel::onEvent,
                selectNextPreset = settingsModel::onEvent,
                leave = screenModel::onEvent,
                restoreCheckpoint = screenModel::onEvent,
                scroll = screenModel::onEvent,
                changeProgress = screenModel::onEvent,
                menuVisibility = screenModel::onEvent,
                openShareApp = screenModel::onEvent,
                openWebBrowser = screenModel::onEvent,
                openTranslator = screenModel::onEvent,
                translateText = screenModel::onEvent,
                openExternalTranslator = screenModel::onEvent,
                dismissTranslation = screenModel::onEvent,
                toggleTranslationOriginal = screenModel::onEvent,
                openDictionary = screenModel::onEvent,
                scrollToChapter = screenModel::onEvent,
                showPdfReadingModeBottomSheet = screenModel::onEvent,
                showBookTranslationBottomSheet = screenModel::onEvent,
                showSettingsBottomSheet = screenModel::onEvent,
                dismissBottomSheet = screenModel::onEvent,
                startBookTranslation = screenModel::onEvent,
                showTranslatedBook = screenModel::onEvent,
                showOriginalBook = screenModel::onEvent,
                confirmBookTranslationGoogleWarning = screenModel::onEvent,
                dismissBookTranslationGoogleWarning = screenModel::onEvent,
                cancelBookTranslation = screenModel::onEvent,
                pauseBookTranslation = screenModel::onEvent,
                resumeBookTranslation = screenModel::onEvent,
                retryBookTranslation = screenModel::onEvent,
                changeBookTranslationProviderMode = screenModel::onEvent,
                changeBookTranslationSourceLanguage = screenModel::onEvent,
                changeBookTranslationTargetLanguage = screenModel::onEvent,
                swapBookTranslationLanguages = screenModel::onEvent,
                changeBookTranslationWifiOnly = { event ->
                    mainModel.onEvent(MainEvent.OnChangeTranslationWifiOnly(event.requireWifi))
                    screenModel.onEvent(event)
                },
                dismissBookTranslationError = screenModel::onEvent,
                showChaptersDrawer = screenModel::onEvent,
                showBookmarksDrawer = screenModel::onEvent,
                scrollToBookmark = screenModel::onEvent,
                bookmarkScrollFinished = screenModel::onEvent,
                deleteBookmark = screenModel::onEvent,
                createBookmark = screenModel::onEvent,
                showHighlightPalette = screenModel::onEvent,
                createHighlight = screenModel::onEvent,
                applyHighlightPaletteColor = screenModel::onEvent,
                requestAnnotationEditor = screenModel::onEvent,
                changeHighlightColor = screenModel::onEvent,
                clearHighlightColor = screenModel::onEvent,
                editAnnotation = screenModel::onEvent,
                saveAnnotation = screenModel::onEvent,
                dismissAnnotationEditor = screenModel::onEvent,
                dismissHighlightPalette = screenModel::onEvent,
                showPaletteEditor = screenModel::onEvent,
                dismissPaletteEditor = screenModel::onEvent,
                updateHighlightPalette = screenModel::onEvent,
                dismissDrawer = screenModel::onEvent,
                changePdfReadingMode = screenModel::onEvent,
                changePdfDefaultReadingMode = {
                    mainModel.onEvent(MainEvent.OnChangePdfDefaultReadingMode(it.name))
                },
                navigateBack = {
                    navigator.pop()
                },
                navigateToBookInfo = { changePath ->
                    if (changePath) BookInfoScreen.changePathChannel.trySend(true)
                    navigator.push(
                        BookInfoScreen(
                            bookId = bookId,
                        ),
                        popping = true,
                        saveInBackStack = false
                    )
                }
            )
        }
    }
}
