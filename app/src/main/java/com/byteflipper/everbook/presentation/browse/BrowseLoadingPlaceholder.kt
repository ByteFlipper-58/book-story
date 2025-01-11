package com.byteflipper.everbook.presentation.browse

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.byteflipper.everbook.presentation.core.components.progress_indicator.CircularProgressIndicator

@Composable
fun BoxScope.BrowseLoadingPlaceholder(modifier: Modifier = Modifier) {
    CircularProgressIndicator(
        modifier = modifier.align(Alignment.Center)
    )
}