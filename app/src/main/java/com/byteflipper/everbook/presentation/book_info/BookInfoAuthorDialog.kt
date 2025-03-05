/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.book_info

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.byteflipper.everbook.R
import com.byteflipper.everbook.domain.library.book.Book
import com.byteflipper.everbook.domain.ui.UIText
import com.byteflipper.everbook.presentation.core.components.dialog.DialogWithTextField
import com.byteflipper.everbook.ui.book_info.BookInfoEvent

@Composable
fun BookInfoAuthorDialog(
    book: Book,
    actionAuthorDialog: (BookInfoEvent.OnActionAuthorDialog) -> Unit,
    dismissDialog: (BookInfoEvent.OnDismissDialog) -> Unit
) {
    val context = LocalContext.current
    DialogWithTextField(
        initialValue = book.author.getAsString() ?: "",
        lengthLimit = 100,
        onDismiss = {
            dismissDialog(BookInfoEvent.OnDismissDialog)
        },
        onAction = {
            actionAuthorDialog(
                BookInfoEvent.OnActionAuthorDialog(
                    author = if (it.isBlank()) UIText.StringResource(R.string.unknown_author)
                    else UIText.StringValue(it.trim().replace("\n", "")),
                    context = context
                )
            )
        }
    )
}