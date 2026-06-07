/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.domain.translation

import androidx.compose.runtime.Immutable

@Immutable
data class TranslationModelState(
    val language: TranslationLanguage,
    val downloaded: Boolean,
    val supported: Boolean
)
