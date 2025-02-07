package com.byteflipper.everbook.presentation.about

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Update
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.byteflipper.everbook.R
import com.byteflipper.everbook.data.remote.dto.LatestReleaseInfo
import com.byteflipper.everbook.presentation.core.components.dialog.Dialog
import com.byteflipper.everbook.presentation.core.constants.Constants
import com.byteflipper.everbook.presentation.core.constants.provideReleasesPage
import com.byteflipper.everbook.ui.about.AboutEvent

@Composable
fun AboutUpdateDialog(
    updateInfo: LatestReleaseInfo?,
    navigateToBrowserPage: (AboutEvent.OnNavigateToBrowserPage) -> Unit,
    dismissDialog: (AboutEvent.OnDismissDialog) -> Unit
) {
    val context = LocalContext.current

    Dialog(
        title = stringResource(id = R.string.update_query, updateInfo?.tagName ?: ""),
        icon = Icons.Default.Update,
        description = stringResource(
            id = R.string.update_app_description
        ),
        actionEnabled = true,
        disableOnClick = false,
        onDismiss = { dismissDialog(AboutEvent.OnDismissDialog) },
        onAction = {
            navigateToBrowserPage(
                AboutEvent.OnNavigateToBrowserPage(
                    page = Constants.provideReleasesPage(),
                    context = context
                )
            )
            dismissDialog(AboutEvent.OnDismissDialog)
        },
        withContent = false
    )
}