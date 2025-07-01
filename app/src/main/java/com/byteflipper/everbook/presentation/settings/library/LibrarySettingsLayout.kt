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
import com.byteflipper.everbook.presentation.core.components.dialog.CategoryDialogWithTextField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import sh.calvin.reorderable.rememberReorderableLazyListState
import androidx.compose.ui.res.stringResource
import com.byteflipper.everbook.R
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Edit

/**
 * @param onReorderModeStateChanged Callback для уведомления о текущем состоянии режима переупорядочивания
 */
@Composable
fun LibrarySettingsLayout(
    listState: LazyListState,
    paddingValues: PaddingValues,
    categories: List<Category>,
    onCreate: (String) -> Unit,
    onToggleVisibility: (Int, Boolean) -> Unit,
    onRename: (Int, String) -> Unit,
    onDelete: (Int, Int?) -> Unit,
    onSaveOrder: (List<Int>) -> Unit = {},
    onReorderModeStateChanged: (Boolean) -> Unit = {}
) {
    var dialogData by remember { mutableStateOf<Pair<Int?, String>?>(null) }
    
    var currentCategoryOrder by remember { mutableStateOf(categories.map { it.id }) }
    
    var isReorderMode by remember { mutableStateOf(false) }
    
    onReorderModeStateChanged(isReorderMode)
    
    if (currentCategoryOrder.size != categories.size) {
        currentCategoryOrder = categories.map { it.id }
    }
    
    val reorderableState = rememberReorderableLazyListState(
        lazyListState = listState,
        onMove = { from, to ->
            val adjustedFrom = from.index - 2
            val adjustedTo = to.index - 2
            
            if (adjustedFrom >= 0 && adjustedTo >= 0 && 
                adjustedFrom < currentCategoryOrder.size && 
                adjustedTo < currentCategoryOrder.size) {
                
                val mutableOrder = currentCategoryOrder.toMutableList()
                val item = mutableOrder.removeAt(adjustedFrom)
                mutableOrder.add(adjustedTo, item)
                currentCategoryOrder = mutableOrder
            }
        }
    )

    if (dialogData != null) {
        val isCreating = dialogData!!.first == null
        CategoryDialogWithTextField(
            title = stringResource(
                if (isCreating) R.string.create_category_dialog_title 
                else R.string.edit_category_dialog_title
            ),
            placeholder = stringResource(R.string.category_name_placeholder),
            icon = if (isCreating) Icons.Outlined.Add else Icons.Outlined.Edit,
            description = stringResource(
                if (isCreating) R.string.create_category_dialog_desc
                else R.string.edit_category_dialog_desc
            ),
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
            onRequestRename = { id, current -> dialogData = Pair(id, current) },
            onSaveOrder = { onSaveOrder(currentCategoryOrder) },
            reorderableState = reorderableState,
            currentOrder = currentCategoryOrder,
            isReorderMode = isReorderMode,
            onReorderModeChanged = { isReorderMode = it }
        )
    }
} 