/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.domain.repository

import com.byteflipper.everbook.domain.library.custom_category.Category
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {

    fun observeCategories(): Flow<List<Category>>

    suspend fun createCategory(name: String)

    suspend fun renameCategory(id: Int, newName: String)

    suspend fun deleteCategory(id: Int, targetId: Int?)

    suspend fun reorderCategories(order: List<Int>)
    
    /**
     * Обновляет порядок категорий в рамках одной транзакции.
     * Используется для оптимизации массового обновления позиций.
     */
    suspend fun updateCategoriesPositions(idToPositionMap: Map<Int, Int>)

    suspend fun toggleCategoryVisibility(id: Int, visible: Boolean)
} 