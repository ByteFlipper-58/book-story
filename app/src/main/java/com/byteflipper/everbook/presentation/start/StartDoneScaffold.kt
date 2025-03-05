/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.start

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun StartDoneScaffold(
    navigateToBrowse: () -> Unit,
    navigateToHelp: () -> Unit
) {
    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
        bottomBar = {
            StartDoneBottomBar(
                navigateToBrowse = navigateToBrowse,
                navigateToHelp = navigateToHelp
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        StartDoneLayout(
            paddingValues = it
        )
    }
}