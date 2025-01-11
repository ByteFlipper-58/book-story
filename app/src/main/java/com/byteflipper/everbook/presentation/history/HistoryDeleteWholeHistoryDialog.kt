package com.byteflipper.everbook.presentation.history

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.byteflipper.everbook.R
import com.byteflipper.everbook.presentation.core.components.dialog.Dialog
import com.byteflipper.everbook.ui.history.HistoryEvent

@Composable
fun HistoryDeleteWholeHistoryDialog(
    actionDeleteWholeHistoryDialog: (HistoryEvent.OnActionDeleteWholeHistoryDialog) -> Unit,
    dismissDialog: (HistoryEvent.OnDismissDialog) -> Unit
) {
    val context = LocalContext.current
    Dialog(
        title = stringResource(id = R.string.delete_history),
        imageVectorIcon = Icons.Outlined.DeleteOutline,
        description = stringResource(id = R.string.delete_history_description),
        actionText = stringResource(id = R.string.delete),
        actionEnabled = true,
        onDismiss = {
            dismissDialog(HistoryEvent.OnDismissDialog)
        },
        onAction = {
            actionDeleteWholeHistoryDialog(
                HistoryEvent.OnActionDeleteWholeHistoryDialog(
                    context = context
                )
            )
        },
        withDivider = false
    )
}