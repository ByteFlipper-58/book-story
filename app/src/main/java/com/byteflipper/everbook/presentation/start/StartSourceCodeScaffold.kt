package com.byteflipper.everbook.presentation.start

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.byteflipper.everbook.ui.about.AboutEvent

@Composable
fun StartSourceCodeScaffold(
    navigateForward: () -> Unit,
    navigateToBrowserPage: (AboutEvent.OnNavigateToBrowserPage) -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            StartSettingsBottomBar(
                currentPage = 3,
                storagePermissionGranted = true,
                navigateForward = navigateForward
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { paddingValues ->
        content(paddingValues)
    }
}