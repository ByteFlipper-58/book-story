package com.byteflipper.everbook.presentation.reader

import androidx.compose.runtime.Composable
import com.byteflipper.everbook.domain.reader.ReaderText.Chapter
import com.byteflipper.everbook.domain.util.Drawer
import com.byteflipper.everbook.ui.reader.ReaderEvent
import com.byteflipper.everbook.ui.reader.ReaderScreen

@Composable
fun ReaderDrawer(
    drawer: Drawer?,
    chapters: List<Chapter>,
    currentChapter: Chapter?,
    currentChapterProgress: Float,
    scrollToChapter: (ReaderEvent.OnScrollToChapter) -> Unit,
    dismissDrawer: (ReaderEvent.OnDismissDrawer) -> Unit
) {
    ReaderChaptersDrawer(
        show = drawer == ReaderScreen.CHAPTERS_DRAWER,
        chapters = chapters,
        currentChapter = currentChapter,
        currentChapterProgress = currentChapterProgress,
        scrollToChapter = scrollToChapter,
        dismissDrawer = dismissDrawer
    )
}