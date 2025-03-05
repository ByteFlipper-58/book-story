/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.history

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.byteflipper.everbook.R
import com.byteflipper.everbook.presentation.core.components.common.AnimatedVisibility
import com.byteflipper.everbook.presentation.core.components.placeholder.EmptyPlaceholder
import com.byteflipper.everbook.ui.theme.Transitions

@Composable
fun BoxScope.HistoryEmptyPlaceholder(
    isLoading: Boolean,
    isRefreshing: Boolean,
    isHistoryEmpty: Boolean
) {
    AnimatedVisibility(
        visible = !isLoading
                && isHistoryEmpty
                && !isRefreshing,
        modifier = Modifier.align(Alignment.Center),
        enter = Transitions.DefaultTransitionIn,
        exit = fadeOut(tween(0))
    ) {
        EmptyPlaceholder(
            modifier = Modifier.align(Alignment.Center),
            message = stringResource(id = R.string.history_empty),
            icon = painterResource(id = R.drawable.empty_history)
        )
    }
}