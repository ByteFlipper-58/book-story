package com.byteflipper.everbook.presentation.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.byteflipper.everbook.R
import com.byteflipper.everbook.domain.ui.UIText
import com.byteflipper.everbook.presentation.core.components.placeholder.ErrorPlaceholder
import com.byteflipper.everbook.presentation.core.util.LocalActivity
import com.byteflipper.everbook.ui.reader.ReaderEvent

@Composable
fun ReaderErrorPlaceholder(
    errorMessage: UIText,
    leave: (ReaderEvent.OnLeave) -> Unit,
    navigateBack: () -> Unit
) {
    val activity = LocalActivity.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {
        ErrorPlaceholder(
            errorMessage = errorMessage.asString(),
            icon = painterResource(id = R.drawable.error),
            actionTitle = stringResource(id = R.string.go_back),
            action = {
                leave(
                    ReaderEvent.OnLeave(
                        activity = activity,
                        navigate = {
                            navigateBack()
                        }
                    )
                )
            }
        )
    }
}