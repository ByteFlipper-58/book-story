package com.byteflipper.everbook.presentation.credits

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import com.byteflipper.everbook.ui.about.AboutEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreditsContent(
    scrollBehavior: TopAppBarScrollBehavior,
    listState: LazyListState,
    navigateToBrowserPage: (AboutEvent.OnNavigateToBrowserPage) -> Unit,
    navigateBack: () -> Unit
) {
    CreditsScaffold(
        scrollBehavior = scrollBehavior,
        listState = listState,
        navigateToBrowserPage = navigateToBrowserPage,
        navigateBack = navigateBack
    )
}