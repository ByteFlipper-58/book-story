/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.statistics
import com.byteflipper.everbook.R
import androidx.compose.ui.res.painterResource

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.byteflipper.everbook.presentation.core.components.common.StyledText
import com.byteflipper.everbook.ui.statistics.ActivityState
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
internal fun ActivityCalendarContent(
    state: ActivityState,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onSelectDate: (LocalDate) -> Unit
) {
    val today = LocalDate.now()
    val canGoNext = state.calendarMonth < YearMonth.now()
    val monthFormatter = remember { DateTimeFormatter.ofPattern("LLLL yyyy", Locale.getDefault()) }
    val weekFormatter = remember { DateTimeFormatter.ofPattern("EEEEE", Locale.getDefault()) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPreviousMonth) {
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_back_rounded_24px),
                    contentDescription = null
                )
            }
            StyledText(
                text = monthFormatter.format(state.calendarMonth),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                ),
                maxLines = 1
            )
            IconButton(
                enabled = canGoNext,
                onClick = onNextMonth
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_forward_rounded_24px),
                    contentDescription = null,
                    tint = if (canGoNext) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.32f)
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        val weekSample = state.stats.calendarDays.take(7)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            weekSample.forEach { day ->
                StyledText(
                    text = weekFormatter.format(day.date),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    ),
                    maxLines = 1
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        state.stats.calendarDays.chunked(7).forEach { week ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                week.forEach { day ->
                    val inMonth = YearMonth.from(day.date) == state.calendarMonth
                    val future = day.date.isAfter(today)
                    ActivityCalendarDayCell(
                        day = day,
                        selected = day.date == state.selectedDate,
                        inMonth = inMonth,
                        enabled = !future,
                        onClick = { onSelectDate(day.date) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
        }

        Spacer(Modifier.height(16.dp))
    }
}
