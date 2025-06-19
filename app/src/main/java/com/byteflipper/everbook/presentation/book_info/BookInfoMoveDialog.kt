/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.book_info

import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.DriveFileMove
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.byteflipper.everbook.R
import com.byteflipper.everbook.domain.library.book.Book
import com.byteflipper.everbook.domain.library.custom_category.Category
import com.byteflipper.everbook.presentation.core.components.dialog.Dialog
import com.byteflipper.everbook.presentation.core.components.dialog.SelectableDialogItem
import com.byteflipper.everbook.ui.book_info.BookInfoEvent

@Composable
fun BookInfoMoveDialog(
    book: Book,
    categories: List<Category>,
    actionMoveDialog: (BookInfoEvent.OnActionMoveDialog) -> Unit,
    dismissDialog: (BookInfoEvent.OnDismissDialog) -> Unit,
    navigateToLibrary: () -> Unit
) {
    val context = LocalContext.current

    val selectableCategories = remember(categories, book) {
        categories.filter { !book.categoryIds.contains(it.id) }
    }
    val selectedCategory = remember { mutableStateOf(selectableCategories.firstOrNull()) }

    Dialog(
        title = stringResource(id = R.string.move_book),
        icon = Icons.AutoMirrored.Outlined.DriveFileMove,
        description = stringResource(
            id = R.string.move_book_description
        ),
        actionEnabled = true,
        onDismiss = { dismissDialog(BookInfoEvent.OnDismissDialog) },
        onAction = {
            selectedCategory.value?.let { cat ->
            actionMoveDialog(
                BookInfoEvent.OnActionMoveDialog(
                        categoryId = cat.id,
                    context = context,
                    navigateToLibrary = navigateToLibrary
                )
            )
            }
        },
        withContent = true,
        items = {
            items(selectableCategories, key = { it.id }) { cat ->
                SelectableDialogItem(
                    selected = cat == selectedCategory.value,
                    title = cat.title.asString()
                ) {
                    selectedCategory.value = cat
                }
            }
        }
    )
}