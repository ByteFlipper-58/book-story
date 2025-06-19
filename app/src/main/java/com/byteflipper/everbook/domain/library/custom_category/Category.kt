/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.domain.library.custom_category

import androidx.compose.runtime.Immutable
import com.byteflipper.everbook.domain.ui.UIText

@Immutable
data class Category(
    val id: Int,
    val name: String,
    val kind: String = "CUSTOM",
    val isVisible: Boolean = true,
    val position: Int = 0,
    val isDefault: Boolean = false,
    val title: UIText = UIText.StringValue("")
) 