/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.reader

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.byteflipper.everbook.R
import com.byteflipper.everbook.domain.reader.ReaderText.Chapter
import com.byteflipper.everbook.presentation.core.components.common.StyledText
import com.byteflipper.everbook.presentation.core.components.modal_drawer.ModalDrawer
import com.byteflipper.everbook.presentation.core.components.modal_drawer.ModalDrawerSelectableItem
import com.byteflipper.everbook.presentation.core.components.modal_drawer.ModalDrawerTitleItem
import com.byteflipper.everbook.presentation.core.util.calculateProgress
import com.byteflipper.everbook.ui.reader.ReaderEvent

@Composable
fun ReaderChaptersDrawer(
    show: Boolean,
    chapters: List<Chapter>,
    currentChapter: Chapter?,
    currentChapterProgress: Float,
    scrollToChapter: (ReaderEvent.OnScrollToChapter) -> Unit,
    dismissDrawer: (ReaderEvent.OnDismissDrawer) -> Unit
) {
    val currentChapterIndex = remember(chapters, currentChapter) {
        derivedStateOf {
            chapters.indexOf(currentChapter).takeIf { it != -1 } ?: 0
        }
    }

    ModalDrawer(
        show = show,
        startIndex = currentChapterIndex.value,
        onDismissRequest = { dismissDrawer(ReaderEvent.OnDismissDrawer) },
        header = {
            ModalDrawerTitleItem(
                title = stringResource(id = R.string.chapters)
            )
        }
    ) {
        itemsIndexed(chapters, key = { index, _ -> index }) { index, chapter ->
            val selected = rememberSaveable(index, currentChapterIndex) {
                index == currentChapterIndex.value
            }

            ModalDrawerSelectableItem(
                selected = selected,
                onClick = {
                    scrollToChapter(
                        ReaderEvent.OnScrollToChapter(
                            chapter = chapter
                        )
                    )
                    dismissDrawer(ReaderEvent.OnDismissDrawer)
                }
            ) {
                StyledText(
                    text = chapter.title,
                    modifier = Modifier.weight(1f),
                    maxLines = 1
                )
                if (selected) {
                    Spacer(modifier = Modifier.width(18.dp))
                    StyledText(text = "${currentChapterProgress.calculateProgress(1)}%")
                }
            }
        }
    }
}