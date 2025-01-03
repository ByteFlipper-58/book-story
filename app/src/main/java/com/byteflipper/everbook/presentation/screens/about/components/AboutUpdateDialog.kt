package com.byteflipper.everbook.presentation.screens.about.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Update
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.byteflipper.everbook.R
import com.byteflipper.everbook.presentation.core.components.dialog.DialogWithContent
import com.byteflipper.everbook.presentation.core.constants.Constants
import com.byteflipper.everbook.presentation.core.constants.provideReleasesPage
import com.byteflipper.everbook.presentation.core.util.showToast
import com.byteflipper.everbook.presentation.screens.about.data.AboutEvent
import com.byteflipper.everbook.presentation.screens.about.data.AboutViewModel

/**
 * Update dialog. If there is app update transfers to the download page.
 */
@Composable
fun AboutUpdateDialog() {
    val state = AboutViewModel.getState()
    val onEvent = AboutViewModel.getEvent()
    val context = LocalContext.current

    val update = remember(state.value.showUpdateDialog) {
        state.value.updateInfo!!
    }

    DialogWithContent(
        title = stringResource(id = R.string.update_query, update.tagName),
        imageVectorIcon = Icons.Default.Update,
        description = stringResource(
            id = R.string.update_app_description
        ),
        actionText = stringResource(id = R.string.proceed),
        isActionEnabled = true,
        disableOnClick = false,
        onDismiss = { onEvent(AboutEvent.OnDismissUpdateDialog) },
        onAction = {
            onEvent(
                AboutEvent.OnNavigateToBrowserPage(
                    page = Constants.provideReleasesPage(),
                    context = context,
                    noAppsFound = {
                        context.getString(R.string.error_no_browser).showToast(
                            context = context,
                            longToast = false
                        )
                    }
                )
            )
            onEvent(AboutEvent.OnDismissUpdateDialog)
        },
        withDivider = false,
    )
}