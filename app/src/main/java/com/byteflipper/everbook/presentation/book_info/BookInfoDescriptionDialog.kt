package com.byteflipper.everbook.presentation.book_info

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.byteflipper.everbook.domain.library.book.Book
import com.byteflipper.everbook.presentation.core.components.dialog.DialogWithTextField
import com.byteflipper.everbook.ui.book_info.BookInfoEvent

@Composable
fun BookInfoDescriptionDialog(
    book: Book,
    actionDescriptionDialog: (BookInfoEvent.OnActionDescriptionDialog) -> Unit,
    dismissDialog: (BookInfoEvent.OnDismissDialog) -> Unit
) {
    val context = LocalContext.current
    DialogWithTextField(
        initialValue = book.description ?: "",
        lengthLimit = 5000,
        onDismiss = {
            dismissDialog(BookInfoEvent.OnDismissDialog)
        },
        onAction = {
            actionDescriptionDialog(
                BookInfoEvent.OnActionDescriptionDialog(
                    description = if (it.isBlank()) null
                    else it.trim().replace("\n", ""),
                    context = context
                )
            )
        }
    )
}