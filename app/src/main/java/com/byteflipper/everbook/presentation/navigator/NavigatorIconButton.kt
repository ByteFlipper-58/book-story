package com.byteflipper.everbook.presentation.navigator

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.Composable
import com.byteflipper.everbook.R
import com.byteflipper.everbook.presentation.core.components.common.IconButton

@Composable
fun NavigatorIconButton(
    onClick: () -> Unit = { navigatorBottomSheetChannel.trySend(true) }
) {
    IconButton(
        icon = Icons.Default.MoreVert,
        contentDescription = R.string.more_content_desc,
        disableOnClick = false
    ) {
        onClick()
    }
}