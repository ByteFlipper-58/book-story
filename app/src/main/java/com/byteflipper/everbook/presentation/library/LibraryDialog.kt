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
    actionMoveDialog: (LibraryEvent.OnActionMoveDialog) -> Unit,
    actionDeleteDialog: (LibraryEvent.OnActionDeleteDialog) -> Unit,
    dismissDialog: (LibraryEvent.OnDismissDialog) -> Unit
) {
    when (dialog) {
        LibraryScreen.MOVE_DIALOG -> {
            LibraryMoveDialog(
                books = books,
                categories = categories,
                selectedItemsCount = selectedItemsCount,
                actionMoveDialog = actionMoveDialog,
                dismissDialog = dismissDialog
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