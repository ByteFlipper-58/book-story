/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.pdf_reader

import android.graphics.Bitmap
import android.view.View
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.byteflipper.everbook.R
import com.byteflipper.everbook.domain.distribution.ReaderInlineContentMode
import com.byteflipper.everbook.domain.distribution.ReaderInlineContentPlacement
import com.byteflipper.everbook.domain.reader.PdfPageDisplayMode
import com.byteflipper.everbook.presentation.core.components.common.ReaderInlineContent
import com.byteflipper.everbook.presentation.core.components.progress_indicator.CircularProgressIndicator
import com.byteflipper.everbook.presentation.core.util.LocalActivity
import com.byteflipper.everbook.ui.pdf_reader.PdfReaderEvent
import com.byteflipper.everbook.ui.pdf_reader.PdfReaderModel
import kotlin.math.roundToInt

@Composable
fun PdfReaderLayout(
    pageCount: Int,
    listState: LazyListState,
    showMenu: Boolean,
    zoom: Float,
    zoomPageIndex: Int,
    zoomSessionId: Long,
    contentPadding: PaddingValues,
    backgroundColor: Color,
    pageDisplayMode: PdfPageDisplayMode,
    pinchZoom: Boolean,
    fullscreenMode: Boolean,
    inlineContentPlacements: List<ReaderInlineContentPlacement>,
    createInlineContentView: (Long) -> View?,
    renderPage: suspend (pageIndex: Int, targetWidth: Int) -> Bitmap?,
    changeZoom: (PdfReaderEvent.OnChangeZoom) -> Unit,
    menuVisibility: (PdfReaderEvent.OnMenuVisibility) -> Unit
) {
    val activity = LocalActivity.current
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val viewportWidthPx = remember(configuration.screenWidthDp, density) {
        with(density) { configuration.screenWidthDp.dp.roundToPx() }
    }
    val renderWidthPx = remember(viewportWidthPx) {
        (viewportWidthPx * PdfReaderModel.MAX_ZOOM).roundToInt()
    }
    val activePageIndex by remember(listState, pageCount) {
        derivedStateOf {
            listState.firstVisibleItemIndex.coerceIn(
                0,
                (pageCount - 1).coerceAtLeast(0)
            )
        }
    }
    val defaultFlingBehavior = ScrollableDefaults.flingBehavior()
    val snapFlingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
    val flingBehavior = when (pageDisplayMode) {
        PdfPageDisplayMode.CONTINUOUS -> defaultFlingBehavior
        PdfPageDisplayMode.PAGED -> snapFlingBehavior
    }
    val inlineContentByPageIndex = remember(inlineContentPlacements) {
        inlineContentPlacements
            .filter { it.mode == ReaderInlineContentMode.PDF }
            .groupBy { it.progressUnit }
    }

    LaunchedEffect(activePageIndex, pageCount, zoomSessionId) {
        if (pageCount <= 0) return@LaunchedEffect

        changeZoom(
            PdfReaderEvent.OnChangeZoom(
                pageIndex = activePageIndex,
                zoom = PdfReaderModel.MIN_ZOOM
            )
        )
    }

    val modifier = Modifier
        .fillMaxSize()
        .background(backgroundColor)
        .padding(contentPadding)
        .clickable {
            menuVisibility(
                PdfReaderEvent.OnMenuVisibility(
                    show = !showMenu,
                    fullscreenMode = fullscreenMode,
                    activity = activity
                )
            )
        }

    LazyColumn(
        modifier = modifier,
        state = listState,
        flingBehavior = flingBehavior,
        contentPadding = when (pageDisplayMode) {
            PdfPageDisplayMode.CONTINUOUS -> PaddingValues(vertical = 12.dp)
            PdfPageDisplayMode.PAGED -> PaddingValues(0.dp)
        },
        verticalArrangement = when (pageDisplayMode) {
            PdfPageDisplayMode.CONTINUOUS -> Arrangement.spacedBy(12.dp)
            PdfPageDisplayMode.PAGED -> Arrangement.spacedBy(0.dp)
        }
    ) {
        items(
            count = pageCount,
            key = { it }
        ) { pageIndex ->
            Column {
                PdfReaderPage(
                    modifier = when (pageDisplayMode) {
                        PdfPageDisplayMode.CONTINUOUS -> Modifier
                        PdfPageDisplayMode.PAGED -> Modifier
                            .fillParentMaxHeight()
                            .fillMaxWidth()
                    },
                    pageIndex = pageIndex,
                    renderWidthPx = renderWidthPx,
                    pageDisplayMode = pageDisplayMode,
                    zoom = if (
                        pageIndex == activePageIndex &&
                        pageIndex == zoomPageIndex
                    ) {
                        zoom
                    } else PdfReaderModel.MIN_ZOOM,
                    zoomSessionId = zoomSessionId,
                    pinchZoom = pinchZoom,
                    renderPage = renderPage,
                    changeZoom = {
                        if (pageIndex == activePageIndex) {
                            changeZoom(
                                PdfReaderEvent.OnChangeZoom(
                                    pageIndex = pageIndex,
                                    zoom = it
                                )
                            )
                        }
                    }
                )
                inlineContentByPageIndex[pageIndex].orEmpty().forEach { placement ->
                    ReaderInlineContent(
                        modifier = when (pageDisplayMode) {
                            PdfPageDisplayMode.CONTINUOUS -> Modifier
                                .fillMaxWidth()
                                .widthIn(max = 760.dp)
                                .align(Alignment.CenterHorizontally)
                                .padding(vertical = 12.dp)

                            PdfPageDisplayMode.PAGED -> Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        },
                        createView = { createInlineContentView(placement.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PdfReaderPage(
    modifier: Modifier = Modifier,
    pageIndex: Int,
    renderWidthPx: Int,
    pageDisplayMode: PdfPageDisplayMode,
    zoom: Float,
    zoomSessionId: Long,
    pinchZoom: Boolean,
    renderPage: suspend (pageIndex: Int, targetWidth: Int) -> Bitmap?,
    changeZoom: (Float) -> Unit
) {
    var bitmap by remember(pageIndex, renderWidthPx) { mutableStateOf<Bitmap?>(null) }
    val pageModifier = when (pageDisplayMode) {
        PdfPageDisplayMode.CONTINUOUS -> modifier.fillMaxWidth()
        PdfPageDisplayMode.PAGED -> modifier.fillMaxSize()
    }

    LaunchedEffect(pageIndex, renderWidthPx) {
        bitmap = null
        bitmap = renderPage(pageIndex, renderWidthPx)
    }

    BoxWithConstraints(
        modifier = pageModifier,
        contentAlignment = Alignment.Center
    ) {
        if (bitmap == null) {
            Box(
                Modifier
                    .then(
                        when (pageDisplayMode) {
                            PdfPageDisplayMode.CONTINUOUS -> Modifier
                                .fillMaxWidth()
                                .height(420.dp)

                            PdfPageDisplayMode.PAGED -> Modifier.fillMaxSize()
                        }
                    )
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            bitmap?.let { image ->
                val contentDescription = stringResource(
                    id = R.string.pdf_page_content_desc,
                    pageIndex + 1
                )
                val imageAspect = image.width / image.height.toFloat()

                when (pageDisplayMode) {
                    PdfPageDisplayMode.CONTINUOUS -> {
                        val imageWidth = maxWidth
                        val imageHeight = imageWidth / imageAspect

                        ZoomablePdfPageImage(
                            image = image,
                            contentDescription = contentDescription,
                            imageWidth = imageWidth,
                            imageHeight = imageHeight,
                            zoom = zoom,
                            zoomSessionId = zoomSessionId,
                            pinchZoom = pinchZoom,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(imageHeight),
                            changeZoom = changeZoom
                        )
                    }

                    PdfPageDisplayMode.PAGED -> {
                        val imageWidth = minOf(maxWidth, maxHeight * imageAspect)
                        val imageHeight = imageWidth / imageAspect

                        ZoomablePdfPageImage(
                            image = image,
                            contentDescription = contentDescription,
                            imageWidth = imageWidth,
                            imageHeight = imageHeight,
                            zoom = zoom,
                            zoomSessionId = zoomSessionId,
                            pinchZoom = pinchZoom,
                            modifier = Modifier.fillMaxSize(),
                            changeZoom = changeZoom
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ZoomablePdfPageImage(
    image: Bitmap,
    contentDescription: String,
    imageWidth: Dp,
    imageHeight: Dp,
    zoom: Float,
    zoomSessionId: Long,
    pinchZoom: Boolean,
    modifier: Modifier = Modifier,
    changeZoom: (Float) -> Unit
) {
    val density = LocalDensity.current
    val imageSize = remember(imageWidth, imageHeight, density) {
        with(density) {
            IntSize(
                width = imageWidth.roundToPx(),
                height = imageHeight.roundToPx()
            )
        }
    }
    var containerSize by remember { mutableStateOf(IntSize.Zero) }
    var displayZoom by remember { mutableStateOf(PdfReaderModel.MIN_ZOOM) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var isGestureInProgress by remember { mutableStateOf(false) }
    val changeZoomState = rememberUpdatedState(changeZoom)

    LaunchedEffect(zoom, zoomSessionId, containerSize, imageSize) {
        if (isGestureInProgress) return@LaunchedEffect

        displayZoom = zoom.coerceIn(
            PdfReaderModel.MIN_ZOOM,
            PdfReaderModel.MAX_ZOOM
        )
        offset = offset.coerceInBounds(
            containerSize = containerSize,
            imageSize = imageSize,
            zoom = displayZoom
        )
    }
    LaunchedEffect(containerSize, imageSize) {
        if (isGestureInProgress) return@LaunchedEffect

        offset = offset.coerceInBounds(
            containerSize = containerSize,
            imageSize = imageSize,
            zoom = displayZoom
        )
    }

    Box(
        modifier = modifier
            .clipToBounds()
            .onSizeChanged {
                containerSize = it
                offset = offset.coerceInBounds(
                    containerSize = it,
                    imageSize = imageSize,
                    zoom = displayZoom
                )
            }
            .pointerInput(pinchZoom, imageSize, containerSize) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    var gestureZoom = displayZoom
                    isGestureInProgress = true

                    do {
                        val event = awaitPointerEvent()
                        val pressedPointers = event.changes.count { it.pressed }

                        if (pinchZoom && pressedPointers > 1) {
                            val zoomChange = event.calculateZoom()
                            val pan = event.calculatePan()

                            if (zoomChange != 1f || pan != Offset.Zero) {
                                val previousZoom = gestureZoom
                                gestureZoom = (gestureZoom * zoomChange).coerceIn(
                                    PdfReaderModel.MIN_ZOOM,
                                    PdfReaderModel.MAX_ZOOM
                                )
                                val scaleChange = gestureZoom / previousZoom
                                val focalPoint = event.calculateCentroid() - containerSize.center

                                displayZoom = gestureZoom
                                offset = (
                                        offset * scaleChange +
                                                focalPoint * (1f - scaleChange) +
                                                pan
                                        ).coerceInBounds(
                                        containerSize = containerSize,
                                        imageSize = imageSize,
                                        zoom = displayZoom
                                    )

                                event.changes.forEach { it.consume() }
                            }
                        } else if (pressedPointers == 1 && displayZoom > PdfReaderModel.MIN_ZOOM) {
                            val pan = event.calculatePan()

                            if (pan != Offset.Zero) {
                                offset = (offset + pan).coerceInBounds(
                                    containerSize = containerSize,
                                    imageSize = imageSize,
                                    zoom = displayZoom
                                )
                                event.changes.forEach { it.consume() }
                            }
                        }
                    } while (event.changes.any { it.pressed })

                    isGestureInProgress = false
                    changeZoomState.value(displayZoom)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Image(
            bitmap = image.asImageBitmap(),
            contentDescription = contentDescription,
            modifier = Modifier
                .width(imageWidth)
                .height(imageHeight)
                .graphicsLayer {
                    scaleX = displayZoom
                    scaleY = displayZoom
                    translationX = offset.x
                    translationY = offset.y
                    transformOrigin = TransformOrigin.Center
                },
            contentScale = ContentScale.FillBounds
        )
    }
}

private val IntSize.center: Offset
    get() = Offset(width / 2f, height / 2f)

private fun Offset.coerceInBounds(
    containerSize: IntSize,
    imageSize: IntSize,
    zoom: Float
): Offset {
    if (
        zoom <= PdfReaderModel.MIN_ZOOM ||
        containerSize == IntSize.Zero ||
        imageSize == IntSize.Zero
    ) {
        return Offset.Zero
    }

    val maxX = ((imageSize.width * zoom - containerSize.width) / 2f)
        .coerceAtLeast(0f)
    val maxY = ((imageSize.height * zoom - containerSize.height) / 2f)
        .coerceAtLeast(0f)

    return Offset(
        x = x.coerceIn(-maxX, maxX),
        y = y.coerceIn(-maxY, maxY)
    )
}
