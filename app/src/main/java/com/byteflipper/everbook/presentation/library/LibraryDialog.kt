/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.library

import androidx.compose.runtime.Composable
import com.byteflipper.everbook.domain.library.book.SelectableBook
import com.byteflipper.everbook.domain.library.category.CategoryWithBooks
import com.byteflipper.everbook.domain.util.Dialog
import com.byteflipper.everbook.ui.library.LibraryEvent
import com.byteflipper.everbook.ui.library.LibraryScreen

@Composable
fun LibraryDialog(
    dialog: Dialog?,
    books: List<SelectableBook>,
    categories: List<CategoryWithBooks>,
    selectedItemsCount: Int,
    actionMoveDialog: (LibraryEvent.OnActionMoveDialog) -> Unit = {},
    actionSetCategoriesDialog: (LibraryEvent.OnActionSetCategoriesDialog) -> Unit,
    actionDeleteDialog: (LibraryEvent.OnActionDeleteDialog) -> Unit,
    dismissDialog: (LibraryEvent.OnDismissDialog) -> Unit
) {
    when (dialog) {
        LibraryScreen.MOVE_DIALOG -> {
            val selectedBooks = books.filter { it.selected }

            val initialIds = if (selectedItemsCount == 0) {
                emptyList()
            } else if (selectedItemsCount == 1) {
                selectedBooks.first().data.categoryIds.filter { it != 0 }
            } else {
                selectedBooks
                    .map { it.data.categoryIds.filter { id -> id != 0 }.toSet() }
                    .reduce { acc, set -> acc intersect set }
                    .toList()
            }

            LibraryCategoriesDialog(
                categories = categories,
                selectedBooksCount = selectedItemsCount,
                initialSelectedIds = initialIds,
                onAction = actionSetCategoriesDialog,
                onDismiss = { dismissDialog(LibraryEvent.OnDismissDialog) }
            )
        }

        LibraryScreen.DELETE_DIALOG -> {
            LibraryDeleteDialog(
                selectedItemsCount = selectedItemsCount,
                actionDeleteDialog = actionDeleteDialog,
                dismissDialog = dismissDialog
            )
        }
    }
}