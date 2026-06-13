/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.translation.language_selection

internal data class TranslationLanguageSelectionItem(
    val code: String,
    val title: String,
    val subtitle: String?,
    val selected: Boolean,
    val supported: Boolean,
    val busy: Boolean,
    val downloaded: Boolean?,
    val downloadLanguageCode: String?
)

internal data class PendingLanguageSelection(
    val selectedLanguageCode: String,
    val downloadLanguageCode: String
)
