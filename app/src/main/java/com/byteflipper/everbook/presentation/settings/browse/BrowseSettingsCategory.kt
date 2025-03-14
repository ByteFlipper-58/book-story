/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

@file:Suppress("FunctionName")

package com.byteflipper.everbook.presentation.settings.browse

import androidx.compose.foundation.lazy.LazyListScope
import com.byteflipper.everbook.presentation.settings.browse.display.BrowseDisplaySubcategory
import com.byteflipper.everbook.presentation.settings.browse.filter.BrowseFilterSubcategory
import com.byteflipper.everbook.presentation.settings.browse.scan.BrowseScanSubcategory
import com.byteflipper.everbook.presentation.settings.browse.sort.BrowseSortSubcategory

fun LazyListScope.BrowseSettingsCategory() {
    BrowseScanSubcategory()
    BrowseDisplaySubcategory()
    BrowseFilterSubcategory()
    BrowseSortSubcategory(
        showDivider = false
    )
}