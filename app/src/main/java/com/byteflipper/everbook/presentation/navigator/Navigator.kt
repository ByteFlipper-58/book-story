/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.navigator

import androidx.activity.compose.BackHandler
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.lifecycle.withCreationCallback
import com.byteflipper.everbook.domain.navigator.Navigator
import com.byteflipper.everbook.domain.navigator.Screen
import com.byteflipper.everbook.domain.navigator.StackEvent
import com.byteflipper.everbook.presentation.core.util.LocalActivity
import kotlinx.coroutines.Dispatchers

@Composable
fun rememberNavigator(initialScreen: Screen): Navigator {
    val activity = LocalActivity.current
    return activity.viewModels<Navigator>(
        extrasProducer = {
            activity.defaultViewModelCreationExtras
                .withCreationCallback<Navigator.Factory> { factory ->
                    factory.create(initialScreen)
                }
        }
    ).value
}

@Composable
fun Navigator(
    modifier: Modifier = Modifier,
    initialScreen: Screen,
    transitionSpec: AnimatedContentTransitionScope<Screen>.(lastEvent: StackEvent) -> ContentTransform,
    contentKey: (Screen) -> Any? = { it },
    backHandlerEnabled: (Screen) -> Boolean = { true },
    content: @Composable (currentScreen: Screen) -> Unit
) {
    val navigator = rememberNavigator(initialScreen = initialScreen)
    val currentScreen = navigator.lastItem.collectAsStateWithLifecycle(
        context = Dispatchers.Main.immediate
    )
    val lastEvent = navigator.lastEvent.collectAsStateWithLifecycle(
        context = Dispatchers.Main.immediate
    )

    CompositionLocalProvider(LocalNavigator provides navigator) {
        AnimatedContent(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface),
            targetState = currentScreen.value,
            transitionSpec = {
                transitionSpec(this, lastEvent.value)
            },
            contentKey = contentKey,
            content = { content(it) }
        )
    }

    BackHandler(enabled = backHandlerEnabled.invoke(currentScreen.value)) {
        navigator.pop()
    }
}