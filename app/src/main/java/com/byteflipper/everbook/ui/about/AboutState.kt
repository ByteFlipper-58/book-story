/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.ui.about

import androidx.compose.runtime.Immutable
import com.byteflipper.everbook.data.remote.dto.LatestReleaseInfo
import com.byteflipper.everbook.domain.util.Dialog

@Immutable
data class AboutState(
    val dialog: Dialog? = null,
    val updateChecked: Boolean = false,
    val updateLoading: Boolean = false,
    val updateInfo: LatestReleaseInfo? = null
)