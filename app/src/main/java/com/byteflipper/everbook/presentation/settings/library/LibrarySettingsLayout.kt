/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.settings.library

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.byteflipper.everbook.domain.library.custom_category.Category
import com.byteflipper.everbook.presentation.core.components.common.LazyColumnWithScrollbar
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import com.byteflipper.everbook.presentation.core.components.dialog.DialogWithTextField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@Composable
fun LibrarySettingsLayout(
    listState: LazyListState,
    paddingValues: PaddingValues,
    categories: List<Category>,
    onCreate: (String) -> Unit,
    onToggleVisibility: (Int, Boolean) -> Unit,
    onRename: (Int, String) -> Unit,
    onDelete: (Int, Int?) -> Unit
) {
    var dialogData by remember { mutableStateOf<Pair<Int?, String>?>(null) }

    if (dialogData != null) {
        DialogWithTextField(
            initialValue = dialogData!!.second,
            onDismiss = { dialogData = null },
            onAction = { newName ->
                val id = dialogData!!.first
                if (id == null) {
                    onCreate(newName)
                } else {
                    onRename(id, newName)
                }
                dialogData = null
            }
        )
    }

    LazyColumnWithScrollbar(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = paddingValues.calculateTopPadding()),
        state = listState,
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        LibrarySettingsCategory(
            categories = categories,
            onToggleVisibility = onToggleVisibility,
            onDelete = onDelete,
            onRequestCreate = { dialogData = Pair(null, "") },
            onRequestRename = { id, current -> dialogData = Pair(id, current) }
        )
    }
} 