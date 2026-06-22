/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.domain.statistics

import androidx.compose.runtime.Immutable

/** Customizable summary cards at the top of the statistics screen. */
@Immutable
enum class StatMetric {
    TOTAL_TIME,
    AVG_PER_DAY,
    STREAK,
    BEST_STREAK,
    FINISHED,
    IN_PROGRESS,
    LONGEST_SESSION,
    BEST_DAY,
    TOTAL_SESSIONS
}

@Immutable
data class StatMetricPref(
    val metric: StatMetric,
    val visible: Boolean
)

/** Default order, all visible. */
fun defaultStatMetrics(): List<StatMetricPref> =
    StatMetric.entries.map { StatMetricPref(it, true) }

/** Serializes to "TOTAL_TIME:1,STREAK:0,..." for DataStore. */
fun List<StatMetricPref>.encodeMetrics(): String =
    joinToString(",") { "${it.metric.name}:${if (it.visible) 1 else 0}" }

/** Parses the stored string, tolerating unknown/missing metrics (appends new ones at the end). */
fun decodeStatMetrics(raw: String?): List<StatMetricPref> {
    if (raw.isNullOrBlank()) return defaultStatMetrics()
    val parsed = raw.split(",").mapNotNull { token ->
        val parts = token.split(":")
        val metric = parts.getOrNull(0)?.let { name ->
            StatMetric.entries.firstOrNull { it.name == name }
        } ?: return@mapNotNull null
        StatMetricPref(metric, parts.getOrNull(1) != "0")
    }
    val known = parsed.map { it.metric }.toSet()
    val missing = StatMetric.entries.filter { it !in known }.map { StatMetricPref(it, true) }
    return (parsed + missing).ifEmpty { defaultStatMetrics() }
}
