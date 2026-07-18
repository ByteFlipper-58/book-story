/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.settings.reader.reading_speed.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.byteflipper.everbook.R
import com.byteflipper.everbook.presentation.settings.components.SettingsSubcategoryTitle
import com.byteflipper.everbook.presentation.core.components.common.StyledText
import com.byteflipper.everbook.ui.main.MainEvent
import com.byteflipper.everbook.ui.main.MainModel
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun AutoScrollSpeedOption() {
    val mainModel = hiltViewModel<MainModel>()
    val state = mainModel.state.collectAsStateWithLifecycle()
    val interactionSource = remember { MutableInteractionSource() }

    val speed = state.value.autoScrollSpeed
    val displayValue = String.format(Locale.US, "%.1f", speed)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.fillMaxWidth(0.2f)) {
            SettingsSubcategoryTitle(
                title = stringResource(id = R.string.auto_scroll_speed_option),
                padding = 0.dp
            )
            Spacer(modifier = Modifier.height(4.dp))
            StyledText(
                text = "${displayValue}x",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Slider(
            valueRange = 1.0f..10.0f,
            value = speed,
            onValueChange = {
                val rounded = (it * 2f).roundToInt() / 2f
                mainModel.onEvent(MainEvent.OnChangeAutoScrollSpeed(rounded))
            },
            interactionSource = interactionSource,
            steps = 17,
            colors = SliderDefaults.colors(
                activeTrackColor = MaterialTheme.colorScheme.secondary,
                thumbColor = MaterialTheme.colorScheme.secondary,
                inactiveTrackColor = MaterialTheme.colorScheme.secondaryContainer,
                activeTickColor = MaterialTheme.colorScheme.onSecondary,
                inactiveTickColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }
}
