/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.ui.settings

import android.os.Parcelable
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import dagger.hilt.android.lifecycle.HiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.parcelize.Parcelize
import com.byteflipper.everbook.domain.navigator.Screen
import com.byteflipper.everbook.domain.library.custom_category.Category
import com.byteflipper.everbook.domain.use_case.category.ObserveCategories
import com.byteflipper.everbook.domain.use_case.category.CreateCategory
import com.byteflipper.everbook.domain.use_case.category.ReorderCategories
import com.byteflipper.everbook.domain.use_case.category.ToggleCategoryVisibility
import com.byteflipper.everbook.domain.use_case.category.RenameCategory
import com.byteflipper.everbook.domain.use_case.category.DeleteCategory
import com.byteflipper.everbook.presentation.navigator.LocalNavigator
import com.byteflipper.everbook.presentation.core.components.top_bar.collapsibleTopAppBarScrollBehavior
import com.byteflipper.everbook.presentation.settings.library.LibrarySettingsScaffold
import javax.inject.Inject
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@HiltViewModel
class LibrarySettingsModel @Inject constructor(
    observeCategories: ObserveCategories,
    private val createCategoryUseCase: CreateCategory,
    private val reorderCategories: ReorderCategories,
    private val toggleCategoryVisibility: ToggleCategoryVisibility,
    private val renameCategory: RenameCategory,
    private val deleteCategory: DeleteCategory
) : ViewModel() {
    val categories: Flow<List<Category>> = observeCategories()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun createCategory(name: String) {
        viewModelScope.launch {
            createCategoryUseCase(name)
        }
    }

    fun reorder(order: List<Int>) {
        viewModelScope.launch {
            reorderCategories(order)
        }
    }

    fun toggleVisibility(id: Int, visible: Boolean) {
        viewModelScope.launch {
            toggleCategoryVisibility(id, visible)
        }
    }

    fun rename(id: Int, newName: String) {
        viewModelScope.launch {
            renameCategory(id, newName)
        }
    }

    fun delete(id: Int, targetId: Int?) {
        viewModelScope.launch {
            deleteCategory(id, targetId)
        }
    }
}

@Parcelize
object LibrarySettingsScreen : Screen, Parcelable {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val (scrollBehavior, listState) = TopAppBarDefaults.collapsibleTopAppBarScrollBehavior()
        val viewModel: LibrarySettingsModel = viewModel()

        val categoriesFlow = viewModel.categories

        LibrarySettingsScaffold(
            scrollBehavior = scrollBehavior,
            listState = listState,
            categories = categoriesFlow.collectAsStateWithLifecycle(initialValue = emptyList()).value,
            navigateBack = { navigator.pop() },
            onCreate = viewModel::createCategory,
            onToggleVisibility = viewModel::toggleVisibility,
            onRename = viewModel::rename,
            onDelete = viewModel::delete
        )
    }
} 