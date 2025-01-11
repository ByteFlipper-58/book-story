package com.byteflipper.everbook.presentation.reader

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import com.byteflipper.everbook.presentation.core.util.LocalActivity
import com.byteflipper.everbook.ui.reader.ReaderEvent

@Composable
fun ReaderBackHandler(
    leave: (ReaderEvent.OnLeave) -> Unit,
    navigateBack: () -> Unit
) {
    val activity = LocalActivity.current

    BackHandler {
        leave(
            ReaderEvent.OnLeave(
                activity = activity,
                navigate = {
                    navigateBack()
                }
            )
        )
    }
}