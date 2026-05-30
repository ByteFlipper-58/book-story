/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.ui.changelog

import androidx.compose.runtime.Immutable
import com.byteflipper.everbook.domain.changelog.ChangelogRelease

@Immutable
data class ChangelogState(
    val isLoading: Boolean = true,
    val releases: List<ChangelogRelease> = emptyList(),
    val selectedRelease: ChangelogRelease? = null
)
