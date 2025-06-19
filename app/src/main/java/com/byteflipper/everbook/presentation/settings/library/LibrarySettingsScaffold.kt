/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.settings.library

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.byteflipper.everbook.domain.library.custom_category.Category
import com.byteflipper.everbook.presentation.navigator.NavigatorBackIconButton
import com.byteflipper.everbook.presentation.core.components.common.StyledText
import androidx.compose.ui.res.stringResource
import com.byteflipper.everbook.R
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.ui.input.nestedscroll.nestedScroll

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibrarySettingsScaffold(
    scrollBehavior: TopAppBarScrollBehavior,
    listState: LazyListState,
    categories: List<Category>,
    navigateBack: () -> Unit,
    onCreate: (String) -> Unit,
    onToggleVisibility: (Int, Boolean) -> Unit,
    onRename: (Int, String) -> Unit,
    onDelete: (Int, Int?) -> Unit
) {
    Scaffold(
        Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            LargeTopAppBar(
                title = { StyledText(stringResource(id = R.string.categories_settings_title)) },
                navigationIcon = { NavigatorBackIconButton(navigateBack = navigateBack) },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        }
    ) { padding ->
        LibrarySettingsLayout(
            listState = listState,
            paddingValues = padding,
            categories = categories,
            onCreate = onCreate,
            onToggleVisibility = onToggleVisibility,
            onRename = onRename,
            onDelete = onDelete
        )
    }
} 