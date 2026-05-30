/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.ui.reader

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
import com.byteflipper.everbook.domain.reader.PdfReadingMode
import com.byteflipper.everbook.domain.reader.Checkpoint
import com.byteflipper.everbook.domain.reader.ReaderText
import com.byteflipper.everbook.domain.reader.ReaderText.Chapter
import com.byteflipper.everbook.domain.ui.UIText
import com.byteflipper.everbook.domain.use_case.book.GetBookById
import com.byteflipper.everbook.domain.use_case.book.GetText
import com.byteflipper.everbook.domain.use_case.book.UpdateBook
import com.byteflipper.everbook.domain.use_case.history.GetLatestHistory
import com.byteflipper.everbook.presentation.core.util.coerceAndPreventNaN
import com.byteflipper.everbook.presentation.core.util.launchActivity
import com.byteflipper.everbook.presentation.core.util.setBrightness
import com.byteflipper.everbook.presentation.core.util.showToast
import com.byteflipper.everbook.ui.history.HistoryScreen
import com.byteflipper.everbook.ui.library.LibraryScreen
import javax.inject.Inject
import kotlin.math.roundToInt

private const val READER = "READER, MODEL"
private const val READER_UI_MIN_UPDATE_ITEMS = 640
private const val READER_UI_MIN_UPDATE_MS = 300L

@HiltViewModel
class ReaderModel @Inject constructor(
    private val getBookById: GetBookById,
    private val updateBook: UpdateBook,
    private val getText: GetText,
    private val getLatestHistory: GetLatestHistory
) : ViewModel() {

    private val mutex = Mutex()

    private val _state = MutableStateFlow(ReaderState())
    val state = _state.asStateFlow()

    private var eventJob = SupervisorJob()
    private var resetJob: Job? = null

    private var scrollJob: Job? = null
    private var progressJob: Job? = null
    private var loadJob: Job? = null

    fun onEvent(event: ReaderEvent) {
        viewModelScope.launch(eventJob + Dispatchers.Main) {
            when (event) {
                is ReaderEvent.OnLoadText -> {
                    loadJob?.cancel()
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
                                    text = snapshot,
                                    chapters = chapters,
                                    chapterIndexes = chapterIndexes.toList(),
                                    isLoading = false,
                                    isParsing = true,
                                    errorMessage = null
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
                                        pdfTextModeUnavailable = true
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
                                    }
                                )
                            }
                            systemBarsVisibility(show = true, activity = event.activity)
                            restoreInitialScrollJob.cancel()
                            return@launch
                        }

                        if (accumulated.size != text.size || accumulated.isEmpty()) {
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
                                errorMessage = null
                            )
                        }

                        yield()

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

                                    Checkpoint(firstVisibleItemIndex, firstVisibleItemScrollOffset)
                                }
                            )
                        }
                    }
                }

                is ReaderEvent.OnChangePdfReadingMode -> {
                    launch {
                        if (
                            event.mode == PdfReadingMode.PARSED_TEXT &&
                            !_state.value.book.pdfTextModeAvailable
                        ) {
                            return@launch
                        }

                        _state.update {
                            it.copy(
                                book = it.book.copy(pdfReadingMode = event.mode),
                                isLoading = event.mode == PdfReadingMode.PARSED_TEXT,
                                errorMessage = null
                            )
                        }
                        updateBook.execute(_state.value.book)

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

                        updateBook.execute(_state.value.book)

                        LibraryScreen.refreshListChannel.trySend(300)
                        HistoryScreen.refreshListChannel.trySend(300)
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

                            listState.requestScrollToItem(chapterIndex)
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
                        delay(300)
                        yield()

                        val scrollTo = (_state.value.text.lastIndex * event.progress).roundToInt()
                        _state.value.listState.requestScrollToItem(scrollTo)
                        updateChapter(scrollTo)
                    }
                }

                is ReaderEvent.OnRestoreCheckpoint -> {
                    launch {
                        _state.value.apply {
                            listState.requestScrollToItem(
                                checkpoint.index,
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

                        _state.update {
                            it.copy(
                                lockMenu = true
                            )
                        }

                        _state.value.listState.apply {
                            if (
                                _state.value.isLoading ||
                                layoutInfo.totalItemsCount < 1 ||
                                _state.value.text.isEmpty() ||
                                _state.value.errorMessage != null
                            ) return@apply

                            _state.update {
                                it.copy(
                                    book = it.book.copy(
                                        progress = calculateProgress(),
                                        scrollIndex = firstVisibleItemIndex,
                                        scrollOffset = firstVisibleItemScrollOffset
                                    )
                                )
                            }

                            updateBook.execute(_state.value.book)

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

                is ReaderEvent.OnOpenTranslator -> {
                    launch {
                        val translatorIntent = Intent()
                        val browserIntent = Intent()

                        translatorIntent.type = "text/plain"
                        translatorIntent.action = Intent.ACTION_PROCESS_TEXT
                        browserIntent.action = Intent.ACTION_WEB_SEARCH

                        translatorIntent.putExtra(
                            Intent.EXTRA_PROCESS_TEXT,
                            event.textToTranslate
                        )
                        translatorIntent.putExtra(Intent.EXTRA_PROCESS_TEXT_READONLY, true)
                        browserIntent.putExtra(
                            SearchManager.QUERY,
                            "translate: ${event.textToTranslate.trim()}"
                        )

                        yield()

                        translatorIntent.launchActivity(
                            activity = event.activity,
                            createChooser = !event.translateWholeParagraph,
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
                            event.activity.getString(R.string.error_no_translator)
                                .showToast(context = event.activity, longToast = false)
                        }
                    }
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
            val book = getBookById.execute(bookId)

            if (book == null) {
                navigateBack()
                return@launch
            }

            eventJob.cancel()
            resetJob?.cancel()
            eventJob.join()
            resetJob?.join()
            eventJob = SupervisorJob()

            _state.update {
                ReaderState(book = book)
            }

            if (
                book.filePath.endsWith(".pdf", ignoreCase = true) &&
                book.pdfReadingMode == PdfReadingMode.ORIGINAL_PDF
            ) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        showMenu = false
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

    @OptIn(FlowPreview::class)
    fun updateProgress(listState: LazyListState) {
        progressJob?.cancel()
        progressJob = viewModelScope.launch(Dispatchers.Main) {
            snapshotFlow {
                listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset
            }.distinctUntilChanged().debounce(300).collectLatest { (index, offset) ->
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

                updateBook.execute(_state.value.book)

                LibraryScreen.refreshListChannel.trySend(0)
                HistoryScreen.refreshListChannel.trySend(0)
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

            if ((firstVisibleItemIndex ?: listState.firstVisibleItemIndex) == 0) {
                return 0f
            }

            val lastVisibleItemIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
                ?: return book.progress
            if (lastVisibleItemIndex >= text.lastIndex) {
                return 1f
            }

            return@run (firstVisibleItemIndex ?: listState.firstVisibleItemIndex)
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
            eventJob = SupervisorJob()

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
}
