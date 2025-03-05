/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.start

import androidx.compose.runtime.Composable
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.byteflipper.everbook.domain.navigator.StackEvent
import com.byteflipper.everbook.domain.ui.ButtonItem
import com.byteflipper.everbook.presentation.core.util.LocalActivity
import com.byteflipper.everbook.ui.main.MainEvent
import com.byteflipper.everbook.ui.start.StartEvent
import com.byteflipper.everbook.ui.start.StartScreen

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun StartSettings(
    currentPage: Int,
    stackEvent: StackEvent,
    storagePermissionGranted: Boolean,
    notificationsPermissionGranted: Boolean,
    storagePermissionState: PermissionState,
    notificationsPermissionState: PermissionState,
    languages: List<ButtonItem>,
    changeLanguage: (MainEvent.OnChangeLanguage) -> Unit,
    storagePermissionRequest: (StartEvent.OnStoragePermissionRequest) -> Unit,
    notificationsPermissionRequest: (StartEvent.OnNotificationsPermissionRequest) -> Unit,
    navigateForward: () -> Unit
) {
    val activity = LocalActivity.current

    StartSettingsScaffold(
        currentPage = currentPage,
        storagePermissionGranted = storagePermissionGranted,
        navigateForward = navigateForward
    ) {
        StartContentTransition(
            targetValue = when (currentPage) {
                0 -> StartScreen.GENERAL_SETTINGS
                1 -> StartScreen.APPEARANCE_SETTINGS
                else -> StartScreen.PERMISSION_SETTINGS
            },
            stackEvent = stackEvent
        ) { page ->
            StartSettingsLayout {
                when (page) {
                    StartScreen.GENERAL_SETTINGS -> {
                        StartSettingsLayoutGeneral(
                            languages = languages,
                            changeLanguage = changeLanguage
                        )
                    }

                    StartScreen.APPEARANCE_SETTINGS -> {
                        StartSettingsLayoutAppearance()
                    }

                    StartScreen.PERMISSION_SETTINGS -> {
                        StartSettingsLayoutPermissions(
                            activity = activity,
                            storagePermissionGranted = storagePermissionGranted,
                            notificationsPermissionGranted = notificationsPermissionGranted,
                            storagePermissionState = storagePermissionState,
                            notificationsPermissionState = notificationsPermissionState,
                            storagePermissionRequest = storagePermissionRequest,
                            notificationsPermissionRequest = notificationsPermissionRequest
                        )
                    }
                }
            }
        }
    }
}