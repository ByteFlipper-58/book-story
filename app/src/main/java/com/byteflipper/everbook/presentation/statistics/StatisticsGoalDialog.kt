/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.statistics

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.byteflipper.everbook.R
import com.byteflipper.everbook.presentation.core.components.common.StyledText

@Composable
internal fun GoalDialog(
    initialMinutes: Int,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var value by remember { mutableStateOf(initialMinutes.toFloat()) }
    val minutes = value.toInt()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { StyledText(stringResource(id = R.string.statistics_daily_goal)) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                StyledText(
                    text = stringResource(id = R.string.time_minutes, minutes),
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = MaterialTheme.colorScheme.primary
                    )
                )
                Spacer(Modifier.height(8.dp))
                Slider(
                    value = value,
                    onValueChange = { value = it },
                    valueRange = 5f..240f,
                    steps = (240 - 5) / 5 - 1
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(minutes) }) {
                StyledText(stringResource(id = R.string.done))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                StyledText(stringResource(id = R.string.cancel))
            }
        }
    )
}
