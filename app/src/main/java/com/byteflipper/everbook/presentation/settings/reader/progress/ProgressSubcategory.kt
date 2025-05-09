/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

@file:Suppress("FunctionName")

package com.byteflipper.everbook.presentation.settings.reader.progress

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.byteflipper.everbook.R
import com.byteflipper.everbook.presentation.settings.components.SettingsSubcategory
import com.byteflipper.everbook.presentation.settings.reader.progress.components.ProgressBarAlignmentOption
import com.byteflipper.everbook.presentation.settings.reader.progress.components.ProgressBarFontSizeOption
import com.byteflipper.everbook.presentation.settings.reader.progress.components.ProgressBarOption
import com.byteflipper.everbook.presentation.settings.reader.progress.components.ProgressBarPaddingOption
import com.byteflipper.everbook.presentation.settings.reader.progress.components.ProgressCountOption

fun LazyListScope.ProgressSubcategory(
    titleColor: @Composable () -> Color = { MaterialTheme.colorScheme.primary },
    title: @Composable () -> String = { stringResource(id = R.string.progress_reader_settings) },
    showTitle: Boolean = true,
    showDivider: Boolean = true
) {
    SettingsSubcategory(
        titleColor = titleColor,
        title = title,
        showTitle = showTitle,
        showDivider = showDivider,
    ) {
        item {
            ProgressCountOption()
        }

        item {
            ProgressBarOption()
        }

        item {
            ProgressBarFontSizeOption()
        }

        item {
            ProgressBarPaddingOption()
        }

        item {
            ProgressBarAlignmentOption()
        }
    }
}