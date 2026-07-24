/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)

package com.byteflipper.everbook.presentation.translation.language_selection
import androidx.compose.ui.res.painterResource

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.byteflipper.everbook.R
import com.byteflipper.everbook.domain.translation.TranslationProviderMode
import com.byteflipper.everbook.presentation.core.components.common.LazyColumnWithScrollbar
import com.byteflipper.everbook.presentation.core.components.common.SearchTextField
import com.byteflipper.everbook.presentation.core.components.common.StyledText

@Composable
internal fun TranslationLanguageSelectionList(
    providerMode: TranslationProviderMode,
    modelManagerAvailable: Boolean,
    errorMessage: String?,
    items: List<TranslationLanguageSelectionItem>,
    query: String,
    onQueryChange: (String) -> Unit,
    listState: LazyListState,
    topPadding: Dp,
    onItemClick: (TranslationLanguageSelectionItem) -> Unit
) {
    LazyColumnWithScrollbar(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = topPadding),
        state = listState,
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        stickyHeader {
            TranslationLanguageSearchHeader {
                TranslationLanguageSearch(
                    query = query,
                    onQueryChange = onQueryChange
                )
            }
        }

        errorMessage?.let { error ->
            item {
                TranslationLanguageError(error = error)
            }
        }

        if (providerMode == TranslationProviderMode.IN_APP && !modelManagerAvailable) {
            item {
                TranslationLanguageNote(
                    text = stringResource(id = R.string.translation_offline_models_unavailable)
                )
            }
        } else if (items.isEmpty()) {
            item {
                TranslationLanguageNote(
                    text = stringResource(id = R.string.translation_models_empty)
                )
            }
        } else {
            items(
                items = items,
                key = { "${it.code}_${it.downloadLanguageCode}" }
            ) { item ->
                TranslationLanguageSelectionItemRow(
                    item = item,
                    onClick = { onItemClick(item) }
                )
            }
        }
    }
}

@Composable
private fun TranslationLanguageSearchHeader(
    content: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface
    ) {
        content()
    }
}

@Composable
private fun TranslationLanguageSearch(
    query: String,
    onQueryChange: (String) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 8.dp),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainerHighest
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_search_rounded_24px),
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(12.dp))
            SearchTextField(
                modifier = Modifier.weight(1f),
                initialQuery = query,
                onQueryChange = onQueryChange,
                onSearch = {}
            )
        }
    }
}

@Composable
private fun TranslationLanguageError(error: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 8.dp),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.errorContainer
    ) {
        StyledText(
            text = error,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            style = MaterialTheme.typography.bodySmall.copy(
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        )
    }
}

@Composable
private fun TranslationLanguageNote(text: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StyledText(
            text = text,
            style = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }
}
