/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.statistics
import androidx.compose.ui.res.painterResource

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import com.byteflipper.everbook.R
import com.byteflipper.everbook.domain.statistics.StatSection
import com.byteflipper.everbook.presentation.core.components.common.StyledText
import kotlin.math.abs

@Composable
internal fun PagerDots(
    count: Int,
    currentPage: Int,
    offsetFraction: Float
) {
    val activeColor = MaterialTheme.colorScheme.primary
    val inactiveColor = MaterialTheme.colorScheme.surfaceContainerHighest
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(count) { i ->
            val distance = (abs(currentPage + offsetFraction - i)).coerceIn(0f, 1f)
            val width = lerp(22.dp, 7.dp, distance)
            val color = lerp(activeColor, inactiveColor, distance)
            Box(
                modifier = Modifier
                    .padding(horizontal = 3.dp)
                    .height(7.dp)
                    .width(width)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}

@Composable
internal fun animatedCount(target: Int): Int {
    var start by remember { mutableStateOf(0) }
    LaunchedEffect(target) { start = target }
    val value by animateIntAsState(
        targetValue = start,
        animationSpec = tween(700),
        label = "count"
    )
    return value
}

@Composable
internal fun EditCard(
    handleModifier: Modifier,
    title: String,
    visible: Boolean,
    onToggleVisible: () -> Unit,
    content: @Composable () -> Unit
) {
    val contentAlpha by animateFloatAsState(
        if (visible) 1f else 0.35f,
        tween(250),
        label = "editAlpha"
    )
    Box(modifier = Modifier.fillMaxWidth()) {
        Box(modifier = Modifier.alpha(contentAlpha)) {
            content()
        }

        OverlayControl(modifier = Modifier.align(Alignment.TopStart)) {
            Icon(
                painter = painterResource(R.drawable.ic_drag_handle_rounded_24px),
                contentDescription = stringResource(id = R.string.drag_content_desc),
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = handleModifier.size(22.dp)
            )
        }

        OverlayControl(
            modifier = Modifier.align(Alignment.TopEnd),
            onClick = onToggleVisible
        ) {
            Icon(
                painter = if (visible) painterResource(R.drawable.ic_visibility_rounded_24px)
                else painterResource(R.drawable.ic_visibility_off_rounded_24px),
                contentDescription = null,
                tint = if (visible) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
internal fun OverlayControl(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .padding(5.dp)
            .size(30.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.92f))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
internal fun WeekStartRow(
    checked: Boolean,
    onChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(start = 16.dp, end = 12.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        StyledText(
            text = stringResource(id = R.string.statistics_week_start_monday),
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleMedium.copy(
                color = MaterialTheme.colorScheme.onSurface
            )
        )
        Switch(
            checked = checked,
            onCheckedChange = onChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.secondary,
                checkedTrackColor = MaterialTheme.colorScheme.secondaryContainer
            )
        )
    }
}

@Composable
internal fun sectionTitle(section: StatSection): String = stringResource(
    id = when (section) {
        StatSection.ACTIVITY -> R.string.statistics_activity
        StatSection.TIME_OF_DAY -> R.string.statistics_time_of_day
        StatSection.CALENDAR -> R.string.statistics_calendar
        StatSection.BOOKS -> R.string.statistics_by_books
    }
)
