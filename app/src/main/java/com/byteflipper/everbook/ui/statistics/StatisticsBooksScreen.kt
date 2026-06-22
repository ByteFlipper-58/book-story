/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.ui.statistics

import android.os.Parcelable
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.byteflipper.everbook.domain.navigator.Screen
import com.byteflipper.everbook.presentation.core.components.top_bar.collapsibleTopAppBarScrollBehavior
import com.byteflipper.everbook.presentation.navigator.LocalNavigator
import com.byteflipper.everbook.presentation.statistics.StatisticsBooksContent
import com.byteflipper.everbook.ui.book_info.BookInfoScreen
import kotlinx.parcelize.Parcelize

@Parcelize
object StatisticsBooksScreen : Screen, Parcelable {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val screenModel = hiltViewModel<StatisticsModel>()
        val state = screenModel.state.collectAsStateWithLifecycle()
        val (scrollBehavior, listState) = TopAppBarDefaults.collapsibleTopAppBarScrollBehavior()

        LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
            screenModel.refreshStatistics()
        }

        StatisticsBooksContent(
            listState = listState,
            scrollBehavior = scrollBehavior,
            isLoading = state.value.isLoading,
            perBook = state.value.statistics.perBook,
            navigateToBookInfo = { navigator.push(BookInfoScreen(bookId = it)) },
            navigateBack = { navigator.pop() }
        )
    }
}
