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
fun NavigatorIconButton(
    onClick: () -> Unit = { navigatorBottomSheetChannel.trySend(true) }
) {
    IconButton(
        icon = R.drawable.ic_more_vert_rounded_24px,
        contentDescription = R.string.more_content_desc,
        disableOnClick = false
    ) {
        onClick()
    }
}