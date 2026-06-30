/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.statistics

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.byteflipper.everbook.R
import com.byteflipper.everbook.presentation.core.components.common.StyledText

@Composable
internal fun ResetMetricsDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { StyledText(stringResource(id = R.string.statistics_reset_metrics_title)) },
        text = {
            StyledText(
                text = stringResource(id = R.string.statistics_reset_metrics_desc),
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                StyledText(stringResource(id = R.string.statistics_reset_layout_action))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                StyledText(stringResource(id = R.string.cancel))
            }
        }
    )
}
