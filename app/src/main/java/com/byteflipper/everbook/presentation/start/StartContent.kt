/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.start

import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.byteflipper.everbook.domain.navigator.StackEvent
import com.byteflipper.everbook.domain.ui.ButtonItem
import com.byteflipper.everbook.ui.about.AboutEvent
import com.byteflipper.everbook.ui.main.MainEvent
import com.byteflipper.everbook.ui.start.StartEvent
import com.byteflipper.everbook.ui.start.StartScreen

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun StartContent(
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
    navigateForward: () -> Unit,
    navigateBack: () -> Unit,
    navigateToBrowse: () -> Unit,
    navigateToHelp: () -> Unit,
    navigateToBrowserPage: (AboutEvent.OnNavigateToBrowserPage) -> Unit,
    ) {
    StartContentTransition(
        modifier = Modifier.background(MaterialTheme.colorScheme.surface),
        targetValue = when {
            currentPage in 0..2 -> StartScreen.SETTINGS
            currentPage == 3 -> StartScreen.SOURCE_CODE
            else -> StartScreen.DONE
        },
        stackEvent = stackEvent
    ) { page ->
        when (page) {
            StartScreen.SETTINGS -> {
                StartSettings(
                    currentPage = currentPage,
                    stackEvent = stackEvent,
                    storagePermissionGranted = storagePermissionGranted,
                    notificationsPermissionGranted = notificationsPermissionGranted,
                    storagePermissionState = storagePermissionState,
                    notificationsPermissionState = notificationsPermissionState,
                    languages = languages,
                    changeLanguage = changeLanguage,
                    storagePermissionRequest = storagePermissionRequest,
                    notificationsPermissionRequest = notificationsPermissionRequest,
                    navigateForward = navigateForward
                )
            }
            StartScreen.SOURCE_CODE -> {
                StartSourceCode(
                    navigateForward = navigateForward,
                    navigateToBrowserPage = navigateToBrowserPage
                )
            }
            StartScreen.DONE -> {
                StartDone(
                    navigateToBrowse = navigateToBrowse,
                    navigateToHelp = navigateToHelp
                )
            }
        }
    }

    StartBackHandler(
        navigateBack = navigateBack
    )
}