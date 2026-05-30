/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.ui.pdf_reader

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.util.LruCache
import androidx.activity.ComponentActivity
import androidx.compose.foundation.lazy.LazyListState
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.byteflipper.everbook.R
import com.byteflipper.everbook.domain.file.CachedFileCompat
import com.byteflipper.everbook.domain.reader.PdfReadingMode
import com.byteflipper.everbook.domain.use_case.book.GetBookById
import com.byteflipper.everbook.domain.use_case.book.UpdateBook
import com.byteflipper.everbook.domain.ui.UIText
import com.byteflipper.everbook.presentation.core.util.coerceAndPreventNaN
import com.byteflipper.everbook.presentation.core.util.setBrightness
import com.byteflipper.everbook.ui.history.HistoryScreen
import com.byteflipper.everbook.ui.library.LibraryScreen
import com.byteflipper.everbook.ui.reader.ReaderScreen
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import java.io.File
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class PdfReaderModel @Inject constructor(
    private val application: Application,
    private val getBookById: GetBookById,
    private val updateBook: UpdateBook
) : ViewModel() {

    private val stateMutex = Mutex()
    private val rendererMutex = Mutex()
    private val initMutex = Mutex()

    private val _state = MutableStateFlow(PdfReaderState())
    val state = _state.asStateFlow()

    private var eventJob = SupervisorJob()
    private var resetJob: Job? = null
    private var progressJob: Job? = null

    private var renderer: PdfRenderer? = null
    private var fileDescriptor: ParcelFileDescriptor? = null
    private val pageCache = object : LruCache<PdfPageCacheKey, Bitmap>(PAGE_CACHE_MAX_BYTES) {
        override fun sizeOf(key: PdfPageCacheKey, value: Bitmap): Int {
            return value.allocationByteCount
        }
    }

    fun init(
        bookId: Int,
        fullscreenMode: Boolean,
        activity: ComponentActivity,
        navigateBack: () -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            initMutex.withLock {
                val book = getBookById.execute(bookId)

                if (book == null) {
                    navigateBack()
                    return@withLock
                }

                if (
                    _state.value.book.id == bookId &&
                    _state.value.pageCount > 0 &&
                    _state.value.errorMessage == null &&
                    renderer != null
                ) {
                    systemBarsVisibility(show = !fullscreenMode, activity = activity)
                    return@withLock
                }

                eventJob.cancel()
                resetJob?.cancel()
                eventJob.join()
                resetJob?.join()
                eventJob = SupervisorJob()
                closeRenderer()

                val zoomSessionId = _state.value.zoomSessionId + 1L
                _state.update {
                    PdfReaderState(
                        book = book,
                        zoom = MIN_ZOOM,
                        zoomSessionId = zoomSessionId
                    )
                }

                val result = openRendererWithRetry(book.filePath)
                if (result == null) {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = UIText.StringResource(R.string.error_could_not_open_pdf)
                        )
                    }
                    systemBarsVisibility(show = true, activity = activity)
                    return@withLock
                }

                systemBarsVisibility(show = !fullscreenMode, activity = activity)

                _state.update {
                    it.copy(
                        pageCount = result.pageCount,
                        isLoading = false,
                        errorMessage = null,
                        showMenu = false
                    )
                }

                updateBook.execute(book)
                LibraryScreen.refreshListChannel.trySend(0)
                HistoryScreen.refreshListChannel.trySend(0)
            }
        }
    }

    fun onEvent(event: PdfReaderEvent) {
        viewModelScope.launch(eventJob + Dispatchers.Main) {
            when (event) {
                is PdfReaderEvent.OnMenuVisibility -> {
                    systemBarsVisibility(
                        show = event.show || !event.fullscreenMode,
                        activity = event.activity
                    )
                    _state.update {
                        it.copy(showMenu = event.show)
                    }
                }

                is PdfReaderEvent.OnChangeProgress -> {
                    launch(Dispatchers.IO) {
                        val pageIndex = event.pageIndex.coerceIn(
                            0,
                            (_state.value.pageCount - 1).coerceAtLeast(0)
                        )
                        _state.update {
                            it.copy(
                                book = it.book.copy(
                                    pdfPageIndex = pageIndex,
                                    pdfPageOffset = event.pageOffset,
                                    progress = calculateProgress(pageIndex)
                                )
                            )
                        }

                        updateBook.execute(_state.value.book)
                        LibraryScreen.refreshListChannel.trySend(300)
                        HistoryScreen.refreshListChannel.trySend(300)
                    }
                }

                is PdfReaderEvent.OnScrollToPage -> {
                    val pageIndex = event.pageIndex.coerceIn(
                        0,
                        (_state.value.pageCount - 1).coerceAtLeast(0)
                    )
                    _state.value.listState.requestScrollToItem(pageIndex)
                    onEvent(PdfReaderEvent.OnChangeProgress(pageIndex, 0))
                }

                is PdfReaderEvent.OnChangeZoom -> {
                    val zoom = event.zoom.coerceIn(MIN_ZOOM, MAX_ZOOM)
                    val pageIndex = event.pageIndex.coerceIn(
                        0,
                        (_state.value.pageCount - 1).coerceAtLeast(0)
                    )
                    if (
                        zoom == _state.value.zoom &&
                        pageIndex == _state.value.zoomPageIndex
                    ) {
                        return@launch
                    }

                    _state.update {
                        it.copy(
                            zoom = zoom,
                            zoomPageIndex = pageIndex
                        )
                    }
                }

                is PdfReaderEvent.OnChangePdfReadingMode -> {
                    if (
                        event.mode == PdfReadingMode.PARSED_TEXT &&
                        !_state.value.book.pdfTextModeAvailable
                    ) {
                        return@launch
                    }

                    if (event.mode == PdfReadingMode.PARSED_TEXT) {
                        saveCurrentPosition()
                        closeRenderer()
                    }
                    val updatedBook = _state.value.book.copy(pdfReadingMode = event.mode)
                    updateBook.execute(updatedBook)
                    _state.update {
                        it.copy(
                            book = updatedBook,
                            zoom = MIN_ZOOM
                        )
                    }
                    LibraryScreen.refreshListChannel.trySend(0)
                    HistoryScreen.refreshListChannel.trySend(0)
                }

                is PdfReaderEvent.OnShowPdfReadingModeBottomSheet -> {
                    _state.update {
                        it.copy(bottomSheet = ReaderScreen.PDF_READING_MODE_BOTTOM_SHEET)
                    }
                }

                is PdfReaderEvent.OnShowSettingsBottomSheet -> {
                    _state.update {
                        it.copy(bottomSheet = ReaderScreen.SETTINGS_BOTTOM_SHEET)
                    }
                }

                is PdfReaderEvent.OnDismissBottomSheet -> {
                    _state.update {
                        it.copy(bottomSheet = null)
                    }
                }

                is PdfReaderEvent.OnLeave -> {
                    saveCurrentPosition()
                    WindowCompat.getInsetsController(
                        event.activity.window,
                        event.activity.window.decorView
                    ).show(WindowInsetsCompat.Type.systemBars())
                    event.activity.setBrightness(brightness = null)
                    event.navigate()
                }
            }
        }
    }

    @OptIn(FlowPreview::class)
    fun updateProgress(listState: LazyListState) {
        progressJob?.cancel()
        progressJob = viewModelScope.launch(Dispatchers.Main) {
            snapshotFlow {
                listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset
            }.distinctUntilChanged().debounce(300).collectLatest { (index, offset) ->
                if (_state.value.isLoading || _state.value.pageCount == 0) return@collectLatest
                onEvent(PdfReaderEvent.OnChangeProgress(index, offset))
            }
        }
    }

    suspend fun renderPage(
        pageIndex: Int,
        targetWidth: Int
    ): Bitmap? {
        if (targetWidth <= 0) return null

        val width = targetWidth.coerceIn(MIN_PAGE_WIDTH_PX, MAX_PAGE_WIDTH_PX)
        val cacheKey = PdfPageCacheKey(pageIndex, width)

        return withContext(Dispatchers.IO) {
            try {
                rendererMutex.withLock {
                    val renderer = renderer ?: return@withLock null
                    pageCache.get(cacheKey)?.takeUnless { it.isRecycled }?.let {
                        return@withLock it
                    }
                    if (pageIndex !in 0 until renderer.pageCount) {
                        return@withLock null
                    }

                    renderer.openPage(pageIndex).use { page ->
                        val height = (width * (page.height / page.width.toFloat()))
                            .roundToInt()
                            .coerceAtLeast(1)
                        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                        bitmap.eraseColor(Color.WHITE)
                        page.render(
                            bitmap,
                            null,
                            null,
                            PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
                        )
                        pageCache.put(cacheKey, bitmap)
                        bitmap
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    fun resetScreen() {
        resetJob = viewModelScope.launch(Dispatchers.Main) {
            eventJob.cancel()
            progressJob?.cancel()
            eventJob = SupervisorJob()
            closeRenderer()

            yield()
            _state.update { PdfReaderState() }
        }
    }

    override fun onCleared() {
        progressJob?.cancel()
        runBlocking {
            closeRenderer()
        }
        super.onCleared()
    }

    private suspend fun saveCurrentPosition() {
        _state.value.listState.apply {
            if (_state.value.isLoading || _state.value.pageCount == 0) return
            _state.update {
                it.copy(
                    book = it.book.copy(
                        pdfPageIndex = firstVisibleItemIndex,
                        pdfPageOffset = firstVisibleItemScrollOffset,
                        progress = calculateProgress(firstVisibleItemIndex)
                    )
                )
            }
            updateBook.execute(_state.value.book)
            LibraryScreen.refreshListChannel.trySend(0)
            HistoryScreen.refreshListChannel.trySend(0)
        }
    }

    private fun calculateProgress(pageIndex: Int): Float {
        val lastPageIndex = (_state.value.pageCount - 1).coerceAtLeast(0)
        if (lastPageIndex == 0) return 0f
        return (pageIndex / lastPageIndex.toFloat()).coerceAndPreventNaN()
    }

    private suspend fun openRendererWithRetry(path: String): PdfRenderer? {
        repeat(OPEN_RETRY_COUNT) { attempt ->
            openRenderer(path)?.let { return it }
            delay(OPEN_RETRY_DELAY_MS * (attempt + 1))
        }
        return null
    }

    private suspend fun openRenderer(path: String): PdfRenderer? {
        return try {
            val file = File(path).takeIf { it.exists() && it.canRead() }
            if (file != null) {
                return openRenderer(file)
            }

            val cachedFile = CachedFileCompat.fromFullPath(
                context = application,
                path = path,
                builder = CachedFileCompat.build(
                    name = path.substringAfterLast(File.separator),
                    path = path,
                    isDirectory = false
                )
            ) ?: return null

            val cachedRawFile = cachedFile.rawFile
                ?.takeIf { it.exists() && it.canRead() }
                ?: return null

            openRenderer(cachedRawFile)
        } catch (e: Exception) {
            e.printStackTrace()
            closeRenderer()
            null
        }
    }

    private suspend fun openRenderer(file: File): PdfRenderer {
        val descriptor = ParcelFileDescriptor.open(
            file,
            ParcelFileDescriptor.MODE_READ_ONLY
        )
        return try {
            rendererMutex.withLock {
                val pdfRenderer = PdfRenderer(descriptor)
                fileDescriptor = descriptor
                renderer = pdfRenderer
                pdfRenderer
            }
        } catch (e: Exception) {
            try {
                descriptor.close()
            } catch (closeException: Exception) {
                closeException.printStackTrace()
            }
            throw e
        }
    }

    private suspend fun closeRenderer() {
        rendererMutex.withLock {
            closeRendererLocked()
        }
    }

    private fun closeRendererLocked() {
        pageCache.evictAll()
        try {
            renderer?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            fileDescriptor?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        renderer = null
        fileDescriptor = null
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

    private suspend inline fun <T> MutableStateFlow<T>.update(function: (T) -> T) {
        stateMutex.withLock {
            yield()
            this.value = function(this.value)
        }
    }

    companion object {
        const val MIN_ZOOM = 1f
        const val MAX_ZOOM = 3f
        private const val OPEN_RETRY_COUNT = 3
        private const val OPEN_RETRY_DELAY_MS = 120L
        private const val MIN_PAGE_WIDTH_PX = 320
        private const val MAX_PAGE_WIDTH_PX = 2400
        private const val PAGE_CACHE_MAX_BYTES = 64 * 1024 * 1024
    }

    private data class PdfPageCacheKey(
        val pageIndex: Int,
        val width: Int
    )
}
