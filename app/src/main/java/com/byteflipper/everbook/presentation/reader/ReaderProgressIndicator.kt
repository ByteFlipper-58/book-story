/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.byteflipper.everbook.domain.library.book.Book
import com.byteflipper.everbook.ui.reader.ReaderEvent

@Composable
fun ReaderProgressIndicator(
    book: Book,
    lockMenu: Boolean,
    isParsing: Boolean,
    listState: LazyListState,
    displayContent: ReaderDisplayContent,
    scroll: (ReaderEvent.OnScroll) -> Unit,
    changeProgress: (ReaderEvent.OnChangeProgress) -> Unit
) {
    val enabled = !lockMenu && !isParsing
    var dragProgress by remember(book.progress) { mutableFloatStateOf(book.progress) }

    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val activeColor = MaterialTheme.colorScheme.primary

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(36.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(trackColor)
            .then(
                if (enabled) {
                    Modifier
                        .pointerInput(Unit) {
                            detectTapGestures { offset ->
                                val fraction = (offset.x / size.width).coerceIn(0f, 1f)
                                dragProgress = fraction
                                applyScroll(
                                    fraction, listState, displayContent, scroll, changeProgress
                                )
                            }
                        }
                        .pointerInput(Unit) {
                            detectHorizontalDragGestures(
                                onDragStart = {},
                                onDragEnd = {},
                                onDragCancel = {},
                                onHorizontalDrag = { change, _ ->
                                    change.consume()
                                    val fraction = (change.position.x / size.width).coerceIn(0f, 1f)
                                    dragProgress = fraction
                                    applyScroll(
                                        fraction, listState, displayContent, scroll, changeProgress
                                    )
                                }
                            )
                        }
                } else Modifier
            ),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(dragProgress.coerceIn(0.02f, 1f))
                .fillMaxHeight()
                .clip(RoundedCornerShape(18.dp))
                .background(activeColor)
        )
    }
}

private fun applyScroll(
    fraction: Float,
    listState: LazyListState,
    displayContent: ReaderDisplayContent,
    scroll: (ReaderEvent.OnScroll) -> Unit,
    changeProgress: (ReaderEvent.OnChangeProgress) -> Unit
) {
    if (listState.layoutInfo.totalItemsCount > 0) {
        scroll(ReaderEvent.OnScroll(fraction))
        changeProgress(
            ReaderEvent.OnChangeProgress(
                progress = fraction,
                firstVisibleItemIndex = displayContent.displayIndexToTextIndex(
                    listState.firstVisibleItemIndex
                ),
                firstVisibleItemOffset = 0
            )
        )
    }
}
