package com.byteflipper.everbook.presentation.start

import androidx.compose.runtime.Composable

@Composable
fun StartDone(
    navigateToBrowse: () -> Unit,
    navigateToHelp: () -> Unit
) {
    StartDoneScaffold(
        navigateToBrowse = navigateToBrowse,
        navigateToHelp = navigateToHelp
    )
}