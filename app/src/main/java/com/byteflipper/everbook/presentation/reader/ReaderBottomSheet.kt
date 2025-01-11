package com.byteflipper.everbook.presentation.reader

import androidx.compose.runtime.Composable
import com.byteflipper.everbook.domain.util.BottomSheet
import com.byteflipper.everbook.ui.reader.ReaderEvent
import com.byteflipper.everbook.ui.reader.ReaderScreen

@Composable
fun ReaderBottomSheet(
    bottomSheet: BottomSheet?,
    fullscreenMode: Boolean,
    menuVisibility: (ReaderEvent.OnMenuVisibility) -> Unit,
    dismissBottomSheet: (ReaderEvent.OnDismissBottomSheet) -> Unit
) {
    when (bottomSheet) {
        ReaderScreen.SETTINGS_BOTTOM_SHEET -> {
            ReaderSettingsBottomSheet(
                fullscreenMode = fullscreenMode,
                menuVisibility = menuVisibility,
                dismissBottomSheet = dismissBottomSheet
            )
        }
    }
}