package com.byteflipper.everbook.presentation.browse

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SdStorage
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.byteflipper.everbook.R
import com.byteflipper.everbook.presentation.core.components.dialog.Dialog
import com.byteflipper.everbook.presentation.core.util.LocalActivity
import com.byteflipper.everbook.ui.browse.BrowseEvent

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun BrowsePermissionDialog(
    storagePermissionState: PermissionState,
    actionPermissionDialog: (BrowseEvent.OnActionPermissionDialog) -> Unit,
    dismissPermissionDialog: (BrowseEvent.OnDismissPermissionDialog) -> Unit
) {
    val activity = LocalActivity.current

    Dialog(
        title = stringResource(id = R.string.storage_permission),
        imageVectorIcon = Icons.Outlined.SdStorage,
        description = stringResource(id = R.string.storage_permission_description),
        actionText = stringResource(id = R.string.grant),
        actionEnabled = true,
        disableOnClick = false,
        onDismiss = {
            dismissPermissionDialog(
                BrowseEvent.OnDismissPermissionDialog(
                    storagePermissionState = storagePermissionState
                )
            )
        },
        onAction = {
            actionPermissionDialog(
                BrowseEvent.OnActionPermissionDialog(
                    activity = activity,
                    storagePermissionState = storagePermissionState
                )
            )
        },
        withDivider = false
    )
}