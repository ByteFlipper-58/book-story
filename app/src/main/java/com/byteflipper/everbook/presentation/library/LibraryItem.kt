/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.library
import androidx.compose.ui.res.painterResource

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.byteflipper.everbook.R
import com.byteflipper.everbook.domain.library.book.SelectableBook
import com.byteflipper.everbook.domain.library.display.LibraryLayout
import com.byteflipper.everbook.domain.library.display.LibraryTitlePosition
import com.byteflipper.everbook.presentation.core.components.common.AsyncCoverImage
import com.byteflipper.everbook.presentation.core.components.common.StyledText
import com.byteflipper.everbook.presentation.core.util.calculateProgress

@Composable
fun LibraryItem(
    book: SelectableBook,
    layout: LibraryLayout,
    hasSelectedItems: Boolean,
    titlePosition: LibraryTitlePosition,
    readButton: Boolean,
    showProgress: Boolean,
    selectBook: (select: Boolean?) -> Unit,
    navigateToBookInfo: () -> Unit,
    navigateToReader: () -> Unit
) {
    when (layout) {
        LibraryLayout.LIST -> LibraryListItem(
            book = book,
            hasSelectedItems = hasSelectedItems,
            readButton = readButton,
            showProgress = showProgress,
            selectBook = selectBook,
            navigateToBookInfo = navigateToBookInfo,
            navigateToReader = navigateToReader
        )

        LibraryLayout.GRID -> LibraryGridItem(
            book = book,
            hasSelectedItems = hasSelectedItems,
            titlePosition = titlePosition,
            readButton = readButton,
            showProgress = showProgress,
            selectBook = selectBook,
            navigateToBookInfo = navigateToBookInfo,
            navigateToReader = navigateToReader
        )
    }
}

@Composable
private fun LibraryGridItem(
    book: SelectableBook,
    hasSelectedItems: Boolean,
    titlePosition: LibraryTitlePosition,
    readButton: Boolean,
    showProgress: Boolean,
    selectBook: (select: Boolean?) -> Unit,
    navigateToBookInfo: () -> Unit,
    navigateToReader: () -> Unit
) {
    val backgroundColor = if (book.selected) MaterialTheme.colorScheme.secondary
    else Color.Transparent
    val belowFontColor = if (book.selected) MaterialTheme.colorScheme.onSecondary
    else MaterialTheme.colorScheme.onSurface

    val drawScrim = titlePosition == LibraryTitlePosition.INSIDE
    val scrimColor = if (drawScrim) {
        MaterialTheme.colorScheme.scrim.copy(0.3f)
    } else Color.Transparent
    val insideFontColor = Color.White.copy(0.85f)
    // Build the scrim gradient once per color instead of inside drawWithContent, which would
    // allocate a fresh Brush + native Shader on every draw frame for every visible grid item.
    // A stable Brush lets Compose's ShaderBrush cache the shader across frames.
    val scrimBrush = remember(scrimColor) {
        Brush.verticalGradient(0f to Color.Transparent, 1f to scrimColor)
    }

    val progress = remember(book.data.progress) {
        "${book.data.progress.calculateProgress(1)}%"
    }

    Column(
        Modifier
            .padding(3.dp)
            .clip(MaterialTheme.shapes.large)
            .background(backgroundColor)
            .padding(3.dp)
    ) {
        Box(
            modifier = Modifier
                .aspectRatio(1f / 1.5f)
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                .combinedClickable(
                    onClick = {
                        if (hasSelectedItems) selectBook(null)
                        else navigateToBookInfo()
                    },
                    onLongClick = {
                        if (!hasSelectedItems) selectBook(true)
                    }
                )
        ) {
            if (book.data.coverImage != null) {
                AsyncCoverImage(
                    uri = book.data.coverImage,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(MaterialTheme.shapes.medium)
                )
            } else {
                Icon(
                    painter = painterResource(R.drawable.ic_image_rounded_24px),
                    contentDescription = stringResource(
                        id = R.string.cover_image_not_found_content_desc
                    ),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth(0.7f)
                        .aspectRatio(1f),
                    tint = MaterialTheme.colorScheme.surfaceContainerHigh
                )
            }

            if (showProgress) {
                StyledText(
                    text = progress,
                    modifier = Modifier
                        .padding(6.dp)
                        .clip(MaterialTheme.shapes.small)
                        .background(MaterialTheme.colorScheme.tertiary)
                        .padding(horizontal = 6.dp, vertical = 3.dp),
                    style = MaterialTheme.typography.bodySmall.copy(
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onTertiary,
                    )
                )
            }

            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .drawWithContent {
                        if (drawScrim) drawRect(brush = scrimBrush)
                        drawContent()
                    }
                    .padding(6.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.End
            ) {
                if (titlePosition == LibraryTitlePosition.INSIDE) {
                    StyledText(
                        text = book.data.title,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 2.dp),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = insideFontColor
                        ),
                        maxLines = 2
                    )

                    Spacer(modifier = Modifier.width(6.dp))
                }

                if (readButton) {
                    FilledIconButton(
                        onClick = { navigateToReader() },
                        modifier = Modifier.size(32.dp),
                        shape = MaterialTheme.shapes.medium,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_play_arrow_rounded_24px),
                            contentDescription = stringResource(id = R.string.continue_reading_content_desc),
                            Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        if (titlePosition == LibraryTitlePosition.BELOW) {
            Spacer(modifier = Modifier.height(6.dp))
            StyledText(
                text = book.data.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = belowFontColor
                ),
                minLines = 2,
                maxLines = 2
            )
            Spacer(modifier = Modifier.height(2.dp))
        }
    }
}

