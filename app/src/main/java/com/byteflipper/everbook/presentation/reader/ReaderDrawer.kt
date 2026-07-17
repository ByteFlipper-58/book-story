/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.reader
import androidx.compose.ui.res.painterResource

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.byteflipper.everbook.R
import com.byteflipper.everbook.domain.reader.Bookmark as BookStoryBookmark
import com.byteflipper.everbook.domain.reader.ExpandableChapter
import com.byteflipper.everbook.domain.reader.ReaderText.Chapter
import com.byteflipper.everbook.domain.util.Drawer
import com.byteflipper.everbook.presentation.core.components.common.StyledText
import com.byteflipper.everbook.presentation.core.components.modal_drawer.ModalDrawer
import com.byteflipper.everbook.presentation.core.components.modal_drawer.ModalDrawerSelectableItem
import com.byteflipper.everbook.presentation.core.util.calculateProgress
import com.byteflipper.everbook.presentation.core.util.noRippleClickable
import com.byteflipper.everbook.ui.reader.ReaderEvent
import com.byteflipper.everbook.ui.reader.ReaderScreen
import com.byteflipper.everbook.ui.theme.ExpandingTransition

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderDrawer(
    drawer: Drawer?,
    chapters: List<Chapter>,
    currentChapter: Chapter?,
    currentChapterProgress: Float,
    bookmarks: List<BookStoryBookmark>,
    highlightColors: List<Int>,
    scrollToChapter: (ReaderEvent.OnScrollToChapter) -> Unit,
    scrollToBookmark: (ReaderEvent.OnScrollToBookmark) -> Unit,
    deleteBookmark: (ReaderEvent.OnDeleteBookmark) -> Unit,
    changeHighlightColor: (ReaderEvent.OnChangeHighlightColor) -> Unit,
    editAnnotation: (ReaderEvent.OnEditAnnotation) -> Unit,
    dismissDrawer: (ReaderEvent.OnDismissDrawer) -> Unit
) {
    val show = drawer != null

    var activeTab by remember { mutableStateOf(0) }

    // Both entry points share one drawer instance; only its initial tab changes.
    LaunchedEffect(drawer) {
        activeTab = if (drawer == ReaderScreen.BOOKMARKS_DRAWER) 1 else 0
    }

    val startIndex = remember(activeTab, chapters, currentChapter) {
        if (activeTab == 0) {
            chapters.indexOf(currentChapter).takeIf { it != -1 } ?: 0
        } else {
            0
        }
    }

    val expandableChapters = remember(show, chapters, currentChapter) {
        mutableStateListOf<ExpandableChapter>().apply {
            var index = 0
            while (index < chapters.size) {
                val chapter = chapters.getOrNull(index) ?: continue
                when (chapter.nested) {
                    false -> {
                        val children = chapters.drop(index + 1).takeWhile { it.nested }
                        add(
                            ExpandableChapter(
                                parent = chapter,
                                expanded = chapter.id == currentChapter?.id ||
                                        children.any { it.id == currentChapter?.id },
                                chapters = children.takeIf { it.isNotEmpty() }
                            )
                        )
                        index += children.size + 1
                    }

                    true -> {
                        add(
                            ExpandableChapter(
                                parent = chapter.copy(nested = false),
                                expanded = false,
                                chapters = null
                            )
                        )
                        index++
                    }
                }
            }
        }
    }

    ModalDrawer(
            show = show,
            startIndex = startIndex,
            onDismissRequest = { dismissDrawer(ReaderEvent.OnDismissDrawer) },
            header = {
                PrimaryTabRow(
                    selectedTabIndex = activeTab,
                    containerColor = Color.Transparent,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                ) {
                    Tab(
                        selected = activeTab == 0,
                        onClick = { activeTab = 0 },
                        selectedContentColor = MaterialTheme.colorScheme.primary,
                        unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        text = {
                            StyledText(
                                text = stringResource(id = R.string.chapters),
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1
                            )
                        }
                    )
                    Tab(
                        selected = activeTab == 1,
                        onClick = { activeTab = 1 },
                        selectedContentColor = MaterialTheme.colorScheme.primary,
                        unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        text = {
                            StyledText(
                                text = stringResource(id = R.string.annotations),
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1
                            )
                        }
                    )
                }
            }
    ) {
            if (activeTab == 0) {
                expandableChapters.forEach { expandableChapter ->
                    item {
                        ModalDrawerSelectableItem(
                            selected = expandableChapter.parent.id == currentChapter?.id,
                            onClick = {
                                scrollToChapter(
                                    ReaderEvent.OnScrollToChapter(
                                        chapter = expandableChapter.parent
                                    )
                                )
                                dismissDrawer(ReaderEvent.OnDismissDrawer)
                            }
                        ) {
                            StyledText(
                                text = expandableChapter.parent.title,
                                modifier = Modifier.weight(1f),
                                maxLines = 1
                            )

                            if (expandableChapter.parent == currentChapter) {
                                Spacer(modifier = Modifier.width(18.dp))
                                StyledText(text = "${currentChapterProgress.calculateProgress(0)}%")
                            }

                            if (!expandableChapter.chapters.isNullOrEmpty()) {
                                Spacer(modifier = Modifier.width(18.dp))
                                Icon(
                                    painter = painterResource(R.drawable.ic_arrow_drop_up_rounded_24px),
                                    modifier = Modifier
                                        .size(24.dp)
                                        .noRippleClickable {
                                            expandableChapters.indexOf(expandableChapter)
                                                .also { chapterIndex ->
                                                    if (chapterIndex == -1) return@noRippleClickable
                                                    expandableChapters[chapterIndex] =
                                                        expandableChapter.copy(
                                                            expanded = !expandableChapter.expanded
                                                        )
                                                }
                                        }
                                        .rotate(
                                            animateFloatAsState(
                                                targetValue = if (expandableChapter.expanded) 0f else -180f
                                            ).value
                                        ),
                                    contentDescription = stringResource(
                                        id = if (expandableChapter.expanded) R.string.collapse_content_desc
                                        else R.string.expand_content_desc
                                    )
                                )
                            }
                        }
                    }

                    if (!expandableChapter.chapters.isNullOrEmpty()) {
                        items(expandableChapter.chapters) { chapter ->
                            ExpandingTransition(visible = expandableChapter.expanded) {
                                ModalDrawerSelectableItem(
                                    selected = chapter.id == currentChapter?.id,
                                    onClick = {
                                        scrollToChapter(
                                            ReaderEvent.OnScrollToChapter(
                                                chapter = chapter
                                            )
                                        )
                                        dismissDrawer(ReaderEvent.OnDismissDrawer)
                                    }
                                ) {
                                    Spacer(modifier = Modifier.width(18.dp))

                                    StyledText(
                                        text = chapter.title,
                                        modifier = Modifier.weight(1f),
                                        maxLines = 1
                                    )

                                    if (chapter == currentChapter) {
                                        Spacer(modifier = Modifier.width(18.dp))
                                        StyledText(text = "${currentChapterProgress.calculateProgress(0)}%")
                                    }
                                }
                            }
                        }
                    }
                }
        } else {
                if (bookmarks.isEmpty()) {
                    item {
                        StyledText(
                            text = stringResource(id = R.string.annotations_empty),
                            modifier = Modifier.padding(horizontal = 30.dp, vertical = 12.dp),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                } else {
                    items(bookmarks, key = { it.id }) { bookmark ->
                        ModalDrawerSelectableItem(
                            selected = false,
                            onClick = {
                                dismissDrawer(ReaderEvent.OnDismissDrawer)
                                scrollToBookmark(ReaderEvent.OnScrollToBookmark(bookmark))
                            }
                        ) {
                            AnnotationTypeIndicator(bookmark = bookmark)
                            Spacer(modifier = Modifier.width(14.dp))

                            val percent = "${bookmark.progress.calculateProgress(0)}%"
                            val subtitle = if (bookmark.chapterTitle.isBlank()) percent
                            else "${bookmark.chapterTitle} · $percent"

                            Column(modifier = Modifier.weight(1f)) {
                                StyledText(
                                    text = bookmark.quotedText.ifBlank {
                                        stringResource(id = R.string.bookmark)
                                    },
                                    maxLines = 2
                                )
                                Spacer(modifier = Modifier.size(2.dp))
                                StyledText(
                                    text = subtitle,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    maxLines = 1
                                )

                                if (bookmark.hasNote) {
                                    Spacer(modifier = Modifier.size(4.dp))
                                    StyledText(
                                        text = bookmark.note.orEmpty(),
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = MaterialTheme.colorScheme.primary
                                        ),
                                        maxLines = 3
                                    )
                                }

                            }

                            AnnotationActionsMenu(
                                bookmark = bookmark,
                                highlightColors = highlightColors,
                                changeHighlightColor = changeHighlightColor,
                                editAnnotation = editAnnotation,
                                deleteBookmark = deleteBookmark
                            )
                        }
                    }
                }
            }
        }
}

@Composable
private fun AnnotationActionsMenu(
    bookmark: BookStoryBookmark,
    highlightColors: List<Int>,
    changeHighlightColor: (ReaderEvent.OnChangeHighlightColor) -> Unit,
    editAnnotation: (ReaderEvent.OnEditAnnotation) -> Unit,
    deleteBookmark: (ReaderEvent.OnDeleteBookmark) -> Unit
) {
    var expanded by remember(bookmark.id) { mutableStateOf(false) }

    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(
                painter = painterResource(R.drawable.ic_more_vert_rounded_24px),
                contentDescription = stringResource(id = R.string.show_more_content_desc)
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            }
        ) {
            if (bookmark.isHighlight) {
                HighlightColorSwatches(
                    selectedArgb = bookmark.colorArgb,
                    colors = highlightColors,
                    onSelect = { argb ->
                        changeHighlightColor(
                            ReaderEvent.OnChangeHighlightColor(bookmark.id, argb)
                        )
                    }
                )
            }
            DropdownMenuItem(
                text = { StyledText(text = stringResource(id = R.string.edit_annotation)) },
                onClick = {
                    expanded = false
                    editAnnotation(ReaderEvent.OnEditAnnotation(bookmark))
                },
                leadingIcon = {
                    Icon(painter = painterResource(R.drawable.ic_edit_rounded_24px), contentDescription = null)
                }
            )
            DropdownMenuItem(
                text = { StyledText(text = stringResource(id = R.string.delete)) },
                onClick = {
                    expanded = false
                    deleteBookmark(ReaderEvent.OnDeleteBookmark(bookmark.id))
                },
                leadingIcon = {
                    Icon(painter = painterResource(R.drawable.ic_delete_rounded_24px), contentDescription = null)
                }
            )
        }
    }
}

@Composable
private fun AnnotationTypeIndicator(bookmark: BookStoryBookmark) {
    val color = when {
        bookmark.isHighlight -> ReaderHighlightColors.colorFor(bookmark.colorArgb)
        bookmark.hasNote -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.surfaceContainerHighest
    }
    Box(
        modifier = Modifier
            .size(30.dp)
            .clip(CircleShape)
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        when {
            bookmark.hasNote -> Icon(
                painter = painterResource(R.drawable.ic_edit_note_rounded_24px),
                contentDescription = null,
                modifier = Modifier.size(17.dp),
                tint = MaterialTheme.colorScheme.onPrimary
            )
            bookmark.isHighlight -> Unit
            else -> Icon(
                painter = painterResource(R.drawable.ic_bookmark_rounded_24px),
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun HighlightColorSwatches(
    selectedArgb: Int?,
    colors: List<Int>,
    onSelect: (Int) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .width(240.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(colors) { argb ->
            ReaderHighlightColorSwatch(
                color = Color(argb),
                selected = argb == selectedArgb,
                size = 22.dp,
                onClick = { onSelect(argb) }
            )
        }
    }
}
