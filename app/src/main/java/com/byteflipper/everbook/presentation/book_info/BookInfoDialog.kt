package com.byteflipper.everbook.presentation.book_info

import androidx.compose.runtime.Composable
import com.byteflipper.everbook.domain.library.book.Book
import com.byteflipper.everbook.domain.util.Dialog
import com.byteflipper.everbook.ui.book_info.BookInfoEvent
import com.byteflipper.everbook.ui.book_info.BookInfoScreen

@Composable
fun BookInfoDialog(
    dialog: Dialog?,
    book: Book,
    actionTitleDialog: (BookInfoEvent.OnActionTitleDialog) -> Unit,
    actionAuthorDialog: (BookInfoEvent.OnActionAuthorDialog) -> Unit,
    actionDescriptionDialog: (BookInfoEvent.OnActionDescriptionDialog) -> Unit,
    actionPathDialog: (BookInfoEvent.OnActionPathDialog) -> Unit,
    actionDeleteDialog: (BookInfoEvent.OnActionDeleteDialog) -> Unit,
    actionMoveDialog: (BookInfoEvent.OnActionMoveDialog) -> Unit,
    dismissDialog: (BookInfoEvent.OnDismissDialog) -> Unit,
    navigateBack: () -> Unit,
    navigateToLibrary: () -> Unit
) {
    when (dialog) {
        BookInfoScreen.DELETE_DIALOG -> {
            BookInfoDeleteDialog(
                actionDeleteDialog = actionDeleteDialog,
                dismissDialog = dismissDialog,
                navigateBack = navigateBack
            )
        }

        BookInfoScreen.MOVE_DIALOG -> {
            BookInfoMoveDialog(
                book = book,
                actionMoveDialog = actionMoveDialog,
                dismissDialog = dismissDialog,
                navigateToLibrary = navigateToLibrary
            )
        }

        BookInfoScreen.TITLE_DIALOG -> {
            BookInfoTitleDialog(
                book = book,
                actionTitleDialog = actionTitleDialog,
                dismissDialog = dismissDialog
            )
        }

        BookInfoScreen.AUTHOR_DIALOG -> {
            BookInfoAuthorDialog(
                book = book,
                actionAuthorDialog = actionAuthorDialog,
                dismissDialog = dismissDialog
            )
        }

        BookInfoScreen.DESCRIPTION_DIALOG -> {
            BookInfoDescriptionDialog(
                book = book,
                actionDescriptionDialog = actionDescriptionDialog,
                dismissDialog = dismissDialog
            )
        }

        BookInfoScreen.PATH_DIALOG -> {
            BookInfoPathDialog(
                book = book,
                actionPathDialog = actionPathDialog,
                dismissDialog = dismissDialog
            )
        }
    }
}