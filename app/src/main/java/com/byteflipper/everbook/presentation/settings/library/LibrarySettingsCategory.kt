/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.settings.library

import androidx.compose.foundation.lazy.LazyListScope
import com.byteflipper.everbook.domain.library.custom_category.Category
import com.byteflipper.everbook.presentation.settings.library.subcategory.CategoriesSubcategory

fun LazyListScope.LibrarySettingsCategory(
    categories: List<Category>,
    onToggleVisibility: (Int, Boolean) -> Unit,
    onDelete: (Int, Int?) -> Unit,
    onRequestCreate: () -> Unit,
    onRequestRename: (Int, String) -> Unit
) {
    CategoriesSubcategory(
        categories = categories,
        onToggleVisibility = onToggleVisibility,
        onRequestRename = onRequestRename,
        onDelete = onDelete,
        onRequestCreate = onRequestCreate
    )
} 