package com.byteflipper.everbook.presentation.screens.book_info.components.dialog

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.byteflipper.everbook.R
import com.byteflipper.everbook.presentation.core.components.dialog.DialogWithContent
import com.byteflipper.everbook.presentation.screens.book_info.data.BookInfoEvent
import com.byteflipper.everbook.presentation.screens.book_info.data.BookInfoViewModel
import com.byteflipper.everbook.presentation.screens.history.data.HistoryEvent
import com.byteflipper.everbook.presentation.screens.history.data.HistoryViewModel
import com.byteflipper.everbook.presentation.screens.library.data.LibraryEvent
import com.byteflipper.everbook.presentation.screens.library.data.LibraryViewModel

/**
 * Confirm update dialog.
 * Updates book if action clicked.
 *
 * @param snackbarHostState [SnackbarHostState].
 */
@Composable
fun BookInfoConfirmUpdateDialog(snackbarHostState: SnackbarHostState) {
    val onEvent = BookInfoViewModel.getEvent()
    val onLibraryEvent = LibraryViewModel.getEvent()
    val onHistoryEvent = HistoryViewModel.getEvent()
    val context = LocalContext.current

    DialogWithContent(
        title = stringResource(id = R.string.confirm_update),
        imageVectorIcon = Icons.Default.Update,
        description = stringResource(
            id = R.string.confirm_update_description
        ),
        actionText = stringResource(id = R.string.confirm),
        isActionEnabled = true,
        onDismiss = { onEvent(BookInfoEvent.OnDismissConfirmTextUpdateDialog) },
        onAction = {
            onEvent(
                BookInfoEvent.OnConfirmTextUpdate(
                    snackbarState = snackbarHostState,
                    context = context,
                    refreshList = {
                        onLibraryEvent(LibraryEvent.OnUpdateBook(it))
                        onHistoryEvent(HistoryEvent.OnUpdateBook(it))
                    }
                )
            )
        },
        withDivider = false
    )
}