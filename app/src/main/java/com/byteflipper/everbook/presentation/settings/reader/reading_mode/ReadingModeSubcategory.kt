/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

@file:Suppress("FunctionName")

package com.byteflipper.everbook.presentation.settings.reader.reading_mode

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.byteflipper.everbook.R
import com.byteflipper.everbook.presentation.settings.components.SettingsSubcategory
import com.byteflipper.everbook.presentation.settings.reader.reading_mode.components.HorizontalGestureAlphaAnimOption
import com.byteflipper.everbook.presentation.settings.reader.reading_mode.components.HorizontalGestureOption
import com.byteflipper.everbook.presentation.settings.reader.reading_mode.components.HorizontalGesturePullAnimOption
import com.byteflipper.everbook.presentation.settings.reader.reading_mode.components.HorizontalGestureScrollOption
import com.byteflipper.everbook.presentation.settings.reader.reading_mode.components.HorizontalGestureSensitivityOption

fun LazyListScope.ReadingModeSubcategory(
    titleColor: @Composable () -> Color = { MaterialTheme.colorScheme.primary },
    title: @Composable () -> String = { stringResource(id = R.string.reading_mode_reader_settings) },
    showTitle: Boolean = true,
    showDivider: Boolean = true
) {
    SettingsSubcategory(
        titleColor = titleColor,
        title = title,
        showTitle = showTitle,
        showDivider = showDivider
    ) {
        item {
            HorizontalGestureOption()
        }

        item {
            HorizontalGestureScrollOption()
        }

        item {
            HorizontalGestureSensitivityOption()
        }

        item {
            HorizontalGesturePullAnimOption()
        }

        item {
            HorizontalGestureAlphaAnimOption()
        }
    }
}