/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

@file:Suppress("FunctionName")

package com.byteflipper.everbook.presentation.start

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.byteflipper.everbook.R
import com.byteflipper.everbook.presentation.settings.components.SettingsSubcategoryTitle
import com.byteflipper.everbook.ui.start.StartEvent

@OptIn(ExperimentalPermissionsApi::class)
fun LazyListScope.StartSettingsLayoutPermissions(
    activity: ComponentActivity,
    storagePermissionGranted: Boolean,
    notificationsPermissionGranted: Boolean,
    storagePermissionState: PermissionState,
    notificationsPermissionState: PermissionState,
    storagePermissionRequest: (StartEvent.OnStoragePermissionRequest) -> Unit,
    notificationsPermissionRequest: (StartEvent.OnNotificationsPermissionRequest) -> Unit
) {
    item {
        Spacer(modifier = Modifier.height(16.dp))
    }

    item {
        SettingsSubcategoryTitle(
            modifier = Modifier,
            title = stringResource(id = R.string.start_permissions_preferences),
            color = MaterialTheme.colorScheme.primary
        )
    }

    item {
        Spacer(modifier = Modifier.height(8.dp))
    }

    item {
        StartSettingsLayoutPermissionsItem(
            title = stringResource(id = R.string.start_permissions_storage),
            description = stringResource(id = R.string.start_permissions_storage_desc),
            isOptional = false,
            isGranted = storagePermissionGranted,
            onGrantClick = {
                storagePermissionRequest(
                    StartEvent.OnStoragePermissionRequest(
                        activity = activity,
                        storagePermissionState = storagePermissionState
                    )
                )
            }
        )
    }

    item {
        StartSettingsLayoutPermissionsItem(
            title = stringResource(id = R.string.start_permissions_notifications),
            description = stringResource(id = R.string.start_permissions_notifications_desc),
            isOptional = true,
            isGranted = notificationsPermissionGranted,
            onGrantClick = {
                notificationsPermissionRequest(
                    StartEvent.OnNotificationsPermissionRequest(
                        activity = activity,
                        notificationsPermissionState = notificationsPermissionState
                    )
                )
            }
        )
    }

    item {
        Spacer(modifier = Modifier.height(8.dp))
    }
}