/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.ui.reader

import android.app.Application
import android.app.SearchManager
import android.content.Intent
import android.net.Uri
import android.os.SystemClock
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.snapshotFlow
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import com.byteflipper.everbook.R
import com.byteflipper.everbook.domain.reader.Bookmark
import com.byteflipper.everbook.domain.reader.BookmarkKind
import com.byteflipper.everbook.domain.reader.PdfReadingMode
import com.byteflipper.everbook.domain.reader.Checkpoint
import com.byteflipper.everbook.domain.reader.HighlightPalette
import com.byteflipper.everbook.data.tts.TtsPlaybackService
import com.byteflipper.everbook.data.tts.TtsSessionManager
import com.byteflipper.everbook.domain.reader.ReaderText
import com.byteflipper.everbook.domain.reader.ReaderText.Chapter
import com.byteflipper.everbook.domain.reader.tts.TtsPreferences
import com.byteflipper.everbook.domain.reader.tts.TtsSessionState
import com.byteflipper.everbook.domain.translation.AUTO_TRANSLATION_LANGUAGE
import com.byteflipper.everbook.domain.translation.BookTranslation
import com.byteflipper.everbook.domain.translation.BookTranslationStatus
import com.byteflipper.everbook.domain.translation.FALLBACK_TRANSLATION_TARGET_LANGUAGE
import com.byteflipper.everbook.domain.translation.TranslationFeature
import com.byteflipper.everbook.domain.translation.TranslationProviderMode
import com.byteflipper.everbook.domain.translation.TranslationRequest
import com.byteflipper.everbook.domain.translation.TranslationResult
import com.byteflipper.everbook.domain.translation.normalizeTranslationLanguageCode
import com.byteflipper.everbook.domain.translation.resolveTranslationLanguageCode
import com.byteflipper.everbook.domain.translation.toTranslationProviderMode
import com.byteflipper.everbook.domain.repository.TranslationRepository
import com.byteflipper.everbook.domain.ui.UIText
import com.byteflipper.everbook.domain.use_case.book.GetBookById
import com.byteflipper.everbook.domain.use_case.book.GetText
import com.byteflipper.everbook.domain.library.category.CategoryDefaults
import com.byteflipper.everbook.domain.use_case.book.UpdateBook
import com.byteflipper.everbook.domain.use_case.data_store.GetDatastore
import com.byteflipper.everbook.domain.use_case.data_store.SetDatastore
import com.byteflipper.everbook.domain.use_case.bookmark.DeleteBookmark
import com.byteflipper.everbook.domain.use_case.bookmark.ObserveBookmarks
import com.byteflipper.everbook.domain.use_case.bookmark.UpsertBookmark
import com.byteflipper.everbook.domain.use_case.history.GetLatestHistory
import com.byteflipper.everbook.domain.statistics.ReadingSession
import com.byteflipper.everbook.domain.use_case.statistics.RecordReadingSession
import com.byteflipper.everbook.domain.use_case.translation.CancelBookTranslation
import com.byteflipper.everbook.domain.use_case.translation.DeleteBookTranslation
import com.byteflipper.everbook.domain.use_case.translation.EnqueueBookTranslation
import com.byteflipper.everbook.domain.use_case.translation.GetBookTranslations
import com.byteflipper.everbook.domain.use_case.translation.GetTranslationCapability
import com.byteflipper.everbook.domain.use_case.translation.ObserveBookTranslations
import com.byteflipper.everbook.domain.use_case.translation.ObserveTranslatedBookText
import com.byteflipper.everbook.domain.use_case.translation.PauseBookTranslation
import com.byteflipper.everbook.domain.use_case.translation.RetryBookTranslation
import com.byteflipper.everbook.domain.use_case.translation.ResumeBookTranslation
import com.byteflipper.everbook.domain.use_case.translation.TranslateText
import com.byteflipper.everbook.presentation.core.constants.DataStoreConstants
import com.byteflipper.everbook.presentation.core.util.coerceAndPreventNaN
import com.byteflipper.everbook.presentation.core.util.launchActivity
import com.byteflipper.everbook.presentation.core.util.setBrightness
import com.byteflipper.everbook.presentation.core.util.showToast
import com.byteflipper.everbook.ui.history.HistoryScreen
import com.byteflipper.everbook.ui.library.LibraryScreen
import javax.inject.Inject
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds

private const val READER = "READER, MODEL"
private const val BOOK_TRANSLATION_LOG = "BookTranslation"
private const val READER_UI_MIN_UPDATE_ITEMS = 640
private const val READER_UI_MIN_UPDATE_MS = 300L

// Leading text of the bookmarked paragraph, kept as a label and re-anchor hint.
private const val BOOKMARK_SNIPPET_MAX_CHARS = 160

// Stored quote snapshot for a highlight; caps runaway selections in the drawer/DB.
private const val HIGHLIGHT_QUOTE_MAX_CHARS = 400

// Paragraphs past the last visible row to still scan when resolving a selection that ends just
// below the fold.
private const val HIGHLIGHT_SEARCH_LOOKAHEAD = 2

private const val ANCHOR_CONTEXT_CHARS = 32

// ponytail: trailing-idle cap only — a mid-read pause still counts. Per-gap idle
// splitting is the upgrade path if reading time starts looking inflated.
private const val SESSION_IDLE_CAP_MS = 5 * 60 * 1000L

private data class ReaderTextScrollAnchor(
    val textIndex: Int,
    val offset: Int
)

// A located annotation range in the original reader text.
private data class SelectionAnchor(
    val paragraphIndex: Int,
    val charStart: Int,
    val charEnd: Int,
    val quoted: String,
    val paragraphHash: Int,
    val prefix: String,
    val suffix: String
)

private enum class SelectionAnchorFailure {
    EMPTY,
    NOT_FOUND,
    MULTI_PARAGRAPH,
    AMBIGUOUS,
    TRANSLATED_TEXT
}

private data class SelectionAnchorResult(
    val anchor: SelectionAnchor? = null,
    val failure: SelectionAnchorFailure? = null
)

