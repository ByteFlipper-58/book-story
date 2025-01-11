package com.byteflipper.everbook.presentation.history

import androidx.compose.runtime.Composable
import com.byteflipper.everbook.domain.util.Dialog
import com.byteflipper.everbook.ui.history.HistoryEvent
import com.byteflipper.everbook.ui.history.HistoryScreen

@Composable
fun HistoryDialog(
    dialog: Dialog?,
    actionDeleteWholeHistoryDialog: (HistoryEvent.OnActionDeleteWholeHistoryDialog) -> Unit,
    dismissDialog: (HistoryEvent.OnDismissDialog) -> Unit
) {
    when (dialog) {
        HistoryScreen.DELETE_WHOLE_HISTORY_DIALOG -> {
            HistoryDeleteWholeHistoryDialog(
                actionDeleteWholeHistoryDialog = actionDeleteWholeHistoryDialog,
                dismissDialog = dismissDialog
            )
        }
    }
}