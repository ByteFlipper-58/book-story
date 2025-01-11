package com.byteflipper.everbook.presentation.history

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import com.byteflipper.everbook.ui.history.HistoryEvent

@Composable
fun HistoryBackHandler(
    showSearch: Boolean,
    searchVisibility: (HistoryEvent.OnSearchVisibility) -> Unit,
    navigateToLibrary: () -> Unit
) {
    BackHandler {
        if (showSearch) {
            searchVisibility(HistoryEvent.OnSearchVisibility(false))
            return@BackHandler
        }

        navigateToLibrary()
    }
}