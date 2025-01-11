package com.byteflipper.everbook.presentation.start

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable

@Composable
fun StartBackHandler(
    navigateBack: () -> Unit
) {
    BackHandler {
        navigateBack()
    }
}