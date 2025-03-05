/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.reader

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import com.byteflipper.everbook.presentation.core.util.LocalActivity
import com.byteflipper.everbook.ui.reader.ReaderEvent

@Composable
fun ReaderBackHandler(
    leave: (ReaderEvent.OnLeave) -> Unit,
    navigateBack: () -> Unit
) {
    val activity = LocalActivity.current

    BackHandler {
        leave(
            ReaderEvent.OnLeave(
                activity = activity,
                navigate = {
                    navigateBack()
                }
            )
        )
    }
}