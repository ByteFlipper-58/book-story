/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.ui.statistics

import androidx.compose.runtime.Immutable
import com.byteflipper.everbook.domain.statistics.StatMetric
import com.byteflipper.everbook.domain.statistics.StatSection
import com.byteflipper.everbook.domain.statistics.StatisticsRange

@Immutable
sealed class StatisticsEvent {
    data class OnChangeRange(val range: StatisticsRange) : StatisticsEvent()
    data class OnSetGoal(val minutes: Int) : StatisticsEvent()
    data class OnToggleSection(val section: StatSection) : StatisticsEvent()
    data class OnReorderSections(val order: List<StatSection>) : StatisticsEvent()
    data class OnToggleMetric(val metric: StatMetric) : StatisticsEvent()
    data class OnReorderMetrics(val order: List<StatMetric>) : StatisticsEvent()
    data class OnSetWeekStart(val monday: Boolean) : StatisticsEvent()
    data object OnResetLayout : StatisticsEvent()
    data object OnResetMetrics : StatisticsEvent()
}