@HiltViewModel
class ReaderModel @Inject constructor(
    private val application: Application,
    private val getBookById: GetBookById,
    private val updateBook: UpdateBook,
    private val getText: GetText,
    private val getLatestHistory: GetLatestHistory,
    private val getDatastore: GetDatastore,
    private val setDatastore: SetDatastore,
    private val getTranslationCapability: GetTranslationCapability,
    private val translateText: TranslateText,
    private val translationRepository: TranslationRepository,
    private val getBookTranslations: GetBookTranslations,
    private val observeBookTranslations: ObserveBookTranslations,
    private val observeTranslatedBookText: ObserveTranslatedBookText,
    private val enqueueBookTranslationUseCase: EnqueueBookTranslation,
    private val retryBookTranslation: RetryBookTranslation,
    private val pauseBookTranslation: PauseBookTranslation,
    private val resumeBookTranslation: ResumeBookTranslation,
    private val cancelBookTranslation: CancelBookTranslation,
    private val deleteBookTranslation: DeleteBookTranslation,
    private val recordReadingSession: RecordReadingSession,
    private val observeBookmarks: ObserveBookmarks,
    private val upsertBookmark: UpsertBookmark,
    private val deleteBookmark: DeleteBookmark,
    private val ttsSessionManager: TtsSessionManager
) : ViewModel() {

    private val mutex = Mutex()

    private val _state = MutableStateFlow(ReaderState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val stored = getDatastore.execute(DataStoreConstants.READER_HIGHLIGHT_PALETTE)
            _state.update { it.copy(highlightColors = HighlightPalette.decode(stored)) }
        }
        viewModelScope.launch {
            translationRepository.isModelDownloadInProgress.collect { isDownloading ->
                _state.update {
                    it.copy(
                        translation = it.translation.copy(isDownloadingModel = isDownloading),
                        bookTranslation = it.bookTranslation.copy(isDownloadingModel = isDownloading)
                    )
                }
            }
        }
        viewModelScope.launch {
            ttsSessionManager.state.collect(::onTtsSessionChanged)
        }
    }

    private suspend fun onTtsSessionChanged(session: TtsSessionState) {
        val current = _state.value
        // The session outlives the reader screen, so a state belonging to another book must not
        // paint highlights over the one being displayed.
        val belongsToCurrentBook = session.isActive && session.bookId == current.book.id
        val tts = if (belongsToCurrentBook) {
            ReaderTtsState(
                playbackState = session.playbackState,
                textIndex = session.textIndex,
                sentenceRange = session.sentenceRange
                    .takeIf { session.preferences.highlightSentence },
                speechRate = session.preferences.speechRate,
                failure = session.failure
            )
        } else {
            ReaderTtsState(speechRate = session.preferences.speechRate)
        }

        if (current.tts != tts) {
            _state.update { it.copy(tts = tts) }
        }
    }

    private suspend fun startTts(preferences: TtsPreferences) {
        val current = _state.value
        if (current.text.isEmpty()) return

        val startIndex = displayIndexToTextIndex(current.listState.firstVisibleItemIndex)
        _state.update { it.copy(isAutoScrolling = false, isAutoScrollPaused = false) }

        val started = ttsSessionManager.start(
            bookId = current.book.id,
            bookTitle = current.book.title,
            entries = current.text,
            startTextIndex = startIndex,
            preferences = preferences
        )
        if (started) {
            TtsPlaybackService.start(application)
        }
    }

    private fun stopTts() {
        ttsSessionManager.stop()
        TtsPlaybackService.stop(application)
    }

    private fun newEventJob(): Job = SupervisorJob(viewModelScope.coroutineContext[Job])

    private var eventJob = newEventJob()
    private var resetJob: Job? = null

    private var scrollJob: Job? = null
    private var progressJob: Job? = null
    private var loadJob: Job? = null
    private var bookTranslationsObserveJob: Job? = null
    private var activeBookTranslationTextJob: Job? = null
    private var bookmarksObserveJob: Job? = null
    private var displayIndexToTextIndex: (Int) -> Int = { it }
    private var textIndexToDisplayIndex: (Int) -> Int = { it }
    private val translationCache = mutableMapOf<String, TranslationResult>()

    // Reading session tracking (statistics). Spans from first text load to OnLeave.
    private var sessionStartTime: Long? = null
    private var sessionProgressStart: Float = 0f
    private var sessionTextIndexStart: Int = 0
    private var sessionLastActiveTime: Long = 0L

    fun onEvent(event: ReaderEvent) {
        CoroutineScope(eventJob + Dispatchers.Main).launch {
            when (event) {
                is ReaderEvent.OnLoadText -> {
                    loadJob?.cancel()
                    bookTranslationsObserveJob?.cancel()
                    activeBookTranslationTextJob?.cancel()
                    bookmarksObserveJob?.cancel()
                    loadJob = launch(Dispatchers.IO) {
                        val accumulated = mutableListOf<ReaderText>()
                        val chapterIndexes = mutableListOf<Int>()
                        val lastOpened = getLatestHistory.execute(_state.value.book.id)?.time
                        var firstChunkReceived = false
                        var renderedItemCount = 0
                        var lastUiUpdateTime = 0L
                        val initialScrollIndex = _state.value.book.scrollIndex.coerceAtLeast(0)
                        val initialScrollOffset = _state.value.book.scrollOffset
                        val initialProgress = _state.value.book.progress
                            .coerceAndPreventNaN()
                            .coerceIn(0f, 1f)

                        // Start a reading session on first load; reloads (e.g. translation
                        // toggles) keep the same session until the reader is left.
                        if (sessionStartTime == null) {
                            val now = System.currentTimeMillis()
                            sessionStartTime = now
                            sessionLastActiveTime = now
                            sessionProgressStart = initialProgress
                            sessionTextIndexStart = initialScrollIndex
                        }

                        fun resolveInitialTargetIndex(
                            textSize: Int,
                            isParsing: Boolean
                        ): Int? {
                            if (textSize < 1) return null
                            val lastIndex = textSize - 1

                            // Progress survives parser re-chunking better than saved item indexes.
                            if (initialProgress > 0f) {
                                if (isParsing) return null
                                return (initialProgress * lastIndex)
                                    .roundToInt()
                                    .coerceIn(0, lastIndex)
                            }

                            if (initialScrollIndex <= 0) return 0
                            if (initialScrollIndex <= lastIndex) return initialScrollIndex
                            if (isParsing) return null
                            return lastIndex
                        }

                        val restoreInitialScrollJob = launch(Dispatchers.Main) {
                            snapshotFlow {
                                val state = _state.value
                                Triple(
                                    state.text.size,
                                    state.listState.layoutInfo.totalItemsCount,
                                    state.isParsing
                                )
                            }.first { (textSize, itemsCount, isParsing) ->
                                if (textSize < 1 || itemsCount < 1) return@first false
                                val targetIndex = resolveInitialTargetIndex(
                                    textSize = textSize,
                                    isParsing = isParsing
                                ) ?: return@first false
                                itemsCount > targetIndex
                            }

                            val targetIndex = resolveInitialTargetIndex(
                                textSize = _state.value.text.size,
                                isParsing = _state.value.isParsing
                            ) ?: 0
                            _state.value.listState.requestScrollToItem(
                                targetIndex,
                                if (targetIndex == initialScrollIndex) initialScrollOffset else 0
                            )
                            updateChapter(index = targetIndex)
                        }

                        suspend fun publishText(force: Boolean) {
                            if (accumulated.isEmpty()) return

                            val now = SystemClock.elapsedRealtime()
                            if (
                                !force &&
                                firstChunkReceived &&
                                accumulated.size - renderedItemCount < READER_UI_MIN_UPDATE_ITEMS &&
                                now - lastUiUpdateTime < READER_UI_MIN_UPDATE_MS
                            ) {
                                return
                            }

                            val snapshot = accumulated.toList()
                            val chapters = chapterIndexes.mapNotNull { index ->
                                snapshot.getOrNull(index) as? Chapter
                            }
                            renderedItemCount = snapshot.size
                            lastUiUpdateTime = now

                            val firstPublish = !firstChunkReceived
                            if (firstPublish) {
                                firstChunkReceived = true
                                systemBarsVisibility(
                                    show = !event.fullscreenMode,
                                    activity = event.activity
                                )
                            }

                            _state.update {
                                it.copy(
                                    showMenu = if (firstPublish) false else it.showMenu,
                                    book = it.book.copy(
                                        lastOpened = lastOpened
                                    ),
                                    originalText = snapshot,
                                    text = snapshot,
                                    chapters = chapters,
                                    chapterIndexes = chapterIndexes.toList(),
                                    isLoading = false,
                                    isParsing = true,
                                    errorMessage = null,
                                    bookTranslation = it.bookTranslation.withBookTextReadiness(
                                        isReady = false
                                    )
                                )
                            }
                        }

                        val text = try {
                            getText.execute(_state.value.book.id) { chunk ->
                                if (chunk.isNotEmpty()) {
                                    val startIndex = accumulated.size
                                    accumulated.addAll(chunk)
                                    chunk.forEachIndexed { index, readerText ->
                                        if (readerText is Chapter) {
                                            chapterIndexes.add(startIndex + index)
                                        }
                                    }
                                    publishText(force = !firstChunkReceived)
                                }
                            }
                        } catch (_: Exception) {
                            emptyList()
                        }

                        yield()

                        if (text.isEmpty()) {
                            if (_state.value.book.filePath.endsWith(".pdf", ignoreCase = true)) {
                                _state.update {
                                    it.copy(
                                        book = it.book.copy(
                                            pdfReadingMode = PdfReadingMode.ORIGINAL_PDF,
                                            pdfTextModeAvailable = false
                                        ),
                                        isLoading = false,
                                        isParsing = false,
                                        errorMessage = null,
                                        pdfTextModeUnavailable = true,
                                        bookTranslation = it.bookTranslation.withBookTextReadiness(
                                            isReady = false
                                        )
                                    )
                                }
                                updateBook.execute(_state.value.book)
                                restoreInitialScrollJob.cancel()
                                return@launch
                            }

                            _state.update {
                                it.copy(
                                    isLoading = false,
                                    isParsing = false,
                                    errorMessage = if (firstChunkReceived) {
                                        null
                                    } else {
                                        UIText.StringResource(R.string.error_could_not_get_text)
                                    },
                                    bookTranslation = it.bookTranslation.withBookTextReadiness(
                                        isReady = false
                                    )
                                )
                            }
                            systemBarsVisibility(show = true, activity = event.activity)
                            restoreInitialScrollJob.cancel()
                            return@launch
                        }

                        if (accumulated.size != text.size) {
                            accumulated.clear()
                            accumulated.addAll(text)
                            chapterIndexes.clear()
                            text.forEachIndexed { index, readerText ->
                                if (readerText is Chapter) chapterIndexes.add(index)
                            }
                        }

                        val finalFirstPublish = !firstChunkReceived
                        if (finalFirstPublish) {
                            firstChunkReceived = true
                            systemBarsVisibility(
                                show = !event.fullscreenMode,
                                activity = event.activity
                            )
                        }

                        _state.update {
                            it.copy(
                                originalText = text,
                                text = text,
                                chapters = chapterIndexes.mapNotNull { index ->
                                    text.getOrNull(index) as? Chapter
                                },
                                chapterIndexes = chapterIndexes.toList(),
                                showMenu = if (finalFirstPublish) false else it.showMenu,
                                book = it.book.copy(
                                    lastOpened = lastOpened
                                ),
                                isLoading = false,
                                isParsing = false,
                                errorMessage = null,
                                bookTranslation = it.bookTranslation.withBookTextReadiness(
                                    isReady = text.isNotEmpty()
                                )
                            )
                        }

                        yield()

                        refreshBookTranslations(_state.value.book.id)
                        observeBookTranslations(_state.value.book.id)
                        observeBookmarks(_state.value.book.id)
                        updateBook.execute(_state.value.book)

                        LibraryScreen.refreshListChannel.trySend(0)
                        HistoryScreen.refreshListChannel.trySend(0)
                    }
                }

                is ReaderEvent.OnMenuVisibility -> {
                    launch {
                        if (_state.value.lockMenu) return@launch

                        yield()

                        systemBarsVisibility(
                            show = event.show || !event.fullscreenMode,
                            activity = event.activity
                        )
                        _state.update {
                            it.copy(
                                showMenu = event.show,
                                checkpoint = _state.value.listState.run {
                                    if (!event.show || !event.saveCheckpoint) return@run it.checkpoint

                                    Checkpoint(
                                        displayIndexToTextIndex(firstVisibleItemIndex),
                                        firstVisibleItemScrollOffset
                                    )
                                }
                            )
                        }
                    }
                }

                is ReaderEvent.OnChangePdfReadingMode -> {
                    launch(Dispatchers.IO) {
                        if (
                            event.mode == PdfReadingMode.PARSED_TEXT &&
                            !_state.value.book.pdfTextModeAvailable
                        ) {
                            return@launch
                        }

                        // ReaderModel is the single source of truth for pdfReadingMode. Refresh
                        // from the DB before writing so the PDF page position saved live by
                        // PdfReaderModel (pdfPageIndex/offset) is not clobbered by a stale copy.
                        val latest = getBookById.execute(_state.value.book.id)
                            ?: _state.value.book
                        val updated = latest.copy(pdfReadingMode = event.mode)
                        _state.update {
                            it.copy(
                                book = updated,
                                // Show the loader only when switching to parsed text with nothing
                                // in memory yet; init() refines/clears this (reuse vs. load).
                                isLoading = event.mode == PdfReadingMode.PARSED_TEXT &&
                                        it.originalText.isEmpty() && it.text.isEmpty(),
                                errorMessage = null,
                                bookTranslation = it.bookTranslation.withBookTextReadiness(
                                    isReady = false
                                )
                            )
                        }
                        updateBook.execute(updated)

                        LibraryScreen.refreshListChannel.trySend(0)
                        HistoryScreen.refreshListChannel.trySend(0)
                    }
                }

                is ReaderEvent.OnShowPdfReadingModeBottomSheet -> {
                    _state.update {
                        it.copy(
                            bottomSheet = ReaderScreen.PDF_READING_MODE_BOTTOM_SHEET,
                            drawer = null
                        )
                    }
                }

                ReaderEvent.OnShowBookTranslationBottomSheet -> {
                    launch(Dispatchers.IO) {
                        Log.i(
                            BOOK_TRANSLATION_LOG,
                            "Opening book translation sheet: bookId=${_state.value.book.id}"
                        )
                        refreshBookTranslations(_state.value.book.id)
                        _state.update {
                            it.copy(
                                bottomSheet = ReaderScreen.BOOK_TRANSLATION_BOTTOM_SHEET,
                                drawer = null
                            )
                        }
                    }
                }

                is ReaderEvent.OnChangeProgress -> {
                    launch(Dispatchers.IO) {
                        _state.update {
                            it.copy(
                                book = it.book.copy(
                                    progress = event.progress,
                                    scrollIndex = event.firstVisibleItemIndex,
                                    scrollOffset = event.firstVisibleItemOffset
                                )
                            )
                        }

                        // Persist progress so it survives process death, but DON'T notify the
                        // library/history here: they aren't visible while reading, and OnLeave
                        // already refreshes both on exit with the final progress. Pinging on every
                        // scroll pause forced a background getBooksFromDatabase reload that competed
                        // with the reader's own DB writes — pure churn.
                        updateBook.execute(_state.value.book)
                    }
                }

                is ReaderEvent.OnScrollToChapter -> {
                    launch {
                        _state.value.apply {
                            val chapterPosition = chapters.indexOfFirst {
                                it.id == event.chapter.id
                            }.takeIf { it != -1 }
                            val chapterIndex = chapterPosition?.let {
                                chapterIndexes.getOrNull(it)
                            }
                            if (chapterIndex == null) {
                                return@launch
                            }

                            listState.requestScrollToItem(textIndexToDisplayIndex(chapterIndex))
                            updateChapter(index = chapterIndex)
                            onEvent(
                                ReaderEvent.OnChangeProgress(
                                    progress = calculateProgress(chapterIndex),
                                    firstVisibleItemIndex = chapterIndex,
                                    firstVisibleItemOffset = 0
                                )
                            )
                        }
                    }
                }

                is ReaderEvent.OnScroll -> {
                    scrollJob?.cancel()
                    scrollJob = launch {
                        delay(300.milliseconds)
                        yield()

                        val scrollTo = (_state.value.text.lastIndex * event.progress).roundToInt()
                        _state.value.listState.requestScrollToItem(textIndexToDisplayIndex(scrollTo))
                        updateChapter(scrollTo)
                    }
                }

                is ReaderEvent.OnRestoreCheckpoint -> {
                    launch {
                        _state.value.apply {
                            listState.requestScrollToItem(
                                textIndexToDisplayIndex(checkpoint.index),
                                checkpoint.offset
                            )

                            updateChapter(checkpoint.index)
                            onEvent(
                                ReaderEvent.OnChangeProgress(
                                    progress = calculateProgress(checkpoint.index),
                                    firstVisibleItemIndex = checkpoint.index,
                                    firstVisibleItemOffset = checkpoint.offset,
                                )
                            )
                        }
                    }
                }

                is ReaderEvent.OnLeave -> {
                    launch {
                        yield()

                        stopTts()
                        _state.update {
                            it.copy(
                                lockMenu = true,
                                isAutoScrolling = false
                            )
                        }

                        _state.value.listState.apply {
                            if (
                                _state.value.isLoading ||
                                layoutInfo.totalItemsCount < 1 ||
                                _state.value.text.isEmpty() ||
                                _state.value.errorMessage != null
                            ) return@apply

                            val textIndex = displayIndexToTextIndex(firstVisibleItemIndex)
                            _state.update {
                                it.copy(
                                    book = it.book.copy(
                                        progress = calculateProgress(textIndex),
                                        scrollIndex = textIndex,
                                        scrollOffset = firstVisibleItemScrollOffset
                                    )
                                )
                            }

                            applyAutoStatusOnSave()
                            updateBook.execute(_state.value.book)

                            sessionStartTime?.let { start ->
                                val endTime = System.currentTimeMillis()
                                    .coerceAtMost(sessionLastActiveTime + SESSION_IDLE_CAP_MS)
                                val pagesRead = (textIndex - sessionTextIndexStart)
                                    .coerceAtLeast(0)
                                recordReadingSession.execute(
                                    ReadingSession(
                                        bookId = _state.value.book.id,
                                        startTime = start,
                                        endTime = endTime,
                                        progressStart = sessionProgressStart,
                                        progressEnd = _state.value.book.progress,
                                        pagesRead = pagesRead
                                    )
                                )
                                sessionStartTime = null
                            }

                            LibraryScreen.refreshListChannel.trySend(0)
                            HistoryScreen.refreshListChannel.trySend(0)
                        }

                        WindowCompat.getInsetsController(
                            event.activity.window,
                            event.activity.window.decorView
                        ).show(WindowInsetsCompat.Type.systemBars())
                        event.activity.setBrightness(brightness = null)

                        event.navigate()
                    }
                }

                is ReaderEvent.OnOpenTranslator -> openExternalTranslator(
                    textToTranslate = event.textToTranslate,
                    translateWholeParagraph = event.translateWholeParagraph,
                    activity = event.activity
                )

                is ReaderEvent.OnOpenExternalTranslator -> openExternalTranslator(
                    textToTranslate = event.textToTranslate,
                    translateWholeParagraph = event.translateWholeParagraph,
                    activity = event.activity
                )

                is ReaderEvent.OnTranslateText -> {
                    val providerMode = event.providerMode.toTranslationProviderMode()
                    if (providerMode == TranslationProviderMode.EXTERNAL) {
                        openExternalTranslator(
                            textToTranslate = event.textToTranslate,
                            translateWholeParagraph = event.translateWholeParagraph,
                            activity = event.activity
                        )
                        return@launch
                    }

                    launchTranslationRequest(
                        text = event.textToTranslate,
                        readerTextIndex = event.readerTextIndex,
                        sourceLanguageCode = event.sourceLanguageCode,
                        targetLanguageCode = event.targetLanguageCode,
                        providerMode = providerMode,
                        requireWifi = event.requireWifi
                    )
                }

                ReaderEvent.OnDismissTranslation -> {
                    _state.update {
                        it.copy(
                            bottomSheet = null,
                            translation = ReaderTranslationState()
                        )
                    }
                }

                ReaderEvent.OnToggleTranslationOriginal -> {
                    _state.update {
                        it.copy(
                            translation = it.translation.copy(
                                showOriginal = !it.translation.showOriginal
                            )
                        )
                    }
                }

                is ReaderEvent.OnChangeBookTranslationProviderMode -> {
                    activeBookTranslationTextJob?.cancel()
                    val providerMode = TranslationFeature.coerceFullBookProviderMode(
                        event.providerMode.toTranslationProviderMode()
                    )
                    Log.d(
                        BOOK_TRANSLATION_LOG,
                        "Provider changed: bookId=${_state.value.book.id} provider=$providerMode"
                    )
                    _state.update {
                        it.copyWithOriginalBookTextIfNeeded(
                            bookTranslation = it.bookTranslation.copy(
                                providerMode = providerMode,
                                currentTranslation = null,
                                displayMode = ReaderBookTranslationDisplayMode.ORIGINAL,
                                activeTranslationId = null,
                                isApplyingTranslation = false,
                                errorMessage = null
                            )
                        )
                    }
                    refreshBookTranslations(_state.value.book.id)
                }

                is ReaderEvent.OnChangeBookTranslationSourceLanguage -> {
                    activeBookTranslationTextJob?.cancel()
                    Log.d(
                        BOOK_TRANSLATION_LOG,
                        "Source language changed: bookId=${_state.value.book.id} " +
                                "source=${event.languageCode}"
                    )
                    _state.update {
                        it.copyWithOriginalBookTextIfNeeded(
                            bookTranslation = it.bookTranslation.copy(
                                sourceLanguageCode = event.languageCode,
                                currentTranslation = null,
                                displayMode = ReaderBookTranslationDisplayMode.ORIGINAL,
                                activeTranslationId = null,
                                isApplyingTranslation = false,
                                errorMessage = null
                            )
                        )
                    }
                    refreshBookTranslations(_state.value.book.id)
                }

                is ReaderEvent.OnChangeBookTranslationTargetLanguage -> {
                    activeBookTranslationTextJob?.cancel()
                    Log.d(
                        BOOK_TRANSLATION_LOG,
                        "Target language changed: bookId=${_state.value.book.id} " +
                                "target=${event.languageCode}"
                    )
                    _state.update {
                        it.copyWithOriginalBookTextIfNeeded(
                            bookTranslation = it.bookTranslation.copy(
                                targetLanguageCode = event.languageCode,
                                currentTranslation = null,
                                displayMode = ReaderBookTranslationDisplayMode.ORIGINAL,
                                activeTranslationId = null,
                                isApplyingTranslation = false,
                                errorMessage = null
                            )
                        )
                    }
                    refreshBookTranslations(_state.value.book.id)
                }

                ReaderEvent.OnSwapBookTranslationLanguages -> {
                    activeBookTranslationTextJob?.cancel()
                    val bookTranslation = _state.value.bookTranslation
                    if (bookTranslation.sourceLanguageCode == AUTO_TRANSLATION_LANGUAGE) {
                        return@launch
                    }
                    Log.d(
                        BOOK_TRANSLATION_LOG,
                        "Languages swapped: bookId=${_state.value.book.id} " +
                                "source=${bookTranslation.targetLanguageCode} " +
                                "target=${bookTranslation.sourceLanguageCode}"
                    )
                    _state.update {
                        it.copyWithOriginalBookTextIfNeeded(
                            bookTranslation = it.bookTranslation.copy(
                                sourceLanguageCode = bookTranslation.targetLanguageCode,
                                targetLanguageCode = bookTranslation.sourceLanguageCode,
                                currentTranslation = null,
                                displayMode = ReaderBookTranslationDisplayMode.ORIGINAL,
                                activeTranslationId = null,
                                isApplyingTranslation = false,
                                errorMessage = null
                            )
                        )
                    }
                    refreshBookTranslations(_state.value.book.id)
                }

                is ReaderEvent.OnChangeBookTranslationWifiOnly -> {
                    _state.update {
                        it.copy(
                            bookTranslation = it.bookTranslation.copy(
                                requireWifi = event.requireWifi,
                                errorMessage = null
                            )
                        )
                    }
                }

                ReaderEvent.OnStartBookTranslation -> {
                    Log.i(
                        BOOK_TRANSLATION_LOG,
                        "Start requested from reader: bookId=${_state.value.book.id}"
                    )
                    enqueueBookTranslation()
                }

                ReaderEvent.OnShowTranslatedBook -> {
                    launch(Dispatchers.IO) {
                        showTranslatedBook()
                    }
                }

                ReaderEvent.OnShowOriginalBook -> {
                    launch(Dispatchers.IO) {
                        showOriginalBook()
                    }
                }

                ReaderEvent.OnConfirmBookTranslationGoogleWarning -> {
                    launch(Dispatchers.IO) {
                        Log.i(
                            BOOK_TRANSLATION_LOG,
                            "Google warning accepted: bookId=${_state.value.book.id}"
                        )
                        setDatastore.execute(
                            DataStoreConstants.BOOK_TRANSLATION_GOOGLE_WARNING_ACCEPTED,
                            true
                        )
                        _state.update {
                            it.copy(
                                bookTranslation = it.bookTranslation.copy(
                                    showGoogleWarning = false
                                )
                            )
                        }
                        enqueueBookTranslation(skipGoogleWarning = true)
                    }
                }

                ReaderEvent.OnDismissBookTranslationGoogleWarning -> {
                    Log.d(
                        BOOK_TRANSLATION_LOG,
                        "Google warning dismissed: bookId=${_state.value.book.id}"
                    )
                    _state.update {
                        it.copy(
                            bookTranslation = it.bookTranslation.copy(showGoogleWarning = false)
                        )
                    }
                }

                is ReaderEvent.OnCancelBookTranslation -> {
                    launch(Dispatchers.IO) {
                        Log.i(
                            BOOK_TRANSLATION_LOG,
                            "Cancelling translation: translationId=${event.translationId}"
                        )
                        cancelBookTranslation.execute(event.translationId)
                        refreshBookTranslations(_state.value.book.id)
                    }
                }

                is ReaderEvent.OnPauseBookTranslation -> {
                    launch(Dispatchers.IO) {
                        Log.i(
                            BOOK_TRANSLATION_LOG,
                            "Pausing translation: translationId=${event.translationId}"
                        )
                        runCatching {
                            pauseBookTranslation.execute(event.translationId)
                        }.onFailure { throwable ->
                            Log.e(
                                BOOK_TRANSLATION_LOG,
                                "Pause failed: translationId=${event.translationId}",
                                throwable
                            )
                            _state.update {
                                it.copy(
                                    bookTranslation = it.bookTranslation.copy(
                                        errorMessage = throwable.message
                                            ?: application.getString(
                                                R.string.book_translation_error_pause_failed
                                            )
                                    )
                                )
                            }
                        }
                        refreshBookTranslations(_state.value.book.id)
                    }
                }

                is ReaderEvent.OnResumeBookTranslation -> {
                    launch(Dispatchers.IO) {
                        Log.i(
                            BOOK_TRANSLATION_LOG,
                            "Resuming translation: translationId=${event.translationId}"
                        )
                        runCatching {
                            resumeBookTranslation.execute(
                                translationId = event.translationId,
                                requireWifi = _state.value.bookTranslation.requireWifi
                            )
                        }.onSuccess { translation ->
                            refreshBookTranslations(_state.value.book.id)
                            showTranslatedBook(
                                translationOverride = translation,
                                allowPartial = true
                            )
                        }.onFailure { throwable ->
                            Log.e(
                                BOOK_TRANSLATION_LOG,
                                "Resume failed: translationId=${event.translationId}",
                                throwable
                            )
                            _state.update {
                                it.copy(
                                    bookTranslation = it.bookTranslation.copy(
                                        errorMessage = throwable.message
                                            ?: application.getString(
                                                R.string.book_translation_error_resume_failed
                                            )
                                    )
                                )
                            }
                            refreshBookTranslations(_state.value.book.id)
                        }
                    }
                }

                is ReaderEvent.OnRetryBookTranslation -> {
                    launch(Dispatchers.IO) {
                        Log.i(
                            BOOK_TRANSLATION_LOG,
                            "Retry requested: translationId=${event.translationId}"
                        )
                        if (
                            _state.value.bookTranslation.currentTranslation?.id == event.translationId &&
                            _state.value.bookTranslation.currentTranslation?.status == BookTranslationStatus.STALE
                        ) {
                            Log.i(
                                BOOK_TRANSLATION_LOG,
                                "Retry starts fresh enqueue for stale translation: " +
                                        "translationId=${event.translationId}"
                            )
                            enqueueBookTranslation(skipGoogleWarning = true)
                            return@launch
                        }
                        runCatching {
                            retryBookTranslation.execute(
                                translationId = event.translationId,
                                requireWifi = _state.value.bookTranslation.requireWifi
                            )
                        }.onFailure { throwable ->
                            Log.e(
                                BOOK_TRANSLATION_LOG,
                                "Retry failed: translationId=${event.translationId}",
                                throwable
                            )
                            _state.update {
                                it.copy(
                                    bookTranslation = it.bookTranslation.copy(
                                        errorMessage = throwable.message
                                            ?: application.getString(
                                                R.string.book_translation_error_retry_failed
                                            )
                                    )
                                )
                            }
                        }
                        refreshBookTranslations(_state.value.book.id)
                    }
                }

                ReaderEvent.OnDismissBookTranslationError -> {
                    _state.update {
                        it.copy(
                            bookTranslation = it.bookTranslation.copy(errorMessage = null)
                        )
                    }
                }

                is ReaderEvent.OnSelectBookTranslation -> {
                    // Switching to an existing translation = pointing the bottom sheet at its
                    // provider/source/target triple. applyCurrentBookTranslation then resolves the
                    // matching row from the cached list and refreshBookTranslations syncs entries.
                    val target = _state.value.bookTranslation.allTranslations
                        .firstOrNull { it.id == event.translationId } ?: return@launch
                    Log.i(
                        BOOK_TRANSLATION_LOG,
                        "Select translation: translationId=${target.id} " +
                                "provider=${target.providerMode} " +
                                "source=${target.sourceLanguageCode} target=${target.targetLanguageCode}"
                    )
                    activeBookTranslationTextJob?.cancel()
                    _state.update {
                        it.copyWithOriginalBookTextIfNeeded(
                            bookTranslation = it.bookTranslation.copy(
                                providerMode = target.providerMode,
                                sourceLanguageCode = target.sourceLanguageCode
                                    ?: AUTO_TRANSLATION_LANGUAGE,
                                targetLanguageCode = target.targetLanguageCode,
                                currentTranslation = target,
                                displayMode = ReaderBookTranslationDisplayMode.ORIGINAL,
                                activeTranslationId = null,
                                isApplyingTranslation = false,
                                errorMessage = null
                            )
                        )
                    }
                    refreshBookTranslations(_state.value.book.id)
                }

                is ReaderEvent.OnDeleteBookTranslation -> {
                    val target = _state.value.bookTranslation.allTranslations
                        .firstOrNull { it.id == event.translationId } ?: return@launch
                    Log.i(
                        BOOK_TRANSLATION_LOG,
                        "Delete translation: translationId=${target.id}"
                    )
                    // If the deleted row was the active one, drop the translated overlay so the
                    // reader returns to the original text without observing a now-missing row.
                    val isActive = _state.value.bookTranslation.activeTranslationId == target.id ||
                            _state.value.bookTranslation.currentTranslation?.id == target.id
                    if (isActive) {
                        activeBookTranslationTextJob?.cancel()
                        _state.update {
                            it.copyWithOriginalBookTextIfNeeded(
                                bookTranslation = it.bookTranslation.copy(
                                    currentTranslation = null,
                                    displayMode = ReaderBookTranslationDisplayMode.ORIGINAL,
                                    activeTranslationId = null,
                                    isApplyingTranslation = false,
                                    errorMessage = null
                                ),
                                force = true
                            )
                        }
                    }
                    runCatching {
                        deleteBookTranslation.execute(target.id)
                    }.onFailure { throwable ->
                        Log.e(
                            BOOK_TRANSLATION_LOG,
                            "Delete translation failed: translationId=${target.id}",
                            throwable
                        )
                    }
                    refreshBookTranslations(_state.value.book.id)
                }

                is ReaderEvent.OnOpenShareApp -> {
                    launch {
                        val shareIntent = Intent()

                        shareIntent.action = Intent.ACTION_SEND
                        shareIntent.type = "text/plain"
                        shareIntent.putExtra(
                            Intent.EXTRA_SUBJECT,
                            event.activity.getString(R.string.app_name)
                        )
                        shareIntent.putExtra(
                            Intent.EXTRA_TEXT,
                            event.textToShare.trim()
                        )

                        yield()

                        shareIntent.launchActivity(
                            activity = event.activity,
                            createChooser = true,
                            success = {
                                return@launch
                            }
                        )

                        withContext(Dispatchers.Main) {
                            event.activity.getString(R.string.error_no_share_app)
                                .showToast(context = event.activity, longToast = false)
                        }
                    }
                }

                is ReaderEvent.OnOpenWebBrowser -> {
                    launch {
                        val browserIntent = Intent()

                        browserIntent.action = Intent.ACTION_WEB_SEARCH
                        browserIntent.putExtra(
                            SearchManager.QUERY,
                            event.textToSearch
                        )

                        yield()

                        browserIntent.launchActivity(
                            activity = event.activity,
                            success = {
                                return@launch
                            }
                        )

                        withContext(Dispatchers.Main) {
                            event.activity.getString(R.string.error_no_browser)
                                .showToast(context = event.activity, longToast = false)
                        }
                    }
                }

                is ReaderEvent.OnOpenDictionary -> {
                    launch {
                        val dictionaryIntent = Intent()
                        val browserIntent = Intent()

                        dictionaryIntent.type = "text/plain"
                        dictionaryIntent.action = Intent.ACTION_PROCESS_TEXT
                        dictionaryIntent.putExtra(
                            Intent.EXTRA_PROCESS_TEXT,
                            event.textToDefine.trim()
                        )
                        dictionaryIntent.putExtra(Intent.EXTRA_PROCESS_TEXT_READONLY, true)

                        browserIntent.action = Intent.ACTION_VIEW
                        val text = event.textToDefine.trim().replace(" ", "+")
                        browserIntent.data = "https://www.onelook.com/?w=$text".toUri()

                        yield()

                        dictionaryIntent.launchActivity(
                            activity = event.activity,
                            createChooser = true,
                            success = {
                                return@launch
                            }
                        )

                        browserIntent.launchActivity(
                            activity = event.activity,
                            success = {
                                return@launch
                            }
                        )

                        withContext(Dispatchers.Main) {
                            event.activity.getString(R.string.error_no_dictionary)
                                .showToast(context = event.activity, longToast = false)
                        }
                    }
                }

                is ReaderEvent.OnShowSettingsBottomSheet -> {
                    _state.update {
                        it.copy(
                            bottomSheet = ReaderScreen.SETTINGS_BOTTOM_SHEET,
                            drawer = null
                        )
                    }
                }

                is ReaderEvent.OnDismissBottomSheet -> {
                    _state.update {
                        it.copy(
                            bottomSheet = null
                        )
                    }
                }

                is ReaderEvent.OnShowChaptersDrawer -> {
                    _state.update {
                        it.copy(
                            drawer = ReaderScreen.CHAPTERS_DRAWER,
                            bottomSheet = null
                        )
                    }
                }

                is ReaderEvent.OnDismissDrawer -> {
                    _state.update {
                        it.copy(
                            drawer = null
                        )
                    }
                }

                is ReaderEvent.OnShowBookmarksDrawer -> {
                    _state.update {
                        it.copy(
                            drawer = ReaderScreen.BOOKMARKS_DRAWER,
                            bottomSheet = null
                        )
                    }
                }

                is ReaderEvent.OnDeleteBookmark -> {
                    launch(Dispatchers.IO) {
                        deleteBookmark.execute(event.id)
                    }
                }

                is ReaderEvent.OnScrollToBookmark -> {
                    val bookmark = event.bookmark
                    val state = _state.value
                    if (state.text.isEmpty()) return@launch
                    val target = bookmark.paragraphIndex.coerceIn(0, state.text.lastIndex)
                    _state.update {
                        it.copy(
                            pendingBookmarkNavigation = bookmark,
                            pendingBookmarkDisplayIndex = textIndexToDisplayIndex(target)
                        )
                    }
                }

                is ReaderEvent.OnBookmarkScrollFinished -> {
                    val bookmark = event.bookmark
                    val state = _state.value
                    val target = bookmark.paragraphIndex.coerceIn(0, state.text.lastIndex)
                    updateChapter(index = target)
                    onEvent(
                        ReaderEvent.OnChangeProgress(
                            progress = calculateProgress(target),
                            firstVisibleItemIndex = target,
                            firstVisibleItemOffset = 0
                        )
                    )
                    _state.update {
                        it.copy(
                            pendingBookmarkNavigation = null,
                            pendingBookmarkDisplayIndex = null,
                            focusedBookmarkId = bookmark.id
                        )
                    }
                    launch {
                        delay(3_000)
                        _state.update {
                            if (it.focusedBookmarkId == bookmark.id) {
                                it.copy(focusedBookmarkId = null)
                            } else it
                        }
                    }
                }

                is ReaderEvent.OnRequestAnnotationEditor -> {
                    _state.update {
                        it.copy(
                            pendingAnnotationText = event.selectedText,
                            pendingAnnotationColorArgb = event.initialColorArgb,
                            highlightPaletteText = null,
                            highlightPaletteAnnotation = null,
                            editingAnnotation = null,
                            drawer = null,
                            bottomSheet = null
                        )
                    }
                }

                is ReaderEvent.OnCreateBookmark -> {
                    launch(Dispatchers.IO) {
                        saveSelectedAnnotation(event.selectedText, note = null, colorArgb = null)
                    }
                }

                is ReaderEvent.OnShowHighlightPalette -> {
                    _state.update {
                        it.copy(
                            highlightPaletteText = event.selectedText,
                            highlightPaletteAnnotation = event.annotation,
                            highlightPaletteAnchorX = event.anchorX,
                            highlightPaletteAnchorY = event.anchorY,
                            pendingAnnotationText = null,
                            pendingAnnotationColorArgb = null,
                            editingAnnotation = null
                        )
                    }
                }

                is ReaderEvent.OnCreateHighlight -> {
                    _state.update {
                        it.copy(
                            highlightPaletteText = null,
                            highlightPaletteAnnotation = null
                        )
                    }
                    launch(Dispatchers.IO) {
                        saveSelectedAnnotation(
                            text = event.selectedText,
                            note = null,
                            colorArgb = event.colorArgb
                        )
                    }
                }

                is ReaderEvent.OnDismissHighlightPalette -> {
                    _state.update {
                        it.copy(
                            highlightPaletteText = null,
                            highlightPaletteAnnotation = null
                        )
                    }
                }

                is ReaderEvent.OnShowHighlightPaletteEditor -> {
                    _state.update {
                        it.copy(
                            showHighlightPaletteEditor = true,
                            highlightPaletteEditorTarget = event.selectedText?.let { selectedText ->
                                HighlightPaletteTarget(
                                    selectedText = selectedText,
                                    annotation = event.annotation
                                )
                            },
                            highlightPaletteText = null,
                            highlightPaletteAnnotation = null
                        )
                    }
                }

                ReaderEvent.OnDismissHighlightPaletteEditor -> {
                    _state.update {
                        it.copy(
                            showHighlightPaletteEditor = false,
                            highlightPaletteEditorTarget = null
                        )
                    }
                }

                is ReaderEvent.OnUpdateHighlightPalette -> {
                    val colors = HighlightPalette.normalize(event.colors)
                    _state.update { it.copy(highlightColors = colors) }
                    launch(Dispatchers.IO) {
                        setDatastore.execute(
                            DataStoreConstants.READER_HIGHLIGHT_PALETTE,
                            HighlightPalette.encode(colors)
                        )
                    }
                }

                is ReaderEvent.OnApplyHighlightPaletteColor -> {
                    val state = _state.value
                    val target = state.highlightPaletteEditorTarget
                    val annotation = target?.annotation
                    if (annotation != null) {
                        launch(Dispatchers.IO) {
                            val existing = _state.value.bookmarks
                                .firstOrNull { it.id == annotation.id } ?: return@launch
                            upsertBookmark.execute(
                                existing.copy(
                                    kind = BookmarkKind.HIGHLIGHT,
                                    colorArgb = event.colorArgb,
                                    updatedAt = System.currentTimeMillis()
                                )
                            )
                        }
                    } else if (target != null) {
                        launch(Dispatchers.IO) {
                            saveSelectedAnnotation(
                                text = target.selectedText,
                                note = null,
                                colorArgb = event.colorArgb
                            )
                        }
                    }
                }

                is ReaderEvent.OnEditAnnotation -> {
                    _state.update {
                        it.copy(
                            editingAnnotation = event.bookmark,
                            pendingAnnotationText = null,
                            pendingAnnotationColorArgb = null,
                            highlightPaletteText = null,
                            highlightPaletteAnnotation = null,
                            drawer = null,
                            bottomSheet = null
                        )
                    }
                }

                is ReaderEvent.OnSaveAnnotation -> {
                    val state = _state.value
                    val editing = state.editingAnnotation
                    val selectedText = state.pendingAnnotationText
                    val note = event.note.trim().ifBlank { null }
                    val kind = if (event.colorArgb == null) {
                        BookmarkKind.BOOKMARK
                    } else {
                        BookmarkKind.HIGHLIGHT
                    }
                    _state.update {
                        it.copy(
                            editingAnnotation = null,
                            pendingAnnotationText = null,
                            pendingAnnotationColorArgb = null
                        )
                    }

                    launch(Dispatchers.IO) {
                        val now = System.currentTimeMillis()
                        if (editing != null) {
                            upsertBookmark.execute(
                                editing.copy(
                                    kind = kind,
                                    note = note,
                                    colorArgb = event.colorArgb,
                                    updatedAt = now
                                )
                            )
                            return@launch
                        }

                        saveSelectedAnnotation(text = selectedText ?: return@launch, note, event.colorArgb)
                    }
                }

                is ReaderEvent.OnDismissAnnotationEditor -> {
                    _state.update {
                        it.copy(
                            editingAnnotation = null,
                            pendingAnnotationText = null,
                            pendingAnnotationColorArgb = null,
                            highlightPaletteText = null,
                            highlightPaletteAnnotation = null
                        )
                    }
                }

                is ReaderEvent.OnChangeHighlightColor -> {
                    launch(Dispatchers.IO) {
                        val existing = _state.value.bookmarks
                            .firstOrNull { it.id == event.id } ?: return@launch
                        if (existing.colorArgb == event.colorArgb) return@launch
                        upsertBookmark.execute(
                            existing.copy(
                                kind = BookmarkKind.HIGHLIGHT,
                                colorArgb = event.colorArgb,
                                updatedAt = System.currentTimeMillis()
                            )
                        )
                    }
                }

                is ReaderEvent.OnClearHighlightColor -> {
                    launch(Dispatchers.IO) {
                        val existing = _state.value.bookmarks
                            .firstOrNull { it.id == event.id } ?: return@launch
                        upsertBookmark.execute(
                            existing.copy(
                                kind = BookmarkKind.BOOKMARK,
                                colorArgb = null,
                                updatedAt = System.currentTimeMillis()
                            )
                        )
                    }
                }

                is ReaderEvent.OnSetAutoScrolling -> {
                    _state.update {
                        it.copy(
                            isAutoScrolling = event.active,
                            isAutoScrollPaused = false
                        )
                    }
                }

                is ReaderEvent.OnSetAutoScrollPaused -> {
                    _state.update {
                        it.copy(isAutoScrollPaused = event.paused)
                    }
                }

                is ReaderEvent.OnStartTts -> {
                    startTts(event.preferences)
                }

                is ReaderEvent.OnStopTts -> {
                    stopTts()
                }

                is ReaderEvent.OnToggleTtsPlayback -> {
                    ttsSessionManager.togglePlayPause()
                }

                is ReaderEvent.OnTtsNextParagraph -> {
                    ttsSessionManager.nextParagraph()
                }

                is ReaderEvent.OnTtsPreviousParagraph -> {
                    ttsSessionManager.previousParagraph()
                }

                is ReaderEvent.OnTtsNextSentence -> {
                    ttsSessionManager.nextSentence()
                }

                is ReaderEvent.OnTtsPreviousSentence -> {
                    ttsSessionManager.previousSentence()
                }

                is ReaderEvent.OnApplyTtsPreferences -> {
                    ttsSessionManager.applyPreferences(event.preferences)
                }
            }
        }
    }

    fun init(
        bookId: Int,
        fullscreenMode: Boolean,
        activity: ComponentActivity,
        navigateBack: () -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentState = _state.value
            if (currentState.book.id == bookId) {
                val isPdf = currentState.book.filePath.endsWith(".pdf", ignoreCase = true)

                if (isPdf && currentState.book.pdfReadingMode == PdfReadingMode.ORIGINAL_PDF) {
                    // Native PDF — PdfReaderModel renders it, nothing to parse here.
                    if (currentState.isLoading) {
                        _state.update { it.copy(isLoading = false, showMenu = false) }
                    }
                    systemBarsVisibility(show = !fullscreenMode, activity = activity)
                    return@launch
                }

                val hasContent = currentState.isParsing ||
                        currentState.text.isNotEmpty() ||
                        currentState.originalText.isNotEmpty()
                if (hasContent) {
                    // Parsed text already in memory (incl. mid-parse) — reuse it and clear any
                    // loader left over from a mode switch.
                    if (currentState.isLoading) {
                        _state.update { it.copy(isLoading = false) }
                    }
                    systemBarsVisibility(show = !fullscreenMode, activity = activity)
                    return@launch
                }

                if (loadJob?.isActive == true) {
                    // A load is already running for this book.
                    return@launch
                }

                // Parsed text wanted (PDF returning to PARSED_TEXT, or a non-PDF book) but none
                // loaded yet — load it. getBookText is cache-backed, so a prior parse is reused.
                onEvent(
                    ReaderEvent.OnLoadText(
                        activity = activity,
                        fullscreenMode = fullscreenMode
                    )
                )
                return@launch
            }

            val loadedBook = getBookById.execute(bookId)

            if (loadedBook == null) {
                navigateBack()
                return@launch
            }

            // Auto-status: opening the reader moves the book into "Reading" (custom categories and
            // already-finished books are left untouched — see CategoryDefaults.computeAutoStatus).
            val openStatus = CategoryDefaults.computeAutoStatus(
                current = loadedBook.categoryId,
                progress = loadedBook.progress,
                isOpening = true
            )
            val book = if (openStatus != loadedBook.categoryId) {
                loadedBook.copy(categoryId = openStatus).also {
                    updateBook.execute(it)
                    LibraryScreen.refreshListChannel.trySend(0)
                }
            } else {
                loadedBook
            }

            eventJob.cancel()
            resetJob?.cancel()
            eventJob.join()
            resetJob?.join()
            eventJob = newEventJob()

            _state.update {
                ReaderState(
                    book = book,
                    bookTranslation = ReaderBookTranslationState(
                        isBookTextReadyForTranslation = false
                    )
                )
            }

            if (
                book.filePath.endsWith(".pdf", ignoreCase = true) &&
                book.pdfReadingMode == PdfReadingMode.ORIGINAL_PDF
            ) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        showMenu = false,
                        bookTranslation = it.bookTranslation.withBookTextReadiness(
                            isReady = false
                        )
                    )
                }
                systemBarsVisibility(
                    show = !fullscreenMode,
                    activity = activity
                )
                return@launch
            }

            onEvent(
                ReaderEvent.OnLoadText(
                    activity = activity,
                    fullscreenMode = fullscreenMode
                )
            )
        }
    }

    /**
     * Applies the reading-progress-driven auto status to the in-state book right before it is
     * persisted (so the existing [updateBook] save carries it). At the moment, this promotes a book to
     * "Already read" once it is finished. Must be the only place that mutates categoryId during
     * reading — otherwise [updateBook] would rewrite the status back from stale state.
     */
    private suspend fun applyAutoStatusOnSave() {
        val book = _state.value.book
        val newStatus = CategoryDefaults.computeAutoStatus(
            current = book.categoryId,
            progress = book.progress,
            isOpening = false
        )
        if (newStatus != book.categoryId) {
            _state.update { it.copy(book = it.book.copy(categoryId = newStatus)) }
        }
    }

    @OptIn(FlowPreview::class)
    fun updateProgress(
        listState: LazyListState,
        displayIndexToTextIndex: (Int) -> Int = { it },
        textIndexToDisplayIndex: (Int) -> Int = { it }
    ) {
        this.displayIndexToTextIndex = displayIndexToTextIndex
        this.textIndexToDisplayIndex = textIndexToDisplayIndex
        progressJob?.cancel()
        progressJob = viewModelScope.launch(Dispatchers.Main) {
            snapshotFlow {
                listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset
            }.distinctUntilChanged().debounce(300.milliseconds).collectLatest { (displayIndex, offset) ->
                sessionLastActiveTime = System.currentTimeMillis()
                val index = displayIndexToTextIndex(displayIndex)
                val progress = calculateProgress(index)
                if (progress == _state.value.book.progress) return@collectLatest
                val (currentChapter, currentChapterProgress) = calculateCurrentChapter(index)

                Log.i(
                    READER,
                    "Changed progress|currentChapter: $progress; ${currentChapter?.title}"
                )
                _state.update {
                    it.copy(
                        book = it.book.copy(
                            progress = progress,
                            scrollIndex = index,
                            scrollOffset = offset
                        ),
                        currentChapter = currentChapter,
                        currentChapterProgress = currentChapterProgress
                    )
                }

                applyAutoStatusOnSave()
                updateBook.execute(_state.value.book)

                // No library/history ping here. This runs on every debounced scroll settle while
                // reading; pinging fired an immediate background getBooksFromDatabase reload each
                // time, competing with this very save. Both screens are off-screen during reading
                // and OnLeave refreshes them on exit with the final progress/auto-status.
            }
        }
    }

    fun findChapterIndexAndLength(index: Int): Pair<Int, Int> {
        val state = _state.value
        val chapterPosition = state.findChapterPosition(index)
        if (chapterPosition == -1) return -1 to -1

        val startIndex = state.chapterIndexes[chapterPosition]
        val endIndex = state.chapterIndexes.getOrNull(chapterPosition + 1) ?: state.text.size
        val chapterLength = (endIndex - (startIndex + 1)).coerceAtLeast(1)
        val currentIndexInChapter = (index - startIndex).coerceIn(1, chapterLength)

        return currentIndexInChapter to chapterLength
    }

    private fun updateChapter(index: Int) {
        viewModelScope.launch {
            val (currentChapter, currentChapterProgress) = calculateCurrentChapter(index)
            _state.update {
                Log.i(
                    READER,
                    "Changed currentChapter|currentChapterProgress:" +
                            " ${currentChapter?.title}($currentChapterProgress)"
                )
                it.copy(
                    currentChapter = currentChapter,
                    currentChapterProgress = currentChapterProgress
                )
            }
        }
    }

    private fun calculateCurrentChapter(index: Int): Pair<Chapter?, Float> {
        val state = _state.value
        val chapterPosition = state.findChapterPosition(index)
        if (chapterPosition == -1) return null to 0f

        val startIndex = state.chapterIndexes[chapterPosition]
        val endIndex = state.chapterIndexes.getOrNull(chapterPosition + 1) ?: state.text.size
        val chapterLength = (endIndex - (startIndex + 1)).coerceAtLeast(1)
        val currentIndexInChapter = (index - startIndex).coerceIn(1, chapterLength)
        val progress = (currentIndexInChapter / chapterLength.toFloat())
            .coerceAndPreventNaN()

        return state.chapters.getOrNull(chapterPosition) to progress
    }

    private fun ReaderState.findChapterPosition(index: Int): Int {
        if (chapterIndexes.isEmpty()) return -1
        val exactIndex = chapterIndexes.binarySearch(index)
        val chapterPosition = if (exactIndex >= 0) exactIndex else -exactIndex - 2
        return chapterPosition.takeIf { it in chapterIndexes.indices } ?: -1
    }

    private fun calculateProgress(firstVisibleItemIndex: Int? = null): Float {
        return _state.value.run {
            if (
                isLoading ||
                isParsing ||
                listState.layoutInfo.totalItemsCount == 0 ||
                text.isEmpty() ||
                errorMessage != null
            ) {
                return book.progress
            }

            val index = firstVisibleItemIndex
                ?: displayIndexToTextIndex(listState.firstVisibleItemIndex)

            if (index == 0) {
                return 0f
            }

            val lastVisibleItemIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
                ?: return book.progress
            if (displayIndexToTextIndex(lastVisibleItemIndex) >= text.lastIndex) {
                return 1f
            }

            return@run index
                .div(text.lastIndex.toFloat())
                .coerceAndPreventNaN()
        }
    }

    private suspend fun systemBarsVisibility(
        show: Boolean,
        activity: ComponentActivity
    ) {
        withContext(Dispatchers.Main) {
            WindowCompat.getInsetsController(
                activity.window,
                activity.window.decorView
            ).apply {
                systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                if (show) show(WindowInsetsCompat.Type.systemBars())
                else hide(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    fun resetScreen() {
        resetJob = viewModelScope.launch(Dispatchers.Main) {
            eventJob.cancel()
            progressJob?.cancel()
            loadJob?.cancel()
            bookTranslationsObserveJob?.cancel()
            activeBookTranslationTextJob?.cancel()
            bookmarksObserveJob?.cancel()
            translationCache.clear()
            eventJob = newEventJob()

            yield()
            _state.update { ReaderState() }
        }
    }

    private suspend inline fun <T> MutableStateFlow<T>.update(function: (T) -> T) {
        mutex.withLock {
            yield()
            this.value = function(this.value)
        }
    }

    private suspend fun showTranslatedBook(
        translationOverride: BookTranslation? = null,
        allowPartial: Boolean = false
    ) {
        val state = _state.value
        val translation = translationOverride?.takeIf { allowPartial || it.canRead }
            ?: state.bookTranslation.readableTranslation
            ?: state.bookTranslation.runningTranslation?.takeIf {
                allowPartial
            }
        if (translation == null) {
            Log.w(
                BOOK_TRANSLATION_LOG,
                "Show translated book ignored: no readable translation bookId=${state.book.id}"
            )
            _state.update {
                it.copy(
                    bookTranslation = it.bookTranslation.copy(
                        errorMessage = application.getString(
                            R.string.book_translation_error_no_text_available
                        )
                    )
                )
            }
            return
        }

        if (
            state.bookTranslation.displayMode == ReaderBookTranslationDisplayMode.TRANSLATED &&
            state.bookTranslation.activeTranslationId == translation.id
        ) {
            Log.i(
                BOOK_TRANSLATION_LOG,
                "Show translated book ignored: already visible translationId=${translation.id}"
            )
            return
        }

        val originalText = state.originalText.takeIf { it.isNotEmpty() } ?: state.text
        if (originalText.isEmpty()) {
            Log.w(
                BOOK_TRANSLATION_LOG,
                "Show translated book ignored: original text is empty bookId=${state.book.id}"
            )
            _state.update {
                it.copy(
                    bookTranslation = it.bookTranslation.copy(
                        errorMessage = application.getString(
                            R.string.book_translation_error_no_parsed_text
                        )
                    )
                )
            }
            return
        }

        val anchor = currentVisibleReaderTextAnchor()
        Log.i(
            BOOK_TRANSLATION_LOG,
            "Show translated book requested: translationId=${translation.id} " +
                    "bookId=${translation.bookId} textItems=${originalText.size} " +
                    "status=${translation.status} completed=${translation.completedUnits}/${translation.totalUnits}"
        )
        _state.update {
            it.copy(
                bookTranslation = it.bookTranslation.copy(
                    displayMode = ReaderBookTranslationDisplayMode.TRANSLATED,
                    activeTranslationId = translation.id,
                    isApplyingTranslation = translation.canRead,
                    errorMessage = null
                )
            )
        }
        observeActiveBookTranslationText(
            translationId = translation.id,
            originalText = originalText
        )
        restoreReaderTextAnchor(anchor, originalText.lastIndex)
    }

    @OptIn(FlowPreview::class)
    private fun observeActiveBookTranslationText(
        translationId: Long,
        originalText: List<ReaderText>
    ) {
        activeBookTranslationTextJob?.cancel()
        activeBookTranslationTextJob = CoroutineScope(eventJob + Dispatchers.IO).launch {
            observeTranslatedBookText.execute(
                translationId = translationId,
                originalText = originalText
            )
                .debounce(150.milliseconds)
                .collectLatest { snapshot ->
                    val currentState = _state.value
                    if (
                        currentState.bookTranslation.displayMode !=
                        ReaderBookTranslationDisplayMode.TRANSLATED ||
                        currentState.bookTranslation.activeTranslationId != translationId
                    ) {
                        return@collectLatest
                    }

                    val anchor = currentVisibleReaderTextAnchor()
                    val translatedText = snapshot.text
                    val (chapters, chapterIndexes) = translatedText.chapterData()
                    _state.update { state ->
                        if (
                            state.bookTranslation.displayMode !=
                            ReaderBookTranslationDisplayMode.TRANSLATED ||
                            state.bookTranslation.activeTranslationId != translationId
                        ) {
                            return@update state
                        }

                        state.copy(
                            text = translatedText,
                            chapters = chapters,
                            chapterIndexes = chapterIndexes,
                            bookTranslation = state.bookTranslation.copy(
                                isApplyingTranslation = false,
                                errorMessage = null
                            )
                        )
                    }
                    restoreReaderTextAnchor(anchor, translatedText.lastIndex)

                    if (Log.isLoggable(BOOK_TRANSLATION_LOG, Log.VERBOSE)) {
                        Log.v(
                            BOOK_TRANSLATION_LOG,
                            "Live translated text applied: translationId=$translationId " +
                                    "entries=${snapshot.translatedEntries} items=${translatedText.size}"
                        )
                    }
                }
        }
    }

    private suspend fun showOriginalBook() {
        val state = _state.value
        val activeTranslationId = state.bookTranslation.activeTranslationId
        val anchor = currentVisibleReaderTextAnchor()
        activeBookTranslationTextJob?.cancel()
        Log.i(
            BOOK_TRANSLATION_LOG,
            "Show original book requested: bookId=${state.book.id} " +
                    "activeTranslationId=$activeTranslationId"
        )
        _state.update {
            it.copyWithOriginalBookTextIfNeeded(
                bookTranslation = it.bookTranslation.copy(
                    displayMode = ReaderBookTranslationDisplayMode.ORIGINAL,
                    activeTranslationId = null,
                    isApplyingTranslation = false,
                    errorMessage = null
                ),
                force = true
            )
        }
        restoreReaderTextAnchor(anchor, _state.value.text.lastIndex)
    }

    private suspend fun currentVisibleReaderTextAnchor(): ReaderTextScrollAnchor =
        withContext(Dispatchers.Main) {
            val state = _state.value
            ReaderTextScrollAnchor(
                textIndex = displayIndexToTextIndex(state.listState.firstVisibleItemIndex)
                    .coerceAtLeast(0),
                offset = state.listState.firstVisibleItemScrollOffset
            )
        }

    private suspend fun restoreReaderTextAnchor(
        anchor: ReaderTextScrollAnchor,
        textLastIndex: Int
    ) {
        if (textLastIndex < 0) return

        val textIndex = anchor.textIndex.coerceIn(0, textLastIndex)
        withContext(Dispatchers.Main) {
            _state.value.listState.requestScrollToItem(
                textIndexToDisplayIndex(textIndex),
                anchor.offset
            )
        }
        updateChapter(textIndex)
    }

    private fun ReaderState.copyWithOriginalBookTextIfNeeded(
        bookTranslation: ReaderBookTranslationState,
        force: Boolean = false
    ): ReaderState {
        if (!force && !this.bookTranslation.isTranslatedBookVisible) {
            return copy(bookTranslation = bookTranslation)
        }

        val original = originalText.takeIf { it.isNotEmpty() } ?: text
        val (chapters, chapterIndexes) = original.chapterData()
        return copy(
            text = original,
            chapters = chapters,
            chapterIndexes = chapterIndexes,
            bookTranslation = bookTranslation
        )
    }

    private fun List<ReaderText>.chapterData(): Pair<List<Chapter>, List<Int>> {
        val chapterIndexes = mutableListOf<Int>()
        val chapters = mutableListOf<Chapter>()
        forEachIndexed { index, readerText ->
            if (readerText is Chapter) {
                chapterIndexes += index
                chapters += readerText
            }
        }
        return chapters to chapterIndexes
    }

    private suspend fun refreshBookTranslations(bookId: Int) {
        if (bookId == -1) return

        val state = _state.value
        val originalText = state.stableOriginalTextOrEmpty()
        if (Log.isLoggable(BOOK_TRANSLATION_LOG, Log.VERBOSE)) {
            Log.v(
                BOOK_TRANSLATION_LOG,
                "Refreshing translations: bookId=$bookId textItems=${originalText.size} " +
                        "loading=${state.isLoading} parsing=${state.isParsing}"
            )
        }
        val translations = getBookTranslations.execute(
            bookId = bookId,
            originalText = originalText
        )

        applyCurrentBookTranslation(translations)
    }

    private fun observeBookTranslations(bookId: Int) {
        if (bookId == -1) return

        if (Log.isLoggable(BOOK_TRANSLATION_LOG, Log.VERBOSE)) {
            Log.v(BOOK_TRANSLATION_LOG, "Observing translations: bookId=$bookId")
        }
        bookTranslationsObserveJob?.cancel()
        bookTranslationsObserveJob = CoroutineScope(eventJob + Dispatchers.IO).launch {
            val state = _state.value
            val originalText = state.stableOriginalTextOrEmpty()
            observeBookTranslations.execute(
                bookId = bookId,
                originalText = originalText
            ).collectLatest { translations ->
                if (Log.isLoggable(BOOK_TRANSLATION_LOG, Log.VERBOSE)) {
                    Log.v(
                        BOOK_TRANSLATION_LOG,
                        "Translations updated: bookId=$bookId count=${translations.size} " +
                                "statuses=${translations.joinToString { "${it.id}:${it.status}" }}"
                    )
                }
                applyCurrentBookTranslation(translations)
            }
        }
    }

    private fun observeBookmarks(bookId: Int) {
        if (bookId == -1) return

        bookmarksObserveJob?.cancel()
        bookmarksObserveJob = CoroutineScope(eventJob + Dispatchers.IO).launch {
            observeBookmarks.execute(bookId).collectLatest { bookmarks ->
                _state.update { it.copy(bookmarks = bookmarks) }
            }
        }
    }

    private suspend fun resolveSelectionAnchorOrToast(selectedRaw: String): SelectionAnchor? {
        val result = resolveSelectionAnchor(selectedRaw)
        val failure = result.failure
        if (failure != null) {
            withContext(Dispatchers.Main) {
                application.getString(failure.messageResId())
                    .showToast(context = application, longToast = false)
            }
        }
        return result.anchor
    }

    private suspend fun resolveSelectionAnchor(selectedRaw: String): SelectionAnchorResult =
        withContext(Dispatchers.Main) {
            val state = _state.value
            if (state.bookTranslation.isTranslatedBookVisible) {
                return@withContext SelectionAnchorResult(
                    failure = SelectionAnchorFailure.TRANSLATED_TEXT
                )
            }

            val text = state.originalText.takeIf { it.isNotEmpty() } ?: state.text
            if (text.isEmpty()) {
                return@withContext SelectionAnchorResult(failure = SelectionAnchorFailure.NOT_FOUND)
            }

            val selected = selectedRaw.trim()
            if (selected.isEmpty()) {
                return@withContext SelectionAnchorResult(failure = SelectionAnchorFailure.EMPTY)
            }

            val segments = selected.lineSequence()
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .toList()
            if (segments.size != 1) {
                return@withContext SelectionAnchorResult(
                    failure = SelectionAnchorFailure.MULTI_PARAGRAPH
                )
            }

            val visible = state.listState.layoutInfo.visibleItemsInfo
            val firstDisplay = visible.firstOrNull()?.index
                ?: return@withContext SelectionAnchorResult(
                    failure = SelectionAnchorFailure.NOT_FOUND
                )
            val lastDisplay = visible.lastOrNull()?.index ?: firstDisplay
            val from = displayIndexToTextIndex(firstDisplay).coerceIn(0, text.lastIndex)
            val to = (displayIndexToTextIndex(lastDisplay) + HIGHLIGHT_SEARCH_LOOKAHEAD)
                .coerceIn(from, text.lastIndex)

            val segment = segments.single()
            val matches = mutableListOf<SelectionAnchor>()
            for (paragraphIndex in from..to) {
                val source = (text.getOrNull(paragraphIndex) as? ReaderText.Text)?.source
                    ?: continue
                source.forEachIndexOf(segment) { start ->
                    val end = start + segment.length
                    matches += SelectionAnchor(
                        paragraphIndex = paragraphIndex,
                        charStart = start,
                        charEnd = end,
                        quoted = segment,
                        paragraphHash = source.hashCode(),
                        prefix = source.substring(
                            (start - ANCHOR_CONTEXT_CHARS).coerceAtLeast(0),
                            start
                        ),
                        suffix = source.substring(
                            end,
                            (end + ANCHOR_CONTEXT_CHARS).coerceAtMost(source.length)
                        )
                    )
                }

            }

            when (matches.size) {
                0 -> SelectionAnchorResult(failure = SelectionAnchorFailure.NOT_FOUND)
                1 -> SelectionAnchorResult(anchor = matches.single())
                else -> SelectionAnchorResult(failure = SelectionAnchorFailure.AMBIGUOUS)
            }
        }

    private fun String.forEachIndexOf(needle: String, action: (Int) -> Unit) {
        var index = indexOf(needle)
        while (index >= 0) {
            action(index)
            index = indexOf(needle, startIndex = index + 1)
        }
    }

    private fun SelectionAnchorFailure.messageResId(): Int = when (this) {
        SelectionAnchorFailure.EMPTY,
        SelectionAnchorFailure.NOT_FOUND -> R.string.highlight_not_found

        SelectionAnchorFailure.MULTI_PARAGRAPH -> R.string.highlight_multi_paragraph_not_supported
        SelectionAnchorFailure.AMBIGUOUS -> R.string.highlight_ambiguous_selection
        SelectionAnchorFailure.TRANSLATED_TEXT -> R.string.reader_annotations_original_only
    }

    private fun ReaderState.createAnnotation(
        kind: BookmarkKind,
        anchor: SelectionAnchor,
        now: Long,
        note: String? = null,
        colorArgb: Int? = null
    ): Bookmark {
        val chapterPosition = findChapterPosition(anchor.paragraphIndex)
        val chapterHeadingIndex = chapterIndexes.getOrNull(chapterPosition)
            ?: anchor.paragraphIndex
        val chapterTitle = chapters.getOrNull(chapterPosition)?.title.orEmpty()
        val quoteLimit = if (kind == BookmarkKind.BOOKMARK) {
            BOOKMARK_SNIPPET_MAX_CHARS
        } else {
            HIGHLIGHT_QUOTE_MAX_CHARS
        }
        return Bookmark(
            bookId = book.id,
            kind = kind,
            chapterIndex = chapterHeadingIndex,
            chapterTitle = chapterTitle,
            paragraphIndex = anchor.paragraphIndex,
            charStart = anchor.charStart,
            charEnd = anchor.charEnd,
            quotedText = anchor.quoted.take(quoteLimit),
            paragraphHash = anchor.paragraphHash,
            prefix = anchor.prefix,
            suffix = anchor.suffix,
            note = note,
            colorArgb = colorArgb,
            progress = calculateProgress(anchor.paragraphIndex),
            createdAt = now,
            updatedAt = now
        )
    }

    private suspend fun saveSelectedAnnotation(
        text: String,
        note: String?,
        colorArgb: Int?
    ) {
        val anchor = resolveSelectionAnchorOrToast(text) ?: return
        val latest = _state.value
        if (latest.book.id == -1) return
        val now = System.currentTimeMillis()
        val kind = if (colorArgb == null) BookmarkKind.BOOKMARK else BookmarkKind.HIGHLIGHT
        val existing = latest.bookmarks.firstOrNull {
            it.paragraphIndex == anchor.paragraphIndex &&
                    it.charStart == anchor.charStart &&
                    it.charEnd == anchor.charEnd
        }
        val annotation = existing?.copy(
            kind = kind,
            note = note,
            colorArgb = colorArgb,
            updatedAt = now
        ) ?: latest.createAnnotation(
            kind = kind,
            anchor = anchor,
            now = now,
            note = note,
            colorArgb = colorArgb
        )
        upsertBookmark.execute(annotation)
    }

    private fun ReaderState.stableOriginalTextOrEmpty(): List<ReaderText> {
        if (isLoading || isParsing) return emptyList()
        return originalText.takeIf { it.isNotEmpty() } ?: text
    }

    private fun ReaderState.isBookTextReadyForTranslation(): Boolean =
        book.id != -1 &&
                !isLoading &&
                !isParsing &&
                (originalText.isNotEmpty() || text.isNotEmpty())

    private fun ReaderBookTranslationState.withBookTextReadiness(
        isReady: Boolean
    ): ReaderBookTranslationState =
        copy(
            isBookTextReadyForTranslation = isReady,
            errorMessage = if (isReady) errorMessage else null
        )

    private suspend fun applyCurrentBookTranslation(translations: List<BookTranslation>) {
        _state.update { state ->
            val providerMode = state.bookTranslation.providerMode
            val sourceLanguageCode = normalizeTranslationLanguageCode(
                state.bookTranslation.sourceLanguageCode
            )?.takeIf { state.bookTranslation.sourceLanguageCode != AUTO_TRANSLATION_LANGUAGE }
            val targetLanguageCode = resolveTranslationLanguageCode(
                state.bookTranslation.targetLanguageCode
            ) ?: FALLBACK_TRANSLATION_TARGET_LANGUAGE
            val matchingTranslations = translations.filter { translation ->
                translation.providerMode == providerMode &&
                        translation.sourceLanguageCode == sourceLanguageCode &&
                        translation.targetLanguageCode == targetLanguageCode
            }
            val currentTranslation = matchingTranslations.firstOrNull {
                it.status != BookTranslationStatus.STALE
            } ?: matchingTranslations.firstOrNull()
            val activeTranslationId = state.bookTranslation.activeTranslationId
            val activeTranslationStillMatches = activeTranslationId == null ||
                    currentTranslation?.id == activeTranslationId
            if (!activeTranslationStillMatches) {
                activeBookTranslationTextJob?.cancel()
                return@update state.copyWithOriginalBookTextIfNeeded(
                    bookTranslation = state.bookTranslation.copy(
                        currentTranslation = currentTranslation,
                        allTranslations = translations,
                        displayMode = ReaderBookTranslationDisplayMode.ORIGINAL,
                        activeTranslationId = null,
                        isApplyingTranslation = false,
                        errorMessage = state.bookTranslation.errorMessage
                    ),
                    force = true
                )
            }
            state.copy(
                bookTranslation = state.bookTranslation.copy(
                    currentTranslation = currentTranslation,
                    allTranslations = translations,
                    errorMessage = state.bookTranslation.errorMessage
                )
            )
        }
    }

    private suspend fun rememberBookTranslationLanguages(
        sourceLanguageCode: String,
        targetLanguageCode: String
    ) {
        val sourceLanguages = (
                listOf(sourceLanguageCode) +
                        (
                                getDatastore.execute(
                                    DataStoreConstants.BOOK_TRANSLATION_RECENT_SOURCE_LANGUAGES
                                ) ?: emptySet()
                                )
                )
            .distinct()
            .take(5)
            .toSet()
        val targetLanguages = (
                listOf(targetLanguageCode) +
                        (
                                getDatastore.execute(
                                    DataStoreConstants.BOOK_TRANSLATION_RECENT_TARGET_LANGUAGES
                                ) ?: emptySet()
                                )
                )
            .distinct()
            .take(5)
            .toSet()

        setDatastore.execute(
            DataStoreConstants.BOOK_TRANSLATION_RECENT_SOURCE_LANGUAGES,
            sourceLanguages
        )
        setDatastore.execute(
            DataStoreConstants.BOOK_TRANSLATION_RECENT_TARGET_LANGUAGES,
            targetLanguages
        )
        _state.update {
            it.copy(
                bookTranslation = it.bookTranslation.copy(
                    recentSourceLanguageCodes = sourceLanguages.toList(),
                    recentTargetLanguageCodes = targetLanguages.toList()
                )
            )
        }
    }

    private fun enqueueBookTranslation(skipGoogleWarning: Boolean = false) {
        CoroutineScope(eventJob + Dispatchers.IO).launch {
            val pendingState = _state.value
            if (!pendingState.isBookTextReadyForTranslation()) {
                Log.i(
                    BOOK_TRANSLATION_LOG,
                    "Enqueue ignored: book text is not ready " +
                            "bookId=${pendingState.book.id} " +
                            "textItems=${pendingState.originalText.size} " +
                            "loading=${pendingState.isLoading} parsing=${pendingState.isParsing}"
                )
                _state.update {
                    it.copy(
                        bookTranslation = it.bookTranslation.withBookTextReadiness(
                            isReady = false
                        ).copy(
                            isStarting = false,
                            showGoogleWarning = false,
                            errorMessage = null
                        )
                    )
                }
                return@launch
            }

            _state.update {
                it.copy(
                    bookTranslation = it.bookTranslation.copy(
                        isStarting = true,
                        showGoogleWarning = false,
                        errorMessage = null
                    )
                )
            }

            val state = _state.value

            val originalText = state.originalText.takeIf { it.isNotEmpty() } ?: state.text
            Log.i(
                BOOK_TRANSLATION_LOG,
                "Preparing enqueue: bookId=${state.book.id} textItems=${originalText.size} " +
                        "provider=${state.bookTranslation.providerMode} " +
                        "source=${state.bookTranslation.sourceLanguageCode} " +
                        "target=${state.bookTranslation.targetLanguageCode} " +
                        "wifiOnly=${state.bookTranslation.requireWifi} " +
                        "skipGoogleWarning=$skipGoogleWarning"
            )

            if (
                state.bookTranslation.providerMode == TranslationProviderMode.GOOGLE_TRANSLATE &&
                !skipGoogleWarning &&
                getDatastore.execute(
                    DataStoreConstants.BOOK_TRANSLATION_GOOGLE_WARNING_ACCEPTED
                ) != true
            ) {
                Log.i(
                    BOOK_TRANSLATION_LOG,
                    "Google warning required before enqueue: bookId=${state.book.id}"
                )
                _state.update {
                    it.copy(
                        bookTranslation = it.bookTranslation.copy(
                            isStarting = false,
                            showGoogleWarning = true,
                            errorMessage = null
                        )
                    )
                }
                return@launch
            }

            try {
                val translation = enqueueBookTranslationUseCase.execute(
                    bookId = state.book.id,
                    text = originalText,
                    sourceLanguageCode = state.bookTranslation.sourceLanguageCode,
                    targetLanguageCode = state.bookTranslation.targetLanguageCode,
                    providerMode = state.bookTranslation.providerMode,
                    requireWifi = state.bookTranslation.requireWifi
                )
                Log.i(
                    BOOK_TRANSLATION_LOG,
                    "Enqueue returned: translationId=${translation.id} " +
                            "status=${translation.status} busy=${translation.isBusy} " +
                            "canRead=${translation.canRead}"
                )
                rememberBookTranslationLanguages(
                    sourceLanguageCode = state.bookTranslation.sourceLanguageCode,
                    targetLanguageCode = state.bookTranslation.targetLanguageCode
                )
                refreshBookTranslations(state.book.id)
                if (translation.isBusy || translation.canRead) {
                    showTranslatedBook(
                        translationOverride = translation,
                        allowPartial = true
                    )
                }
                _state.update {
                    it.copy(
                        bookTranslation = it.bookTranslation.copy(isStarting = false)
                    )
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (throwable: Throwable) {
                Log.e(
                    BOOK_TRANSLATION_LOG,
                    "Enqueue failed: bookId=${state.book.id}",
                    throwable
                )
                _state.update {
                    it.copy(
                        bookTranslation = it.bookTranslation.copy(
                            isStarting = false,
                            errorMessage = throwable.message ?: "Could not translate the book."
                        )
                    )
                }
                refreshBookTranslations(state.book.id)
            }
        }
    }

    private fun openExternalTranslator(
        textToTranslate: String,
        translateWholeParagraph: Boolean,
        activity: ComponentActivity
    ) {
        CoroutineScope(eventJob + Dispatchers.Main).launch {
            val translatorIntent = Intent()
            val browserIntent = Intent()

            translatorIntent.type = "text/plain"
            translatorIntent.action = Intent.ACTION_PROCESS_TEXT
            browserIntent.action = Intent.ACTION_WEB_SEARCH

            translatorIntent.putExtra(
                Intent.EXTRA_PROCESS_TEXT,
                textToTranslate
            )
            translatorIntent.putExtra(Intent.EXTRA_PROCESS_TEXT_READONLY, true)
            browserIntent.putExtra(
                SearchManager.QUERY,
                "translate: ${textToTranslate.trim()}"
            )

            yield()

            translatorIntent.launchActivity(
                activity = activity,
                createChooser = !translateWholeParagraph,
                success = {
                    return@launch
                }
            )
            browserIntent.launchActivity(
                activity = activity,
                success = {
                    return@launch
                }
            )

            withContext(Dispatchers.Main) {
                activity.getString(R.string.error_no_translator)
                    .showToast(context = activity, longToast = false)
            }
        }
    }

    private fun launchTranslationRequest(
        text: String,
        readerTextIndex: Int?,
        sourceLanguageCode: String,
        targetLanguageCode: String,
        providerMode: TranslationProviderMode,
        requireWifi: Boolean
    ) {
        CoroutineScope(eventJob + Dispatchers.IO).launch {
            val normalizedText = text.trim()
            if (normalizedText.isBlank()) return@launch

            val capability = getTranslationCapability.execute()
            val source = sourceLanguageCode
                .takeIf { it == AUTO_TRANSLATION_LANGUAGE }
                ?: normalizeTranslationLanguageCode(sourceLanguageCode)
                ?: AUTO_TRANSLATION_LANGUAGE
            val target = resolveTranslationLanguageCode(targetLanguageCode)
                ?: FALLBACK_TRANSLATION_TARGET_LANGUAGE

            _state.update {
                it.copy(
                    bottomSheet = if (readerTextIndex == null) {
                        ReaderScreen.TRANSLATION_BOTTOM_SHEET
                    } else it.bottomSheet,
                    drawer = null,
                    translation = it.translation.copy(
                        text = normalizedText,
                        readerTextIndex = readerTextIndex,
                        showOriginal = false,
                        sourceLanguageCode = source,
                        detectedSourceLanguageCode = null,
                        targetLanguageCode = target,
                        providerMode = providerMode,
                        requireWifi = requireWifi,
                        capability = capability,
                        translatedText = null,
                        isTranslating = capability.isAvailable(providerMode),
                        errorMessage = if (!capability.isAvailable(providerMode)) {
                            translationUnavailableMessage(providerMode)
                        } else null
                    )
                )
            }

            if (!capability.isAvailable(providerMode)) {
                return@launch
            }

            val cacheKey = buildTranslationCacheKey(
                text = normalizedText,
                sourceLanguageCode = source,
                targetLanguageCode = target,
                providerMode = providerMode
            )
            val cachedResult = translationCache[cacheKey]
            if (cachedResult != null) {
                applyTranslationResult(cachedResult)
                return@launch
            }

            runCatching {
                translateText.execute(
                    TranslationRequest(
                        text = normalizedText,
                        sourceLanguageCode = source.takeIf { it != AUTO_TRANSLATION_LANGUAGE },
                        targetLanguageCode = target,
                        requireWifi = requireWifi,
                        providerMode = providerMode
                    )
                )
            }.onSuccess { result ->
                translationCache[cacheKey] = result
                applyTranslationResult(result)
            }.onFailure { throwable ->
                _state.update {
                    it.copy(
                        translation = it.translation.copy(
                            isTranslating = false,
                            errorMessage = throwable.message ?: "Could not translate text."
                        )
                    )
                }
            }
        }
    }

    private suspend fun applyTranslationResult(result: TranslationResult) {
        _state.update {
            it.copy(
                translation = it.translation.copy(
                    detectedSourceLanguageCode = result.sourceLanguageCode,
                    targetLanguageCode = result.targetLanguageCode,
                    translatedText = result.translatedText,
                    isTranslating = false,
                    errorMessage = null
                )
            )
        }
    }

    private fun buildTranslationCacheKey(
        text: String,
        sourceLanguageCode: String,
        targetLanguageCode: String,
        providerMode: TranslationProviderMode
    ): String {
        val normalizedText = text.replace(Regex("\\s+"), " ").trim()
        return "${providerMode.name}|$sourceLanguageCode|$targetLanguageCode|$normalizedText"
    }

    private fun translationUnavailableMessage(providerMode: TranslationProviderMode): String =
        when (providerMode) {
            TranslationProviderMode.IN_APP ->
                "In-app translation is unavailable in this build."

            TranslationProviderMode.GOOGLE_TRANSLATE ->
                "Google Translate is unavailable in this build."

            TranslationProviderMode.EXTERNAL ->
                "External translation uses installed apps."
        }
}
