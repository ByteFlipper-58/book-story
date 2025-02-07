package com.byteflipper.everbook.presentation.book_info

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.byteflipper.everbook.R
import com.byteflipper.everbook.presentation.core.components.dialog.Dialog
import com.byteflipper.everbook.ui.book_info.BookInfoEvent

@Composable
fun BookInfoDeleteDialog(
    actionDeleteDialog: (BookInfoEvent.OnActionDeleteDialog) -> Unit,
    dismissDialog: (BookInfoEvent.OnDismissDialog) -> Unit,
    navigateBack: () -> Unit
) {
    val context = LocalContext.current

    Dialog(
        title = stringResource(id = R.string.delete_book),
        icon = Icons.Outlined.DeleteOutline,
        description = stringResource(
            id = R.string.delete_book_description
        ),
        onDismiss = { dismissDialog(BookInfoEvent.OnDismissDialog) },
        withContent = false,
        actionEnabled = true,
        onAction = {
            actionDeleteDialog(
                BookInfoEvent.OnActionDeleteDialog(
                    context = context,
                    navigateBack = navigateBack
                )
            )
        }
    )
}