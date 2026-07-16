/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.reader

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.byteflipper.everbook.R
import com.byteflipper.everbook.domain.reader.Bookmark
import com.byteflipper.everbook.ui.reader.ReaderEvent
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@Composable
fun ReaderHighlightPalette(
    selectedText: String?,
    annotation: Bookmark?,
    anchorX: Int,
    anchorY: Int,
    colors: List<Int>,
    createHighlight: (ReaderEvent.OnCreateHighlight) -> Unit,
    createBookmark: (ReaderEvent.OnCreateBookmark) -> Unit,
    changeHighlightColor: (ReaderEvent.OnChangeHighlightColor) -> Unit,
    clearHighlightColor: (ReaderEvent.OnClearHighlightColor) -> Unit,
    openNoteEditor: (ReaderEvent.OnRequestAnnotationEditor) -> Unit,
    editAnnotation: (ReaderEvent.OnEditAnnotation) -> Unit,
    deleteBookmark: (ReaderEvent.OnDeleteBookmark) -> Unit,
    managePalette: (ReaderEvent.OnShowHighlightPaletteEditor) -> Unit,
    dismiss: (ReaderEvent.OnDismissHighlightPalette) -> Unit
) {
    val text = selectedText ?: return
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val screenWidth = density.run { configuration.screenWidthDp.dp.roundToPx() }
    val screenHeight = density.run { configuration.screenHeightDp.dp.roundToPx() }
    val edgePadding = density.run { 8.dp.roundToPx() }
    val colorStripWidth = 150.dp
    val estimatedToolbarWidth = density.run { 300.dp.roundToPx() }
    val estimatedToolbarHeight = density.run { 48.dp.roundToPx() }
    val toolbarHalfWidth = density.run { 150.dp.roundToPx() }
    val toolbarVerticalOffset = density.run { 60.dp.roundToPx() }
    val popupOffset = IntOffset(
        x = (anchorX - toolbarHalfWidth).coerceIn(
            edgePadding,
            (screenWidth - estimatedToolbarWidth - edgePadding).coerceAtLeast(edgePadding)
        ),
        y = (anchorY - toolbarVerticalOffset).coerceIn(
            edgePadding,
            (screenHeight - estimatedToolbarHeight - edgePadding).coerceAtLeast(edgePadding)
        )
    )
    var toolbarOffset by remember(text, anchorX, anchorY) { mutableStateOf(popupOffset) }
    var toolbarSize by remember { mutableStateOf(IntSize.Zero) }
    var visible by remember(text, anchorX, anchorY) { mutableStateOf(true) }
    var exitAction by remember(text, anchorX, anchorY) { mutableStateOf<(() -> Unit)?>(null) }

    fun hide(onHidden: () -> Unit) {
        if (!visible) return
        exitAction = onHidden
        visible = false
    }

    LaunchedEffect(visible) {
        if (!visible) {
            delay(180)
            exitAction?.invoke()
        }
    }

    Popup(
        alignment = Alignment.TopStart,
        offset = IntOffset.Zero,
        onDismissRequest = {
            hide { dismiss(ReaderEvent.OnDismissHighlightPalette) }
        },
        properties = PopupProperties(
            focusable = true,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures {
                        hide { dismiss(ReaderEvent.OnDismissHighlightPalette) }
                    }
                }
        ) {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(180)) + scaleIn(
                    initialScale = 0.82f,
                    animationSpec = tween(240)
                ),
                exit = fadeOut(animationSpec = tween(120)) + scaleOut(
                    targetScale = 0.88f,
                    animationSpec = tween(180)
                )
            ) {
                Surface(
                    modifier = Modifier
                        .offset { toolbarOffset }
                        .zIndex(1f)
                        .onSizeChanged { toolbarSize = it }
                        .pointerInput(Unit) {
                            detectDragGesturesAfterLongPress { change, dragAmount ->
                                change.consume()
                                val width = toolbarSize.width.takeIf { it > 0 } ?: estimatedToolbarWidth
                                val height = toolbarSize.height.takeIf { it > 0 } ?: estimatedToolbarHeight
                                toolbarOffset = IntOffset(
                                    x = (toolbarOffset.x + dragAmount.x.roundToInt()).coerceIn(
                                    edgePadding,
                                    (screenWidth - width - edgePadding).coerceAtLeast(edgePadding)
                                    ),
                                    y = (toolbarOffset.y + dragAmount.y.roundToInt()).coerceIn(
                                    edgePadding,
                                    (screenHeight - height - edgePadding).coerceAtLeast(edgePadding)
                                    )
                                )
                            }
                        },
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    tonalElevation = 0.dp,
                    shadowElevation = 10.dp
                ) {
                Row(
                    modifier = Modifier.padding(6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LazyRow(
                        modifier = Modifier.width(colorStripWidth),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        item {
                            OutlinedIconButton(
                                modifier = Modifier.size(36.dp),
                                onClick = {
                                    hide {
                                        dismiss(ReaderEvent.OnDismissHighlightPalette)
                                        annotation?.let {
                                            clearHighlightColor(ReaderEvent.OnClearHighlightColor(it.id))
                                        } ?: createBookmark(ReaderEvent.OnCreateBookmark(text))
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.BookmarkBorder,
                                    contentDescription = stringResource(R.string.annotation_no_color),
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        items(colors) { argb ->
                            val color = androidx.compose.ui.graphics.Color(argb)
                            ReaderHighlightColorSwatch(
                                color = color,
                                selected = annotation?.colorArgb == argb,
                                size = 32.dp,
                                onClick = {
                                        hide {
                                            dismiss(ReaderEvent.OnDismissHighlightPalette)
                                            annotation?.let {
                                                changeHighlightColor(
                                                    ReaderEvent.OnChangeHighlightColor(it.id, color.toArgb())
                                                )
                                            } ?: createHighlight(
                                                ReaderEvent.OnCreateHighlight(text, color.toArgb())
                                            )
                                        }
                                }
                            )
                        }
                        item {
                            OutlinedIconButton(
                                modifier = Modifier.size(36.dp),
                                onClick = {
                                    hide {
                                        dismiss(ReaderEvent.OnDismissHighlightPalette)
                                        managePalette(
                                            ReaderEvent.OnShowHighlightPaletteEditor(
                                                selectedText = text,
                                                annotation = annotation
                                            )
                                        )
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Palette,
                                    contentDescription = stringResource(R.string.highlight_palette_manage)
                                )
                            }
                        }
                    }
                    VerticalDivider(
                        modifier = Modifier.height(28.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                    OutlinedIconButton(
                        modifier = Modifier.size(36.dp),
                        onClick = {
                            hide {
                                dismiss(ReaderEvent.OnDismissHighlightPalette)
                                annotation?.let {
                                    editAnnotation(ReaderEvent.OnEditAnnotation(it))
                                } ?: openNoteEditor(
                                    ReaderEvent.OnRequestAnnotationEditor(
                                        selectedText = text,
                                        initialColorArgb = ReaderHighlightColors.defaultArgb
                                    )
                                )
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.EditNote,
                            contentDescription = stringResource(id = R.string.add_annotation_note),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    if (annotation != null) {
                        OutlinedIconButton(
                            modifier = Modifier.size(36.dp),
                            onClick = {
                                hide {
                                    dismiss(ReaderEvent.OnDismissHighlightPalette)
                                    deleteBookmark(ReaderEvent.OnDeleteBookmark(annotation.id))
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.DeleteOutline,
                                contentDescription = stringResource(id = R.string.delete),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}
}
