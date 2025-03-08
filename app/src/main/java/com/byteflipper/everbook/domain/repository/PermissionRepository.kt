/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.domain.repository

import android.net.Uri
import androidx.activity.ComponentActivity
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState

interface PermissionRepository {

    suspend fun grantPersistableUriPermission(
        uri: Uri
    )

    suspend fun releasePersistableUriPermission(
        uri: Uri
    )

    @OptIn(ExperimentalPermissionsApi::class)
    suspend fun grantNotificationsPermission(
        activity: ComponentActivity,
        notificationsPermissionState: PermissionState
    ): Boolean
}