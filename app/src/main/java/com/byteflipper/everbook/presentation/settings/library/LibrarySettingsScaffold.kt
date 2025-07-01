/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.settings.library

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.byteflipper.everbook.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibrarySettingsScaffold(onBackPressed: () -> Unit) {
    val viewModel = hiltViewModel<LibrarySettingsViewModel>()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val listState = rememberLazyListState()
    
    var showOrderSavedMessage by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    
    var isReorderMode by remember { mutableStateOf(false) }
    
    var lastCategoryOrder by remember { mutableStateOf<List<Int>?>(null) }
    
    BackHandler(enabled = isReorderMode) {
        lastCategoryOrder?.let { order ->
            viewModel.updateCategoriesOrder(order)
            showOrderSavedMessage = true
        }
    }
    
    LaunchedEffect(showOrderSavedMessage) {
        if (showOrderSavedMessage) {
            showOrderSavedMessage = false
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .windowInsetsPadding(WindowInsets.navigationBars)
            .imePadding(),
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            LibrarySettingsTopBar(
                scrollBehavior = scrollBehavior,
                navigateBack = onBackPressed
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        LibrarySettingsLayout(
            listState = listState,
            paddingValues = paddingValues,
            categories = categories,
            onCreate = { name -> viewModel.createCategory(name) },
            onRename = { id, name -> viewModel.renameCategory(id, name) },
            onDelete = { id, _ -> viewModel.deleteCategory(id) },
            onToggleVisibility = { id, visible -> viewModel.toggleCategoryVisibility(id, visible) },
            onSaveOrder = { order -> 
                lastCategoryOrder = order
                viewModel.updateCategoriesOrder(order)
                showOrderSavedMessage = true
            },
            onReorderModeStateChanged = { mode ->
                isReorderMode = mode
                if (mode) {
                    lastCategoryOrder = categories.map { it.id }
                }
            }
        )
    }
}