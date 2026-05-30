/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.pdf_reader

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.byteflipper.everbook.R
import com.byteflipper.everbook.presentation.core.components.common.IconButton
import com.byteflipper.everbook.presentation.core.components.common.StyledText
import com.byteflipper.everbook.presentation.core.util.calculateProgress
import com.byteflipper.everbook.presentation.core.util.noRippleClickable
import com.byteflipper.everbook.ui.pdf_reader.PdfReaderEvent
import com.byteflipper.everbook.ui.pdf_reader.PdfReaderModel
import com.byteflipper.everbook.ui.theme.readerBarsColor
import kotlin.math.roundToInt

@Composable
fun PdfReaderBottomBar(
    pageIndex: Int,
    pageCount: Int,
    zoom: Float,
    bottomBarPadding: Dp,
    showZoomControls: Boolean,
    scrollToPage: (PdfReaderEvent.OnScrollToPage) -> Unit,
    changeZoom: (PdfReaderEvent.OnChangeZoom) -> Unit
) {
    val progress = remember(pageIndex, pageCount) {
        if (pageCount <= 0) "0%"
        else "${(pageIndex / (pageCount - 1).coerceAtLeast(1).toFloat()).calculateProgress(1)}%"
    }
    val pageLabel = remember(pageIndex, pageCount, progress) {
        "$progress · ${pageIndex + 1} / $pageCount"
    }

    Column(
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.readerBarsColor)
            .noRippleClickable {}
            .navigationBarsPadding()
            .padding(horizontal = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(Modifier.height(16.dp))

        StyledText(
            text = pageLabel,
            style = MaterialTheme.typography.titleLarge.copy(
                color = MaterialTheme.colorScheme.onSurface
            )
        )

        Slider(
            value = pageIndex.toFloat(),
            onValueChange = {
                scrollToPage(PdfReaderEvent.OnScrollToPage(it.roundToInt()))
            },
            valueRange = 0f..(pageCount - 1).coerceAtLeast(0).toFloat(),
            colors = SliderDefaults.colors(
                inactiveTrackColor = MaterialTheme.colorScheme.secondary.copy(0.15f),
                disabledActiveTrackColor = MaterialTheme.colorScheme.primary,
                disabledThumbColor = MaterialTheme.colorScheme.primary,
                disabledInactiveTrackColor = MaterialTheme.colorScheme.secondary.copy(0.15f),
            )
        )

        if (showZoomControls) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ZoomIn,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.width(8.dp))

                IconButton(
                    icon = Icons.Default.Remove,
                    contentDescription = R.string.zoom_out_content_desc,
                    modifier = Modifier.size(32.dp),
                    disableOnClick = false,
                    enabled = zoom > PdfReaderModel.MIN_ZOOM
                ) {
                    changeZoom(
                        PdfReaderEvent.OnChangeZoom(
                            pageIndex = pageIndex,
                            zoom = zoom - 0.25f
                        )
                    )
                }

                StyledText(
                    text = "${(zoom * 100).roundToInt()}%",
                    modifier = Modifier.width(64.dp),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                )

                IconButton(
                    icon = Icons.Default.Add,
                    contentDescription = R.string.zoom_in_content_desc,
                    modifier = Modifier.size(32.dp),
                    disableOnClick = false,
                    enabled = zoom < PdfReaderModel.MAX_ZOOM
                ) {
                    changeZoom(
                        PdfReaderEvent.OnChangeZoom(
                            pageIndex = pageIndex,
                            zoom = zoom + 0.25f
                        )
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp + bottomBarPadding))
    }
}
