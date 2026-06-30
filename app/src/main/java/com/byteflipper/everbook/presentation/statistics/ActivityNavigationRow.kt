/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.statistics

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.byteflipper.everbook.R
import com.byteflipper.everbook.domain.statistics.ActivityRange
import com.byteflipper.everbook.presentation.core.components.common.StyledText
import com.byteflipper.everbook.ui.statistics.ActivityState
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val NAV_BUTTON_SIZE = 48.dp
private val TOGGLE_HEIGHT = 56.dp

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun ActivityNavigationRow(
    state: ActivityState,
    onRangeChange: (ActivityRange) -> Unit,
    onPreviousPeriod: () -> Unit,
    onNextPeriod: () -> Unit,
    onShowCalendar: () -> Unit
) {
    val today = LocalDate.now()
    val canGoNext = state.selectedDate.isBefore(today)

    val ranges = ActivityRange.entries
    val labels = listOf(
        stringResource(R.string.activity_range_day),
        stringResource(R.string.activity_range_week),
        stringResource(R.string.activity_range_month),
        stringResource(R.string.activity_range_3months),
        stringResource(R.string.activity_range_year)
    )

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ranges.forEachIndexed { index, range ->
                val checked = range == state.range
                ToggleButton(
                    checked = checked,
                    onCheckedChange = { isChecked ->
                        if (isChecked) onRangeChange(range)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(TOGGLE_HEIGHT),
                    shapes = when (index) {
                        0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                        ranges.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                        else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                    }
                ) {
                    Text(
                        text = labels[index],
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        fontSize = 13.sp
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StyledText(
                text = activityNavigatorLabel(state),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                maxLines = 1
            )

            Box(
                modifier = Modifier
                    .size(NAV_BUTTON_SIZE)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .clickable(onClick = onPreviousPeriod),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(22.dp)
                )
            }

            Box(
                modifier = Modifier
                    .size(NAV_BUTTON_SIZE)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .then(
                        if (canGoNext) Modifier.clickable(onClick = onNextPeriod)
                        else Modifier.alpha(0.38f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(22.dp)
                )
            }

            Box(
                modifier = Modifier
                    .size(NAV_BUTTON_SIZE)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .clickable(onClick = onShowCalendar),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.CalendarToday,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
internal fun activityNavigatorLabel(state: ActivityState): String {
    if (state.range == ActivityRange.DAY) {
        if (state.selectedDate == LocalDate.now()) {
            return stringResource(id = R.string.activity_today)
        }
        val formatter = remember { DateTimeFormatter.ofPattern("d MMMM", Locale.getDefault()) }
        return formatter.format(state.selectedDate)
    }
    return state.rangeStats?.label ?: ""
}
