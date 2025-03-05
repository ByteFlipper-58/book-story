/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.domain.use_case.permission

import androidx.activity.ComponentActivity
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.byteflipper.everbook.domain.repository.PermissionRepository
import javax.inject.Inject

class GrantNotificationsPermission @Inject constructor(
    private val repository: PermissionRepository
) {

    @OptIn(ExperimentalPermissionsApi::class)
    suspend fun execute(
        activity: ComponentActivity,
        notificationsPermissionState: PermissionState
    ): Boolean {
        return repository.grantNotificationsPermission(
            activity = activity,
            notificationsPermissionState = notificationsPermissionState
        )
    }
}