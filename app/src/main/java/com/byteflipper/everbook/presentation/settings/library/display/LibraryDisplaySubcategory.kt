/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

@file:Suppress("FunctionName")

package com.byteflipper.everbook.presentation.settings.library.display

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.byteflipper.everbook.R
import com.byteflipper.everbook.presentation.settings.components.SettingsSubcategory
import com.byteflipper.everbook.presentation.settings.library.display.components.LibraryGridSizeOption
import com.byteflipper.everbook.presentation.settings.library.display.components.LibraryLayoutOption
import com.byteflipper.everbook.presentation.settings.library.display.components.LibraryReadButtonOption
import com.byteflipper.everbook.presentation.settings.library.display.components.LibraryShowProgressOption
import com.byteflipper.everbook.presentation.settings.library.display.components.LibraryTitlePositionOption

fun LazyListScope.LibraryDisplaySubcategory(
    titleColor: @Composable () -> Color = { MaterialTheme.colorScheme.primary },
    title: @Composable () -> String = { stringResource(id = R.string.display_settings) },
    showTitle: Boolean = true,
    showDivider: Boolean = true
) {
    SettingsSubcategory(
        titleColor = titleColor,
        title = title,
        showTitle = showTitle,
        showDivider = showDivider
    ) {
        item { LibraryLayoutOption() }
        item { LibraryGridSizeOption() }
        item { LibraryTitlePositionOption() }
        item { LibraryReadButtonOption() }
        item { LibraryShowProgressOption() }
    }
}
