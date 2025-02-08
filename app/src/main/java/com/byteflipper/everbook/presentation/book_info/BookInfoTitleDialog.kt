package com.byteflipper.everbook.presentation.book_info

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.byteflipper.everbook.domain.library.book.Book
import com.byteflipper.everbook.presentation.core.components.dialog.DialogWithTextField
import com.byteflipper.everbook.ui.book_info.BookInfoEvent

@Composable
fun BookInfoTitleDialog(
    book: Book,
    actionTitleDialog: (BookInfoEvent.OnActionTitleDialog) -> Unit,
    dismissDialog: (BookInfoEvent.OnDismissDialog) -> Unit
) {
    val context = LocalContext.current
    DialogWithTextField(
        initialValue = book.title,
        lengthLimit = 100,
        onDismiss = {
            dismissDialog(BookInfoEvent.OnDismissDialog)
        },
        onAction = {
            if (it.isBlank()) return@DialogWithTextField
            actionTitleDialog(
                BookInfoEvent.OnActionTitleDialog(
                    title = it.trim().replace("\n", ""),
                    context = context
                )
            )
        }
    )
}