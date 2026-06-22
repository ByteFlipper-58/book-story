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
import com.byteflipper.everbook.domain.statistics.ActivityRange
import com.byteflipper.everbook.domain.use_case.data_store.GetDatastore
import com.byteflipper.everbook.domain.use_case.statistics.GetActivityRangeStats
import com.byteflipper.everbook.domain.use_case.statistics.GetDailyActivityStats
import com.byteflipper.everbook.presentation.core.constants.DataStoreConstants
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ActivityModel @Inject constructor(
    private val getDailyActivityStats: GetDailyActivityStats,
    private val getActivityRangeStats: GetActivityRangeStats,
    private val getDatastore: GetDatastore
) : ViewModel() {

    private val _state = MutableStateFlow(ActivityState())
    val state = _state.asStateFlow()

    init {
        reload()
    }

    fun onEvent(event: ActivityEvent) {
        when (event) {
            is ActivityEvent.OnSetRange -> {
                _state.update { it.copy(range = event.range) }
                reload(
                    date = _state.value.selectedDate,
                    month = _state.value.calendarMonth,
                    showLoading = false
                )
            }

            is ActivityEvent.OnSelectDate -> selectDate(event.date)
            is ActivityEvent.OnPreviousPeriod -> previousPeriod()
            is ActivityEvent.OnNextPeriod -> nextPeriod()
            is ActivityEvent.OnGoToToday -> selectDate(LocalDate.now())

            is ActivityEvent.OnDrillDown -> {
                _state.update { it.copy(range = event.range) }
                selectDate(event.date)
            }

            is ActivityEvent.OnShowCalendar ->
                _state.update { it.copy(isCalendarVisible = true) }

            is ActivityEvent.OnDismissCalendar ->
                _state.update { it.copy(isCalendarVisible = false) }

            is ActivityEvent.OnPreviousMonth -> {
                val month = _state.value.calendarMonth.minusMonths(1)
                reload(date = _state.value.selectedDate, month = month, showLoading = false)
            }

            is ActivityEvent.OnNextMonth -> {
                val month = _state.value.calendarMonth.plusMonths(1)
                    .coerceAtMost(YearMonth.now())
                reload(date = _state.value.selectedDate, month = month, showLoading = false)
            }
        }
    }

    fun refresh() {
        reload(
            date = _state.value.selectedDate,
            month = _state.value.calendarMonth,
            showLoading = false
        )
    }

    private fun selectDate(date: LocalDate) {
        val selected = date.coerceAtMostToday()
        reload(date = selected, month = YearMonth.from(selected), showLoading = false)
    }

    private fun previousPeriod() {
        val s = _state.value
        val newDate = when (s.range) {
            ActivityRange.DAY -> s.selectedDate.minusDays(1)
            ActivityRange.WEEK -> s.selectedDate.minusWeeks(1)
            ActivityRange.MONTH -> s.selectedDate.minusMonths(1)
            ActivityRange.THREE_MONTHS -> s.selectedDate.minusMonths(3)
            ActivityRange.YEAR -> s.selectedDate.minusYears(1)
        }
        selectDate(newDate)
    }

    private fun nextPeriod() {
        val s = _state.value
        val newDate = when (s.range) {
            ActivityRange.DAY -> s.selectedDate.plusDays(1)
            ActivityRange.WEEK -> s.selectedDate.plusWeeks(1)
            ActivityRange.MONTH -> s.selectedDate.plusMonths(1)
            ActivityRange.THREE_MONTHS -> s.selectedDate.plusMonths(3)
            ActivityRange.YEAR -> s.selectedDate.plusYears(1)
        }
        selectDate(newDate)
    }

    private fun reload(
        date: LocalDate = _state.value.selectedDate,
        month: YearMonth = _state.value.calendarMonth,
        showLoading: Boolean = true
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            if (showLoading) _state.update { it.copy(isLoading = true) }
            try {
                val selected = date.coerceAtMostToday()
                val goal = getDatastore.execute(DataStoreConstants.READING_GOAL_MINUTES)
                    ?: StatisticsModel.DEFAULT_GOAL
                val weekStartMonday = getDatastore
                    .execute(DataStoreConstants.STATISTICS_WEEK_START_MONDAY) ?: true
                val range = _state.value.range

                val dailyStats = getDailyActivityStats.execute(
                    date = selected,
                    calendarMonth = month,
                    weekStartsMonday = weekStartMonday
                )

                val rangeStats = if (range != ActivityRange.DAY) {
                    getActivityRangeStats.execute(
                        range = range,
                        anchorDate = selected,
                        weekStartsMonday = weekStartMonday
                    )
                } else null

                _state.update {
                    it.copy(
                        stats = dailyStats,
                        rangeStats = rangeStats,
                        selectedDate = selected,
                        calendarMonth = dailyStats.calendarMonth,
                        goalMinutes = goal,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load activity stats", e)
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    companion object {
        private const val TAG = "ActivityModel"
    }

    private fun LocalDate.coerceAtMostToday(): LocalDate {
        val today = LocalDate.now()
        return if (isAfter(today)) today else this
    }

    private fun YearMonth.coerceAtMost(max: YearMonth): YearMonth {
        return if (this > max) max else this
    }
}
