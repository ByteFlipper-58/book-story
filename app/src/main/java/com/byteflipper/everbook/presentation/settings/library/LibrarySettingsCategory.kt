/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.settings.library

import androidx.compose.foundation.lazy.LazyListScope
import com.byteflipper.everbook.domain.library.custom_category.Category
import com.byteflipper.everbook.presentation.settings.library.subcategory.ReorderableCategoriesSubcategory
import sh.calvin.reorderable.ReorderableLazyListState

/**
 * Extension функция для LazyColumn, добавляющая секцию настроек категорий.
 * 
 * @param categories Список всех категорий
 * @param onToggleVisibility Обработчик изменения видимости категории
 * @param onRequestRename Обработчик запроса на переименование категории
 * @param onDelete Обработчик удаления категории
 * @param onRequestCreate Обработчик запроса на создание новой категории  
 * @param onSaveOrder Обработчик сохранения порядка категорий
 * @param reorderableState Состояние для поддержки перетаскивания
 * @param currentOrder Текущий порядок категорий (список ID)
 * @param isReorderMode Текущее состояние режима переупорядочивания
 * @param onReorderModeChanged Callback для изменения состояния режима
 */
fun LazyListScope.LibrarySettingsCategory(
    categories: List<Category>,
    onToggleVisibility: (Int, Boolean) -> Unit,
    onRequestRename: (Int, String) -> Unit,
    onDelete: (Int, Int?) -> Unit,
    onRequestCreate: () -> Unit,
    onSaveOrder: () -> Unit,
    reorderableState: ReorderableLazyListState,
    currentOrder: List<Int>,
    isReorderMode: Boolean,
    onReorderModeChanged: (Boolean) -> Unit
) {
    ReorderableCategoriesSubcategory(
        categories = categories,
        onToggleVisibility = onToggleVisibility,
        onRequestRename = onRequestRename,
        onDelete = onDelete,
        onRequestCreate = onRequestCreate,
        onSaveOrder = onSaveOrder,
        reorderableState = reorderableState,
        currentOrder = currentOrder,
        isReorderMode = isReorderMode,
        onReorderModeChanged = onReorderModeChanged
    )
} 