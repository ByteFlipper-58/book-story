/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.ui.start

import android.Manifest
import android.annotation.SuppressLint
import android.os.Parcelable
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.text.TextStyle
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import com.byteflipper.everbook.domain.navigator.Screen
import com.byteflipper.everbook.domain.navigator.StackEvent
import com.byteflipper.everbook.domain.ui.ButtonItem
import com.byteflipper.everbook.presentation.core.constants.Constants
import com.byteflipper.everbook.presentation.core.constants.provideLanguages
import com.byteflipper.everbook.presentation.core.util.LocalActivity
import com.byteflipper.everbook.presentation.navigator.LocalNavigator
import com.byteflipper.everbook.presentation.start.StartContent
import com.byteflipper.everbook.ui.about.AboutModel
import com.byteflipper.everbook.ui.browse.BrowseScreen
import com.byteflipper.everbook.ui.help.HelpScreen
import com.byteflipper.everbook.ui.main.MainEvent
import com.byteflipper.everbook.ui.main.MainModel

@Parcelize
object StartScreen : Screen, Parcelable {

    @IgnoredOnParcel
    const val SETTINGS = "settings"

    @IgnoredOnParcel
    const val GENERAL_SETTINGS = "general_settings"

    @IgnoredOnParcel
    const val APPEARANCE_SETTINGS = "appearance_settings"

    @IgnoredOnParcel
    const val PERMISSION_SETTINGS = "permission_settings"

    @IgnoredOnParcel
    const val SOURCE_CODE = "source_code"

    @IgnoredOnParcel
    const val DONE = "done"

    @SuppressLint("InlinedApi")
    @OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val screenModel = hiltViewModel<StartModel>()
        val mainModel = hiltViewModel<MainModel>()
        val aboutModel = hiltViewModel<AboutModel>()

        val state = screenModel.state.collectAsStateWithLifecycle()
        val mainState = mainModel.state.collectAsStateWithLifecycle()

        val activity = LocalActivity.current
        val storagePermissionState = rememberPermissionState(
            permission = Manifest.permission.READ_EXTERNAL_STORAGE
        )
        val notificationsPermissionState = rememberPermissionState(
            permission = Manifest.permission.POST_NOTIFICATIONS
        )

        val currentPage = remember { mutableIntStateOf(0) }
        val stackEvent = remember { mutableStateOf(StackEvent.Default) }

        val languages = remember(mainState.value.language) {
            Constants.provideLanguages().sortedBy { it.second }.map {
                ButtonItem(
                    id = it.first,
                    title = it.second,
                    textStyle = TextStyle(),
                    selected = it.first == mainState.value.language
                )
            }.sortedBy { it.title }
        }

        LaunchedEffect(Unit) {
            screenModel.onEvent(
                StartEvent.OnCheckPermissions(
                    storagePermissionState = storagePermissionState,
                    notificationsPermissionState = notificationsPermissionState
                )
            )
        }
        DisposableEffect(Unit) {
            onDispose { screenModel.resetScreen() }
        }

        StartContent(
            currentPage = currentPage.intValue,
            stackEvent = stackEvent.value,
            storagePermissionGranted = state.value.storagePermissionGranted,
            notificationsPermissionGranted = state.value.notificationsPermissionGranted,
            storagePermissionState = storagePermissionState,
            notificationsPermissionState = notificationsPermissionState,
            languages = languages,
            changeLanguage = mainModel::onEvent,
            storagePermissionRequest = screenModel::onEvent,
            notificationsPermissionRequest = screenModel::onEvent,
            navigateForward = {
                if ((currentPage.intValue + 1 == 4) && !state.value.storagePermissionGranted) {
                    return@StartContent
                }

                stackEvent.value = StackEvent.Default
                currentPage.intValue += 1
            },
            navigateBack = {
                if ((currentPage.intValue - 1) < 0) {
                    activity.finish()
                } else {
                    stackEvent.value = StackEvent.Pop
                    currentPage.intValue -= 1
                }
            },
            navigateToBrowse = {
                navigator.push(
                    BrowseScreen,
                    saveInBackStack = false
                )
                BrowseScreen.refreshListChannel.trySend(Unit)
                mainModel.onEvent(MainEvent.OnChangeShowStartScreen(false))
            },
            navigateToHelp = {
                navigator.push(
                    HelpScreen(fromStart = true),
                    saveInBackStack = false
                )
            },
            navigateToBrowserPage = aboutModel::onEvent
        )
    }
}