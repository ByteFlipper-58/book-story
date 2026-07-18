/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.TextButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.byteflipper.everbook.R
import com.byteflipper.everbook.presentation.core.util.LocalActivity
import com.byteflipper.everbook.domain.library.book.Book
import com.byteflipper.everbook.domain.reader.Checkpoint
import com.byteflipper.everbook.domain.reader.ReaderText
import com.byteflipper.everbook.domain.util.Direction
import com.byteflipper.everbook.presentation.core.components.common.IconButton
import com.byteflipper.everbook.presentation.core.components.common.StyledText
import com.byteflipper.everbook.presentation.core.util.noRippleClickable
import com.byteflipper.everbook.ui.reader.ReaderEvent
import com.byteflipper.everbook.ui.theme.readerBarsColor
import com.byteflipper.everbook.ui.theme.HorizontalExpandingTransition

@Composable
fun ReaderBottomBar(
    onSetAutoScrolling: (ReaderEvent.OnSetAutoScrolling) -> Unit,
    menuVisibility: (ReaderEvent.OnMenuVisibility) -> Unit,
    fullscreenMode: Boolean,
    book: Book,
    progress: String,
    text: List<ReaderText>,
    listState: LazyListState,
    displayContent: ReaderDisplayContent,
    lockMenu: Boolean,
    isParsing: Boolean,
    checkpoint: Checkpoint,
    bottomBarPadding: Dp,
    restoreCheckpoint: (ReaderEvent.OnRestoreCheckpoint) -> Unit,
    scroll: (ReaderEvent.OnScroll) -> Unit,
    changeProgress: (ReaderEvent.OnChangeProgress) -> Unit
) {
    val activity = LocalActivity.current
    val firstVisibleItemIndex = remember(displayContent) {
        derivedStateOf {
            displayContent.displayIndexToTextIndex(listState.firstVisibleItemIndex)
        }
    }
    val arrowDirection = remember(checkpoint.index, firstVisibleItemIndex.value) {
        derivedStateOf {
            val checkpointIndex = checkpoint.index
            val index = firstVisibleItemIndex.value

            when {
                checkpointIndex > index -> Direction.END
                checkpointIndex < index -> Direction.START
                else -> Direction.NEUTRAL
            }
        }
    }
    val checkpointProgress = remember(checkpoint.index, text.lastIndex) {
        derivedStateOf {
            val lastIndex = text.lastIndex.takeIf { it > 0 } ?: return@derivedStateOf 0f
            ((checkpoint.index / lastIndex.toFloat()) * 0.987f).coerceIn(0f, 0.987f)
        }
    }

    Column(
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.readerBarsColor)
            .noRippleClickable(onClick = {})
            .navigationBarsPadding()
            .padding(horizontal = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth().height(48.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalExpandingTransition(
                visible = arrowDirection.value == Direction.START,
                startDirection = true
            ) {
                IconButton(
                    icon = R.drawable.ic_arrow_back_rounded_24px,
                    contentDescription = R.string.checkpoint_back_content_desc,
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.secondary,
                    disableOnClick = false
                ) {
                    restoreCheckpoint(ReaderEvent.OnRestoreCheckpoint)
                }
            }

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                ReaderProgressIndicator(
                    book = book,
                    lockMenu = lockMenu,
                    isParsing = isParsing,
                    listState = listState,
                    displayContent = displayContent,
                    scroll = scroll,
                    changeProgress = changeProgress
                )

                if (arrowDirection.value != Direction.NEUTRAL) {
                    Box(
                        modifier = Modifier.matchParentSize(),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        ReaderBottomBarSliderIndicator(
                            progress = checkpointProgress.value,
                            bookProgress = book.progress
                        )
                    }
                }

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = progress,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                }
            }

            HorizontalExpandingTransition(
                visible = arrowDirection.value == Direction.END,
                startDirection = false
            ) {
                IconButton(
                    icon = R.drawable.ic_arrow_forward_rounded_24px,
                    contentDescription = R.string.checkpoint_forward_content_desc,
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.secondary,
                    disableOnClick = false
                ) {
                    restoreCheckpoint(ReaderEvent.OnRestoreCheckpoint)
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        TextButton(
            onClick = {
                onSetAutoScrolling(ReaderEvent.OnSetAutoScrolling(true))
                menuVisibility(
                    ReaderEvent.OnMenuVisibility(
                        show = false,
                        fullscreenMode = fullscreenMode,
                        saveCheckpoint = false,
                        activity = activity
                    )
                )
            },
            modifier = Modifier
                .padding(bottom = 4.dp)
                .height(32.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_block),
                contentDescription = null,
                modifier = Modifier.size(12.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = stringResource(id = R.string.auto_scroll_button),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.height(6.dp + bottomBarPadding))
    }
}
