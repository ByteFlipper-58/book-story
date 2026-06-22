/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.domain.statistics

import androidx.compose.runtime.Immutable

/** Customizable sections of the statistics screen (the summary cards are always shown). */
enum class StatSection {
    ACTIVITY, TIME_OF_DAY, CALENDAR, BOOKS
}

@Immutable
data class StatSectionPref(
    val section: StatSection,
    val visible: Boolean
)

/** Default order, all visible. */
fun defaultStatSections(): List<StatSectionPref> =
    StatSection.entries.map { StatSectionPref(it, true) }

/** Serializes to "GOAL:1,ACTIVITY:0,..." for DataStore. */
fun List<StatSectionPref>.encode(): String =
    joinToString(",") { "${it.section.name}:${if (it.visible) 1 else 0}" }

/** Parses the stored string, tolerating unknown/missing sections (appends new ones at the end). */
fun decodeStatSections(raw: String?): List<StatSectionPref> {
    if (raw.isNullOrBlank()) return defaultStatSections()
    val parsed = raw.split(",").mapNotNull { token ->
        val parts = token.split(":")
        val section = parts.getOrNull(0)?.let { name ->
            StatSection.entries.firstOrNull { it.name == name }
        } ?: return@mapNotNull null
        StatSectionPref(section, parts.getOrNull(1) != "0")
    }
    // Append any sections added in newer versions that aren't in the stored value.
    val known = parsed.map { it.section }.toSet()
    val missing = StatSection.entries.filter { it !in known }.map { StatSectionPref(it, true) }
    val result = parsed + missing
    return result.ifEmpty { defaultStatSections() }
}
