/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.ui.changelog

import android.os.Parcelable
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.parcelize.Parcelize
import com.byteflipper.everbook.domain.navigator.Screen
import com.byteflipper.everbook.presentation.core.components.top_bar.collapsibleTopAppBarScrollBehavior
import com.byteflipper.everbook.presentation.navigator.LocalNavigator
import com.byteflipper.everbook.presentation.changelog.ChangelogContent
import com.byteflipper.everbook.ui.main.MainModel

@Parcelize
data class ChangelogScreen(
    val initialVersionCode: Int? = null
) : Screen, Parcelable {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val mainModel = hiltViewModel<MainModel>()
        val screenModel = hiltViewModel<ChangelogModel>()
        val mainState = mainModel.state.collectAsStateWithLifecycle()
        val state = screenModel.state.collectAsStateWithLifecycle()

        val (scrollBehavior, listState) = TopAppBarDefaults.collapsibleTopAppBarScrollBehavior()

        LaunchedEffect(mainState.value.language, initialVersionCode) {
            screenModel.load(
                language = mainState.value.language,
                initialVersionCode = initialVersionCode
            )
        }

        ChangelogContent(
            state = state.value,
            listState = listState,
            scrollBehavior = scrollBehavior,
            onEvent = screenModel::onEvent,
            navigateBack = {
                navigator.pop()
            }
        )
    }
}