@Composable
private fun LibraryListItem(
    book: SelectableBook,
    hasSelectedItems: Boolean,
    readButton: Boolean,
    showProgress: Boolean,
    selectBook: (select: Boolean?) -> Unit,
    navigateToBookInfo: () -> Unit,
    navigateToReader: () -> Unit
) {
    val backgroundColor = if (book.selected) MaterialTheme.colorScheme.secondaryContainer
    else Color.Transparent
    val fontColor = if (book.selected) MaterialTheme.colorScheme.onSecondaryContainer
    else MaterialTheme.colorScheme.onSurface

    val progress = remember(book.data.progress) {
        "${book.data.progress.calculateProgress(1)}%"
    }

    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(backgroundColor)
            .combinedClickable(
                onClick = {
                    if (hasSelectedItems) selectBook(null)
                    else navigateToBookInfo()
                },
                onLongClick = {
                    if (!hasSelectedItems) selectBook(true)
                }
            )
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(MaterialTheme.shapes.small)
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
        ) {
            if (book.data.coverImage != null) {
                AsyncCoverImage(
                    uri = book.data.coverImage,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(MaterialTheme.shapes.small)
                )
            } else {
                Icon(
                    painter = painterResource(R.drawable.ic_image_rounded_24px),
                    contentDescription = stringResource(id = R.string.cover_image_not_found_content_desc),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth(0.7f)
                        .aspectRatio(1f),
                    tint = MaterialTheme.colorScheme.surfaceContainerHigh
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        StyledText(
            text = book.data.title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium.copy(
                color = fontColor
            ),
            maxLines = 2
        )

        if (showProgress) {
            Spacer(modifier = Modifier.width(8.dp))
            StyledText(
                text = progress,
                modifier = Modifier
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.tertiary)
                    .padding(horizontal = 6.dp, vertical = 3.dp),
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onTertiary
                )
            )
        }

        if (readButton) {
            Spacer(modifier = Modifier.width(8.dp))
            FilledIconButton(
                onClick = { navigateToReader() },
                modifier = Modifier.size(32.dp),
                shape = MaterialTheme.shapes.medium,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_play_arrow_rounded_24px),
                    contentDescription = stringResource(id = R.string.continue_reading_content_desc),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
