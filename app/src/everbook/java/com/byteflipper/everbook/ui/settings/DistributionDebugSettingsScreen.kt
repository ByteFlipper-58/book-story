/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.ui.settings

import android.os.Parcelable
import androidx.compose.runtime.Composable
import com.byteflipper.everbook.domain.navigator.Screen
import kotlinx.parcelize.Parcelize

@Parcelize
object DistributionDebugSettingsScreen : Screen, Parcelable {

    @Composable
    override fun Content() = Unit
}
