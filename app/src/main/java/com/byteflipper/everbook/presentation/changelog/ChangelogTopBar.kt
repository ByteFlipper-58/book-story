/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.changelog

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.byteflipper.everbook.R
import com.byteflipper.everbook.presentation.core.components.common.StyledText
import com.byteflipper.everbook.presentation.navigator.NavigatorBackIconButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangelogTopBar(
    versionName: String?,
    scrollBehavior: TopAppBarScrollBehavior,
    navigateBack: () -> Unit
) {
    LargeTopAppBar(
        title = {
            Column {
                StyledText(text = stringResource(id = R.string.changelog_option))
                versionName?.let {
                    StyledText(
                        text = stringResource(id = R.string.changelog_release_version, it),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        maxLines = 1
                    )
                }
            }
        },
        navigationIcon = {
            NavigatorBackIconButton(navigateBack = navigateBack)
        },
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    )
}
