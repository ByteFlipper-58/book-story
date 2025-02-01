package com.byteflipper.everbook.presentation.reader

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.launch
import com.byteflipper.everbook.presentation.core.components.common.LazyColumnWithScrollbar
import com.byteflipper.everbook.presentation.core.components.modal_bottom_sheet.ModalBottomSheet
import com.byteflipper.everbook.presentation.core.util.LocalActivity
import com.byteflipper.everbook.presentation.settings.appearance.colors.ColorsSubcategory
import com.byteflipper.everbook.presentation.settings.reader.chapters.ChaptersSubcategory
import com.byteflipper.everbook.presentation.settings.reader.font.FontSubcategory
import com.byteflipper.everbook.presentation.settings.reader.images.ImagesSubcategory
import com.byteflipper.everbook.presentation.settings.reader.misc.MiscSubcategory
import com.byteflipper.everbook.presentation.settings.reader.padding.PaddingSubcategory
import com.byteflipper.everbook.presentation.settings.reader.progress.ProgressSubcategory
import com.byteflipper.everbook.presentation.settings.reader.reading_mode.ReadingModeSubcategory
import com.byteflipper.everbook.presentation.settings.reader.reading_speed.ReadingSpeedSubcategory
import com.byteflipper.everbook.presentation.settings.reader.system.SystemSubcategory
import com.byteflipper.everbook.presentation.settings.reader.text.TextSubcategory
import com.byteflipper.everbook.presentation.settings.reader.translator.TranslatorSubcategory
import com.byteflipper.everbook.ui.reader.ReaderEvent

private var initialPage = 0

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderSettingsBottomSheet(
    fullscreenMode: Boolean,
    menuVisibility: (ReaderEvent.OnMenuVisibility) -> Unit,
    dismissBottomSheet: (ReaderEvent.OnDismissBottomSheet) -> Unit
) {
    val activity = LocalActivity.current
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(initialPage) { 3 }
    DisposableEffect(Unit) { onDispose { initialPage = pagerState.currentPage } }

    val animatedScrimColor by animateColorAsState(
        targetValue = if (pagerState.currentPage == 2) Color.Transparent
        else BottomSheetDefaults.ScrimColor,
        animationSpec = tween(300)
    )
    val animatedHeight by animateFloatAsState(
        targetValue = if (pagerState.currentPage == 2) 0.6f else 0.7f,
        animationSpec = tween(300)
    )

    LaunchedEffect(pagerState.currentPage) {
        menuVisibility(
            ReaderEvent.OnMenuVisibility(
                show = pagerState.currentPage != 2,
                fullscreenMode = fullscreenMode,
                saveCheckpoint = false,
                activity = activity
            )
        )
    }

    ModalBottomSheet(
        hasFixedHeight = true,
        scrimColor = animatedScrimColor,
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(animatedHeight),
        dragHandle = {},
        onDismissRequest = {
            dismissBottomSheet(ReaderEvent.OnDismissBottomSheet)
        },
        sheetGesturesEnabled = false
    ) {
        ReaderSettingsBottomSheetTabRow(
            currentPage = pagerState.currentPage,
            scrollToPage = {
                scope.launch {
                    pagerState.animateScrollToPage(it)
                }
            }
        )

        HorizontalPager(state = pagerState) { page ->
            when (page) {
                0 -> {
                    LazyColumnWithScrollbar(Modifier.fillMaxSize()) {
                        ReadingModeSubcategory(
                            titleColor = { MaterialTheme.colorScheme.onSurface }
                        )
                        PaddingSubcategory(
                            titleColor = { MaterialTheme.colorScheme.onSurface }
                        )
                        SystemSubcategory(
                            titleColor = { MaterialTheme.colorScheme.onSurface }
                        )
                        ReadingSpeedSubcategory(
                            titleColor = { MaterialTheme.colorScheme.onSurface }
                        )
                        MiscSubcategory(
                            titleColor = { MaterialTheme.colorScheme.onSurface },
                            showDivider = false
                        )
                    }
                }

                1 -> {
                    LazyColumnWithScrollbar(Modifier.fillMaxSize()) {
                        FontSubcategory(
                            titleColor = { MaterialTheme.colorScheme.onSurface }
                        )
                        TextSubcategory(
                            titleColor = { MaterialTheme.colorScheme.onSurface }
                        )
                        ImagesSubcategory(
                            titleColor = { MaterialTheme.colorScheme.onSurface }
                        )
                        ChaptersSubcategory(
                            titleColor = { MaterialTheme.colorScheme.onSurface }
                        )
                        ProgressSubcategory(
                            titleColor = { MaterialTheme.colorScheme.onSurface }
                        )
                        TranslatorSubcategory(
                            titleColor = { MaterialTheme.colorScheme.onSurface },
                            showDivider = false
                        )
                    }
                }

                2 -> {
                    LazyColumnWithScrollbar(Modifier.fillMaxSize()) {
                        ColorsSubcategory(
                            showTitle = false,
                            showDivider = false,
                            backgroundColor = { MaterialTheme.colorScheme.surfaceContainer }
                        )
                    }
                }
            }
        }
    }
}