/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.settings.general.components

import android.Manifest
import android.annotation.SuppressLint
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Security
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.byteflipper.everbook.R
import com.byteflipper.everbook.presentation.core.components.dialog.Dialog
import com.byteflipper.everbook.presentation.core.components.settings.SwitchWithTitle
import com.byteflipper.everbook.presentation.core.util.LocalActivity
import com.byteflipper.everbook.ui.main.MainEvent
import com.byteflipper.everbook.ui.main.MainModel
import com.byteflipper.everbook.ui.settings.SettingsEvent
import com.byteflipper.everbook.ui.settings.SettingsModel

@OptIn(ExperimentalPermissionsApi::class)
@SuppressLint("InlinedApi")
@Composable
fun CheckForUpdatesOption() {
    val mainModel = hiltViewModel<MainModel>()
    val settingsModel = hiltViewModel<SettingsModel>()

    val state = mainModel.state.collectAsStateWithLifecycle()
    val activity = LocalActivity.current
    val notificationsPermissionState = rememberPermissionState(
        permission = Manifest.permission.POST_NOTIFICATIONS
    )
    val showConfirmation = remember { mutableStateOf(false) }

    if (showConfirmation.value) {
        Dialog(
            title = stringResource(id = R.string.enable_check_for_updates),
            description = stringResource(id = R.string.enable_check_for_updates_description),
            icon = Icons.Default.Security,
            actionEnabled = true,
            onDismiss = { showConfirmation.value = false },
            onAction = {
                mainModel.onEvent(MainEvent.OnChangeCheckForUpdates(true))
                showConfirmation.value = false
            },
            withContent = false
        )
    }

    SwitchWithTitle(
        selected = state.value.checkForUpdates,
        title = stringResource(id = R.string.check_for_updates_option),
        description = stringResource(id = R.string.check_for_updates_option_desc)
    ) {
        settingsModel.onEvent(
            SettingsEvent.OnChangeCheckForUpdates(
                enable = !state.value.checkForUpdates,
                activity = activity,
                notificationsPermissionState = notificationsPermissionState,
                onChangeCheckForUpdates = {
                    when (it) {
                        true -> showConfirmation.value = true
                        false -> mainModel.onEvent(MainEvent.OnChangeCheckForUpdates(false))
                    }
                }
            )
        )
    }
}