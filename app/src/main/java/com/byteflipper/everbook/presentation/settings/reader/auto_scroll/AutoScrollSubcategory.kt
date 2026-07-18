/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

@file:Suppress("FunctionName")

package com.byteflipper.everbook.presentation.settings.reader.auto_scroll

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.byteflipper.everbook.R
import com.byteflipper.everbook.presentation.settings.components.SettingsSubcategory
import com.byteflipper.everbook.presentation.settings.reader.reading_speed.components.AutoScrollSpeedOption
import com.byteflipper.everbook.presentation.settings.reader.auto_scroll.components.AutoScrollChipAlignmentOption
import com.byteflipper.everbook.presentation.settings.reader.auto_scroll.components.AutoScrollChipOpacityEnabledOption
import com.byteflipper.everbook.presentation.settings.reader.auto_scroll.components.AutoScrollChipOpacityOption
import com.byteflipper.everbook.presentation.settings.reader.auto_scroll.components.AutoScrollChipPlayPauseOption

fun LazyListScope.AutoScrollSubcategory(
    titleColor: @Composable () -> Color = { MaterialTheme.colorScheme.primary },
    title: @Composable () -> String = { stringResource(id = R.string.auto_scroll_reader_settings) },
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
            AutoScrollSpeedOption()
        }

        item {
            AutoScrollChipAlignmentOption()
        }

        item {
            AutoScrollChipOpacityEnabledOption()
        }

        item {
            AutoScrollChipOpacityOption()
        }

        item {
            AutoScrollChipPlayPauseOption()
        }
    }
}
