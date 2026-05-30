/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.domain.changelog

import androidx.compose.runtime.Immutable

@Immutable
data class ChangelogRelease(
    val versionCode: Int,
    val versionName: String,
    val file: String,
    val locale: String,
    val availableLocales: List<String>,
    val pages: List<ChangelogPage>
)

@Immutable
data class ChangelogPage(
    val title: String,
    val blocks: List<ChangelogBlock>
)

@Immutable
sealed class ChangelogBlock {
    data class Paragraph(val text: String) : ChangelogBlock()
    data class BulletList(val items: List<String>) : ChangelogBlock()
    data class Heading(val text: String) : ChangelogBlock()
    data class Quote(val text: String) : ChangelogBlock()
}
