/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.navigator

import androidx.compose.runtime.compositionLocalOf
import com.byteflipper.everbook.domain.navigator.Navigator

val LocalNavigator = compositionLocalOf<Navigator> { error("Navigator was not passed.") }