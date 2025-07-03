/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.start

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.byteflipper.everbook.domain.navigator.StackEvent
import com.byteflipper.everbook.domain.ui.ButtonItem
import com.byteflipper.everbook.ui.about.AboutEvent
import com.byteflipper.everbook.ui.about.AboutModel
import com.byteflipper.everbook.ui.main.MainEvent
import com.byteflipper.everbook.ui.start.StartScreen

@Composable
fun StartSettings(
    currentPage: Int,
    stackEvent: StackEvent,
    languages: List<ButtonItem>,
    changeLanguage: (MainEvent.OnChangeLanguage) -> Unit,
    navigateForward: () -> Unit
) {
    val aboutModel = hiltViewModel<AboutModel>()
    
    // Состояние принятия политики конфиденциальности
    val privacyAccepted = remember { mutableStateOf(false) }
    
    StartSettingsScaffold(
        currentPage = currentPage,
        nextEnabled = if (currentPage == 4) privacyAccepted.value else true,
        navigateForward = navigateForward
    ) {
        StartContentTransition(
            targetValue = when (currentPage) {
                0 -> StartScreen.GENERAL_SETTINGS
                1 -> StartScreen.APPEARANCE_SETTINGS
                2 -> StartScreen.SCAN_SETTINGS
                3 -> StartScreen.SOURCE_CODE_SETTINGS
                else -> StartScreen.PRIVACY_POLICY_SETTINGS
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

                    StartScreen.SCAN_SETTINGS -> {
                        StartSettingsLayoutScan()
                    }
                    
                    StartScreen.SOURCE_CODE_SETTINGS -> {
                        StartSettingsLayoutSourceCode(aboutModel)
                    }

                    StartScreen.PRIVACY_POLICY_SETTINGS -> {
                        StartSettingsLayoutPrivacyPolicy(
                            accepted = privacyAccepted.value,
                            onToggle = { privacyAccepted.value = it }
                        )
                    }
                }
            }
        }
    }
}