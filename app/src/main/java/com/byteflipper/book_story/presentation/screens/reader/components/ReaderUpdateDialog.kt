package com.byteflipper.book_story.presentation.screens.reader.components

import androidx.activity.ComponentActivity
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Upgrade
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.byteflipper.book_story.R
import com.byteflipper.book_story.presentation.core.components.dialog.DialogWithContent
import com.byteflipper.book_story.presentation.core.navigation.LocalNavigator
import com.byteflipper.book_story.presentation.screens.reader.data.ReaderEvent
import com.byteflipper.book_story.presentation.screens.reader.data.ReaderViewModel

/**
 * Reader Update Dialog.
 * Navigates user to BookInfo and starts book update.
 */
@Composable
fun ReaderUpdateDialog() {
    val onEvent = ReaderViewModel.getEvent()
    val onNavigate = LocalNavigator.current
    val context = LocalContext.current

    DialogWithContent(
        title = stringResource(id = R.string.update_book),
        imageVectorIcon = Icons.Rounded.Upgrade,
        description = stringResource(
            id = R.string.update_book_description
        ),
        actionText = stringResource(id = R.string.proceed),
        isActionEnabled = true,
        onDismiss = { onEvent(ReaderEvent.OnShowHideUpdateDialog(false)) },
        onAction = {
            onEvent(
                ReaderEvent.OnUpdateText(
                    activity = context as ComponentActivity,
                    onNavigate = onNavigate
                )
            )
        },
        withDivider = false
    )
}