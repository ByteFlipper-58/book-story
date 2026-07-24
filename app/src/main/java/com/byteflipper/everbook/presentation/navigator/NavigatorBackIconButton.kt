/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.navigator

import androidx.compose.runtime.Composable
import com.byteflipper.everbook.R
import com.byteflipper.everbook.presentation.core.components.common.IconButton

@Composable
fun NavigatorBackIconButton(
    enabled: Boolean = true,
    navigateBack: () -> Unit,
) {
    IconButton(
        icon = R.drawable.ic_arrow_back_rounded_24px,
        contentDescription = R.string.go_back_content_desc,
        disableOnClick = true,
        enabled = enabled
    ) {
        navigateBack()
    }
}