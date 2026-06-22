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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.byteflipper.everbook.R
import com.byteflipper.everbook.domain.statistics.ActivityRange
import com.byteflipper.everbook.domain.statistics.DayBucket
import com.byteflipper.everbook.presentation.core.components.common.StyledText
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
internal fun ActivityBreakdownGroup(
    buckets: List<DayBucket>,
    range: ActivityRange,
    onCardClick: (ActivityRange, LocalDate) -> Unit
) {
    val largeShape = MaterialTheme.shapes.large
    val zone = ZoneId.systemDefault()

    Column(modifier = Modifier.fillMaxWidth()) {
        buckets.forEachIndexed { index, bucket ->
            val shape = when {
                buckets.size == 1 -> largeShape
                index == 0 -> largeShape.copy(
                    bottomStart = CornerSize(3.dp),
                    bottomEnd = CornerSize(3.dp)
                )
                index == buckets.lastIndex -> largeShape.copy(
                    topStart = CornerSize(3.dp),
                    topEnd = CornerSize(3.dp)
                )
                else -> RoundedCornerShape(3.dp)
            }
            val topPadding = if (index > 0) 2.dp else 0.dp

            val bucketDate = Instant.ofEpochMilli(bucket.dayStartMillis)
                .atZone(zone).toLocalDate()

            val label = breakdownLabel(
                bucket = bucket,
                index = index,
                range = range,
                totalBuckets = buckets.size
            )
            val duration = formatDuration(bucket.timeMs)

            Row(
                modifier = Modifier
                    .padding(top = topPadding)
                    .fillMaxWidth()
                    .clip(shape)
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .clickable {
                        val (targetRange, targetDate) = when (range) {
                            ActivityRange.WEEK -> ActivityRange.DAY to bucketDate
                            ActivityRange.MONTH,
                            ActivityRange.THREE_MONTHS -> ActivityRange.WEEK to bucketDate
                            ActivityRange.YEAR -> ActivityRange.MONTH to bucketDate
                            else -> return@clickable
                        }
                        onCardClick(targetRange, targetDate)
                    }
                    .padding(horizontal = 18.dp, vertical = 18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StyledText(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.weight(1f, fill = false),
                    maxLines = 1
                )
                Spacer(Modifier.width(12.dp))
                StyledText(
                    text = duration,
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun breakdownLabel(
    bucket: DayBucket,
    index: Int,
    range: ActivityRange,
    totalBuckets: Int
): String {
    val today = LocalDate.now()
    val zone = ZoneId.systemDefault()
    val bucketDate = Instant.ofEpochMilli(bucket.dayStartMillis).atZone(zone).toLocalDate()
    val locale = Locale.getDefault()

    return when (range) {
        ActivityRange.DAY -> ""
        ActivityRange.WEEK -> {
            when {
                bucketDate == today -> stringResource(R.string.activity_today)
                bucketDate == today.minusDays(1) -> stringResource(R.string.activity_yesterday)
                else -> {
                    val fmt = DateTimeFormatter.ofPattern("EEE, dd.MM", locale)
                    fmt.format(bucketDate)
                }
            }
        }
        ActivityRange.MONTH, ActivityRange.THREE_MONTHS -> {
            val weekEnd = minOf(bucketDate.plusDays(6), today)
            val isCurrentWeek = !today.isBefore(bucketDate) && !today.isAfter(bucketDate.plusDays(6))
            when {
                isCurrentWeek -> stringResource(R.string.activity_this_week)
                else -> {
                    val fmtDay = DateTimeFormatter.ofPattern("d", locale)
                    val fmtDayMonth = DateTimeFormatter.ofPattern("d MMM", locale)
                    if (bucketDate.month == weekEnd.month) {
                        "${fmtDay.format(bucketDate)}–${fmtDayMonth.format(weekEnd)}"
                    } else {
                        "${fmtDayMonth.format(bucketDate)} – ${fmtDayMonth.format(weekEnd)}"
                    }
                }
            }
        }
        ActivityRange.YEAR -> {
            val ym = YearMonth.from(bucketDate)
            when {
                ym == YearMonth.now() -> stringResource(R.string.activity_this_month)
                else -> {
                    val fmt = DateTimeFormatter.ofPattern("LLLL", locale)
                    fmt.format(bucketDate).replaceFirstChar { it.uppercaseChar() }
                }
            }
        }
    }
}
