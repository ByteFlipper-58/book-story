/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.settings.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.byteflipper.everbook.domain.library.custom_category.Category
import com.byteflipper.everbook.domain.repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibrarySettingsViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    val categories: StateFlow<List<Category>> = categoryRepository.observeCategories()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun createCategory(name: String) {
        if (name.isNotBlank()) {
            viewModelScope.launch {
                categoryRepository.createCategory(name)
            }
        }
    }

    fun renameCategory(id: Int, newName: String) {
        if (newName.isNotBlank()) {
            viewModelScope.launch {
                categoryRepository.renameCategory(id, newName)
            }
        }
    }

    fun deleteCategory(id: Int) {
        viewModelScope.launch {
            categoryRepository.deleteCategory(id, null)
        }
    }

    fun toggleCategoryVisibility(id: Int, visible: Boolean) {
        viewModelScope.launch {
            categoryRepository.toggleCategoryVisibility(id, visible)
        }
    }

    /**
     * Обновляет порядок категорий на основе переданного списка ID.
     * 
     * @param order Список ID категорий в новом порядке.
     */
    fun updateCategoriesOrder(order: List<Int>) {
        viewModelScope.launch {
            // Создаем карту [ID категории -> новая позиция]
            val positionsMap = order.mapIndexed { index, id -> id to index }.toMap()
            
            categoryRepository.updateCategoriesPositions(positionsMap)
        }
    }
} 