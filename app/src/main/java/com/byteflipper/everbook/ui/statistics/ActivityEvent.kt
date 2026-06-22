/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.ui.statistics

import androidx.compose.runtime.Immutable
import com.byteflipper.everbook.domain.statistics.ActivityRange
import java.time.LocalDate

@Immutable
sealed class ActivityEvent {
    data class OnSetRange(val range: ActivityRange) : ActivityEvent()
    data class OnSelectDate(val date: LocalDate) : ActivityEvent()
    data object OnPreviousPeriod : ActivityEvent()
    data object OnNextPeriod : ActivityEvent()
    data object OnGoToToday : ActivityEvent()
    data class OnDrillDown(val range: ActivityRange, val date: LocalDate) : ActivityEvent()
    data object OnShowCalendar : ActivityEvent()
    data object OnDismissCalendar : ActivityEvent()
    data object OnPreviousMonth : ActivityEvent()
    data object OnNextMonth : ActivityEvent()
}
