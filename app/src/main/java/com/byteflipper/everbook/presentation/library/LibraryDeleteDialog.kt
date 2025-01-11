package com.byteflipper.everbook.presentation.library

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.byteflipper.everbook.R
import com.byteflipper.everbook.presentation.core.components.dialog.Dialog
import com.byteflipper.everbook.ui.library.LibraryEvent

@Composable
fun LibraryDeleteDialog(
    selectedItemsCount: Int,
    actionDeleteDialog: (LibraryEvent.OnActionDeleteDialog) -> Unit,
    dismissDialog: (LibraryEvent.OnDismissDialog) -> Unit
) {
    val context = LocalContext.current

    Dialog(
        title = stringResource(id = R.string.delete_books),
        imageVectorIcon = Icons.Outlined.DeleteOutline,
        description = stringResource(
            id = R.string.delete_books_description,
            selectedItemsCount
        ),
        actionText = stringResource(id = R.string.delete),
        actionEnabled = true,
        onDismiss = {
            dismissDialog(LibraryEvent.OnDismissDialog)
        },
        onAction = {
            actionDeleteDialog(
                LibraryEvent.OnActionDeleteDialog(
                    context = context
                )
            )
        },
        withDivider = false
    )
}