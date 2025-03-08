/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.browse

import androidx.compose.runtime.Composable
import com.byteflipper.everbook.domain.library.book.SelectableNullableBook
import com.byteflipper.everbook.domain.util.Dialog
import com.byteflipper.everbook.ui.browse.BrowseEvent
import com.byteflipper.everbook.ui.browse.BrowseScreen

@Composable
fun BrowseDialog(
    dialog: Dialog?,
    loadingAddDialog: Boolean,
    selectedBooksAddDialog: List<SelectableNullableBook>,
    dismissAddDialog: (BrowseEvent.OnDismissAddDialog) -> Unit,
    actionAddDialog: (BrowseEvent.OnActionAddDialog) -> Unit,
    selectAddDialog: (BrowseEvent.OnSelectAddDialog) -> Unit,
    navigateToLibrary: () -> Unit
) {
    when (dialog) {
        BrowseScreen.ADD_DIALOG -> {
            BrowseAddDialog(
                loadingAddDialog = loadingAddDialog,
                selectedBooksAddDialog = selectedBooksAddDialog,
                dismissAddDialog = dismissAddDialog,
                actionAddDialog = actionAddDialog,
                selectAddDialog = selectAddDialog,
                navigateToLibrary = navigateToLibrary
            )
        }
    }
}