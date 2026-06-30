/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.ui.statistics

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.byteflipper.everbook.domain.statistics.decodeStatMetrics
import com.byteflipper.everbook.domain.statistics.decodeStatSections
import com.byteflipper.everbook.domain.statistics.decodeStatisticsRange
import com.byteflipper.everbook.domain.statistics.defaultStatMetrics
import com.byteflipper.everbook.domain.statistics.defaultStatSections
import com.byteflipper.everbook.domain.statistics.encode
import com.byteflipper.everbook.domain.statistics.encodeMetrics
import com.byteflipper.everbook.domain.statistics.StatisticsRange
import com.byteflipper.everbook.domain.use_case.data_store.GetDatastore
import com.byteflipper.everbook.domain.use_case.data_store.SetDatastore
import com.byteflipper.everbook.domain.use_case.statistics.GetReadingStatistics
import com.byteflipper.everbook.presentation.core.constants.DataStoreConstants
import javax.inject.Inject

@HiltViewModel
class StatisticsModel @Inject constructor(
    private val getReadingStatistics: GetReadingStatistics,
    private val getDatastore: GetDatastore,
    private val setDatastore: SetDatastore
) : ViewModel() {

    private val _state = MutableStateFlow(StatisticsState())
    val state = _state.asStateFlow()

    init {
        load()
    }

    fun onEvent(event: StatisticsEvent) {
        when (event) {
            is StatisticsEvent.OnChangeRange -> {
                _state.update { it.copy(statisticsRange = event.range) }
                persist(DataStoreConstants.STATISTICS_RANGE, event.range.name)
                reloadStatistics(
                    weekStartMonday = _state.value.weekStartMonday,
                    range = event.range
                )
            }

            is StatisticsEvent.OnSetGoal -> {
                val minutes = event.minutes.coerceIn(GOAL_MIN, GOAL_MAX)
                _state.update { it.copy(goalMinutes = minutes) }
                persist(DataStoreConstants.READING_GOAL_MINUTES, minutes)
            }

            is StatisticsEvent.OnToggleSection -> {
                val updated = _state.value.sections.map {
                    if (it.section == event.section) it.copy(visible = !it.visible) else it
                }
                _state.update { it.copy(sections = updated) }
                persist(DataStoreConstants.STATISTICS_SECTIONS, updated.encode())
            }

            is StatisticsEvent.OnReorderSections -> {
                val bySection = _state.value.sections.associateBy { it.section }
                val reordered = event.order.mapNotNull { bySection[it] }
                if (reordered.size != _state.value.sections.size) return
                _state.update { it.copy(sections = reordered) }
                persist(DataStoreConstants.STATISTICS_SECTIONS, reordered.encode())
            }

            is StatisticsEvent.OnToggleMetric -> {
                val updated = _state.value.metrics.map {
                    if (it.metric == event.metric) it.copy(visible = !it.visible) else it
                }
                _state.update { it.copy(metrics = updated) }
                persist(DataStoreConstants.STATISTICS_METRICS, updated.encodeMetrics())
            }

            is StatisticsEvent.OnReorderMetrics -> {
                val byMetric = _state.value.metrics.associateBy { it.metric }
                val reordered = event.order.mapNotNull { byMetric[it] }
                if (reordered.size != _state.value.metrics.size) return
                _state.update { it.copy(metrics = reordered) }
                persist(DataStoreConstants.STATISTICS_METRICS, reordered.encodeMetrics())
            }

            is StatisticsEvent.OnSetWeekStart -> {
                _state.update { it.copy(weekStartMonday = event.monday) }
                persist(DataStoreConstants.STATISTICS_WEEK_START_MONDAY, event.monday)
                // Heatmap alignment depends on week start — reload aggregation.
                reloadStatistics(event.monday, _state.value.statisticsRange)
            }

            is StatisticsEvent.OnResetLayout -> {
                val sections = defaultStatSections()
                val metrics = defaultStatMetrics()
                _state.update {
                    it.copy(
                        sections = sections,
                        metrics = metrics
                    )
                }
                persist(DataStoreConstants.STATISTICS_SECTIONS, sections.encode())
                persist(DataStoreConstants.STATISTICS_METRICS, metrics.encodeMetrics())
            }

            is StatisticsEvent.OnResetMetrics -> {
                val metrics = defaultStatMetrics()
                _state.update { it.copy(metrics = metrics) }
                persist(DataStoreConstants.STATISTICS_METRICS, metrics.encodeMetrics())
            }
        }
    }

    fun refreshStatistics() {
        val current = _state.value
        if (current.isLoading) return
        reloadStatistics(
            weekStartMonday = current.weekStartMonday,
            range = current.statisticsRange
        )
    }

    private fun <T> persist(key: androidx.datastore.preferences.core.Preferences.Key<T>, value: T) {
        viewModelScope.launch(Dispatchers.IO) { setDatastore.execute(key, value) }
    }

    private fun reloadStatistics(
        weekStartMonday: Boolean,
        range: StatisticsRange
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val statistics = getReadingStatistics.execute(weekStartMonday, range)
                _state.update { it.copy(statistics = statistics) }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to reload statistics", e)
            }
        }
    }

    private fun load() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val goal = getDatastore.execute(DataStoreConstants.READING_GOAL_MINUTES)
                val rangeRaw = getDatastore.execute(DataStoreConstants.STATISTICS_RANGE)
                val weekStart = getDatastore.execute(DataStoreConstants.STATISTICS_WEEK_START_MONDAY)
                val sectionsRaw = getDatastore.execute(DataStoreConstants.STATISTICS_SECTIONS)
                val metricsRaw = getDatastore.execute(DataStoreConstants.STATISTICS_METRICS)
                val weekStartMonday = weekStart ?: true
                val statisticsRange = decodeStatisticsRange(rangeRaw)
                val statistics = getReadingStatistics.execute(weekStartMonday, statisticsRange)
                _state.update {
                    it.copy(
                        statistics = statistics,
                        goalMinutes = goal ?: DEFAULT_GOAL,
                        statisticsRange = statisticsRange,
                        weekStartMonday = weekStartMonday,
                        sections = decodeStatSections(sectionsRaw),
                        metrics = decodeStatMetrics(metricsRaw),
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load statistics", e)
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    companion object {
        private const val TAG = "StatisticsModel"
        const val DEFAULT_GOAL = 30
        const val GOAL_MIN = 5
        const val GOAL_MAX = 240
    }
}
