/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

@file:Suppress("UnusedVariable", "unused")

package com.byteflipper.everbook.ui.main

import android.annotation.SuppressLint
import android.database.CursorWindow
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.togetherWith
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.internal.immutableListOf
import com.byteflipper.everbook.R
import com.byteflipper.everbook.domain.navigator.NavigatorItem
import com.byteflipper.everbook.domain.navigator.StackEvent
import com.byteflipper.everbook.presentation.core.components.navigation_bar.NavigationBar
import com.byteflipper.everbook.presentation.core.components.navigation_rail.NavigationRail
import com.byteflipper.everbook.presentation.main.MainActivityKeyboardManager
import com.byteflipper.everbook.presentation.navigator.Navigator
import com.byteflipper.everbook.presentation.navigator.NavigatorTabs
import com.byteflipper.everbook.ui.browse.BrowseModel
import com.byteflipper.everbook.ui.browse.BrowseScreen
import com.byteflipper.everbook.ui.history.HistoryModel
import com.byteflipper.everbook.ui.history.HistoryScreen
import com.byteflipper.everbook.ui.library.LibraryModel
import com.byteflipper.everbook.ui.library.LibraryScreen
import com.byteflipper.everbook.ui.settings.SettingsModel
import com.byteflipper.everbook.ui.start.StartScreen
import com.byteflipper.everbook.ui.theme.BookStoryTheme
import com.byteflipper.everbook.ui.theme.Transitions
import com.byteflipper.everbook.domain.ui.isDark
import com.byteflipper.everbook.domain.ui.isPureDark
import java.lang.reflect.Field


@SuppressLint("DiscouragedPrivateApi")
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    // Creating an instance of Models
    private val mainModel: MainModel by viewModels()
    private val settingsModel: SettingsModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        // Splash screen
        installSplashScreen().apply {
            setKeepOnScreenCondition {
                !mainModel.isReady.value
            }
        }

        // Default super
        super.onCreate(savedInstanceState)

        // Bigger Cursor size for Room
        try {
            val field: Field = CursorWindow::class.java.getDeclaredField("sCursorWindowSize")
            field.isAccessible = true
            field.set(null, 100 * 1024 * 1024)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Initializing the MainModel
        mainModel.init(settingsModel.isReady)

        // Edge to edge
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            // Initializing Screen Models
            val libraryModel = hiltViewModel<LibraryModel>()
            val historyModel = hiltViewModel<HistoryModel>()
            val browseModel = hiltViewModel<BrowseModel>()

            val state = mainModel.state.collectAsStateWithLifecycle()
            val isLoaded = mainModel.isReady.collectAsStateWithLifecycle()

            val tabs = immutableListOf(
                NavigatorItem(
                    screen = LibraryScreen,
                    title = R.string.library_screen,
                    tooltip = R.string.library_content_desc,
                    selectedIcon = R.drawable.library_screen_filled,
                    unselectedIcon = R.drawable.library_screen_outlined
                ),
                NavigatorItem(
                    screen = HistoryScreen,
                    title = R.string.history_screen,
                    tooltip = R.string.history_content_desc,
                    selectedIcon = R.drawable.history_screen_filled,
                    unselectedIcon = R.drawable.history_screen_outlined
                ),
                NavigatorItem(
                    screen = BrowseScreen,
                    title = R.string.browse_screen,
                    tooltip = R.string.browse_content_desc,
                    selectedIcon = R.drawable.browse_screen_filled,
                    unselectedIcon = R.drawable.browse_screen_outlined
                )
            )

            MainActivityKeyboardManager()

            if (isLoaded.value) {
                BookStoryTheme(
                    theme = state.value.theme,
                    isDark = state.value.darkTheme.isDark(),
                    isPureDark = state.value.pureDark.isPureDark(this),
                    themeContrast = state.value.themeContrast
                ) {
                    Navigator(
                        initialScreen = if (state.value.showStartScreen) StartScreen
                        else LibraryScreen,
                        transitionSpec = { lastEvent ->
                            when (lastEvent) {
                                StackEvent.Default -> {
                                    Transitions.SlidingTransitionIn
                                        .togetherWith(Transitions.SlidingTransitionOut)
                                }

                                StackEvent.Pop -> {
                                    Transitions.BackSlidingTransitionIn
                                        .togetherWith(Transitions.BackSlidingTransitionOut)
                                }
                            }
                        },
                        contentKey = {
                            when (it) {
                                LibraryScreen, HistoryScreen, BrowseScreen -> "tabs"
                                else -> it
                            }
                        },
                        backHandlerEnabled = { it != StartScreen }
                    ) { screen ->
                        when (screen) {
                            LibraryScreen, HistoryScreen, BrowseScreen -> {
                                NavigatorTabs(
                                    currentTab = screen,
                                    transitionSpec = {
                                        Transitions.FadeTransitionIn
                                            .togetherWith(Transitions.FadeTransitionOut)
                                    },
                                    navigationBar = { NavigationBar(tabs = tabs) },
                                    navigationRail = { NavigationRail(tabs = tabs) }
                                ) { tab ->
                                    tab.Content()
                                }
                            }

                            else -> {
                                screen.Content()
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        cacheDir.deleteRecursively()
        super.onDestroy()
    }
}