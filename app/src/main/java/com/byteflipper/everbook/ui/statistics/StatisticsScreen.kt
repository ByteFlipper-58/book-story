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
import androidx.lifecycle.Lifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.parcelize.Parcelize
import com.byteflipper.everbook.domain.navigator.Screen
import com.byteflipper.everbook.presentation.core.components.top_bar.collapsibleTopAppBarScrollBehavior
import com.byteflipper.everbook.presentation.navigator.LocalNavigator
import com.byteflipper.everbook.presentation.statistics.StatisticsContent
import com.byteflipper.everbook.ui.book_info.BookInfoScreen

@Parcelize
object StatisticsScreen : Screen, Parcelable {

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

        StatisticsContent(
            listState = listState,
            scrollBehavior = scrollBehavior,
            state = state.value,
            onChangeRange = screenModel::onEvent,
            onSetGoal = screenModel::onEvent,
            onToggleSection = screenModel::onEvent,
            onReorderSections = screenModel::onEvent,
            onToggleMetric = screenModel::onEvent,
            onReorderMetrics = screenModel::onEvent,
            onSetWeekStart = screenModel::onEvent,
            onResetLayout = screenModel::onEvent,
            onResetMetrics = screenModel::onEvent,
            navigateToBookInfo = { navigator.push(BookInfoScreen(bookId = it)) },
            navigateToAllBooks = { navigator.push(StatisticsBooksScreen) },
            navigateToActivity = { navigator.push(ActivityScreen) },
            navigateBack = { navigator.pop() }
        )
    }
}
