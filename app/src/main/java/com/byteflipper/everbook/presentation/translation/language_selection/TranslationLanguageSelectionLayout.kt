/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.byteflipper.everbook.presentation.translation.language_selection

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import com.byteflipper.everbook.domain.translation.TranslationProviderMode
import com.byteflipper.everbook.ui.settings.TranslatorSettingsState
import java.util.Locale

@Composable
fun TranslationLanguageSelectionLayout(
    role: TranslationLanguageSelectionRole,
    providerMode: TranslationProviderMode,
    selectedLanguageCode: String,
    requireWifi: Boolean,
    translatorState: TranslatorSettingsState,
    navigateBack: () -> Unit,
    onRefreshModels: () -> Unit,
    onDownloadModel: (String) -> Unit,
    onSelectLanguage: (String) -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val listState = rememberLazyListState()
    var query by remember { mutableStateOf("") }
    var pendingSelection by remember { mutableStateOf<PendingLanguageSelection?>(null) }
    val items = translationLanguageSelectionItems(
        role = role,
        providerMode = providerMode,
        selectedLanguageCode = selectedLanguageCode,
        requireWifi = requireWifi,
        translatorState = translatorState
    )
    val filteredItems = remember(query, items) {
        val normalizedQuery = query.trim().lowercase(Locale.ROOT)
        if (normalizedQuery.isBlank()) {
            items
        } else {
            items.filter {
                it.code.lowercase(Locale.ROOT).contains(normalizedQuery) ||
                        it.title.lowercase(Locale.ROOT).contains(normalizedQuery) ||
                        it.subtitle.orEmpty().lowercase(Locale.ROOT).contains(normalizedQuery)
            }
        }
    }

    LaunchedEffect(pendingSelection, translatorState.models, translatorState.busyLanguageCodes) {
        val pending = pendingSelection ?: return@LaunchedEffect
        val model = translatorState.models.firstOrNull {
            it.language.code == pending.downloadLanguageCode
        }
        if (model?.downloaded == true) {
            onSelectLanguage(pending.selectedLanguageCode)
            pendingSelection = null
            navigateBack()
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
            TranslationLanguageSelectionTopBar(
                role = role,
                providerMode = providerMode,
                modelManagerAvailable = translatorState.modelManagerAvailable,
                isLoadingModels = translatorState.isLoadingModels,
                scrollBehavior = scrollBehavior,
                navigateBack = navigateBack,
                refreshModels = onRefreshModels
            )
        }
    ) { paddingValues ->
        TranslationLanguageSelectionList(
            providerMode = providerMode,
            modelManagerAvailable = translatorState.modelManagerAvailable,
            errorMessage = translatorState.errorMessage,
            items = filteredItems,
            query = query,
            onQueryChange = { query = it },
            listState = listState,
            topPadding = paddingValues.calculateTopPadding(),
            onItemClick = { item ->
                if (providerMode == TranslationProviderMode.IN_APP &&
                    item.downloadLanguageCode != null &&
                    item.downloaded == false
                ) {
                    pendingSelection = PendingLanguageSelection(
                        selectedLanguageCode = item.code,
                        downloadLanguageCode = item.downloadLanguageCode
                    )
                    onDownloadModel(item.downloadLanguageCode)
                } else {
                    onSelectLanguage(item.code)
                    navigateBack()
                }
            }
        )
    }
}
