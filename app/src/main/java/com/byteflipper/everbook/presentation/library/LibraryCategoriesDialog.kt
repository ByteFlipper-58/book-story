/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.library

import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material3.Checkbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.byteflipper.everbook.R
import com.byteflipper.everbook.domain.library.category.CategoryWithBooks
import com.byteflipper.everbook.presentation.core.components.dialog.Dialog
import com.byteflipper.everbook.presentation.core.components.common.StyledText
import com.byteflipper.everbook.ui.library.LibraryEvent
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.clickable

@Composable
fun LibraryCategoriesDialog(
    categories: List<CategoryWithBooks>,
    selectedBooksCount: Int,
    initialSelectedIds: List<Int> = emptyList(),
    onAction: (LibraryEvent.OnActionSetCategoriesDialog) -> Unit,
    onDismiss: () -> Unit
) {
    val selectedIds = remember { mutableStateListOf<Int>().apply { addAll(initialSelectedIds.filter { it != 0 }) } }

    Dialog(
        title = stringResource(id = R.string.choose_categories),
        icon = Icons.Outlined.Category,
        description = stringResource(id = R.string.move_books_description, selectedBooksCount),
        actionEnabled = true,
        onDismiss = onDismiss,
        onAction = {
            onAction(
                LibraryEvent.OnActionSetCategoriesDialog(selectedIds.toList())
            )
        },
        withContent = true,
        items = {
            items(categories.filter { it.id != 0 }) { cat ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (selectedIds.contains(cat.id)) selectedIds.remove(cat.id)
                            else selectedIds.add(cat.id)
                        }
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(checked = selectedIds.contains(cat.id), onCheckedChange = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    StyledText(text = cat.title.asString())
                }
            }
        }
    )
} 