/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.settings.library.subcategory

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.outlined.Reorder
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.byteflipper.everbook.R
import com.byteflipper.everbook.domain.library.custom_category.Category
import com.byteflipper.everbook.presentation.settings.library.components.CategoryItem
import com.byteflipper.everbook.presentation.settings.library.components.CreateCategoryButton
import com.byteflipper.everbook.presentation.settings.library.components.DraggableCategoryItem
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.ReorderableLazyListState
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.animation.Crossfade

/**
 * Реализация подкатегории настроек категорий с поддержкой переупорядочивания.
 * 
 * @param isReorderMode Текущее состояние режима переупорядочивания
 * @param onReorderModeChanged Callback для изменения состояния режима
 */
fun LazyListScope.ReorderableCategoriesSubcategory(
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
    item { Spacer(Modifier.height(4.dp)) }
    
    item {
        Text(
            text = stringResource(R.string.categories_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
    
    val sortedCategories = categories.sortedBy { category ->
        val index = currentOrder.indexOf(category.id)
        if (index == -1) Int.MAX_VALUE else index
    }
    
    if (isReorderMode) {
        itemsIndexed(
            items = sortedCategories,
            key = { _, category -> category.id }
        ) { index, category ->
            ReorderableItem(
                state = reorderableState,
                key = category.id
            ) { isDragging ->
                DraggableCategoryItem(
                    category = category,
                    onToggleVisibility = { onToggleVisibility(category.id, !category.isVisible) },
                    onEdit = { onRequestRename(category.id, category.name) }
                )
            }
            
            Spacer(Modifier.height(12.dp))
        }
    } else {
        itemsIndexed(
            items = sortedCategories,
            key = { _, category -> category.id }
        ) { index, category ->
            CategoryItem(
                category = category,
                onToggleVisibility = { onToggleVisibility(category.id, !category.isVisible) },
                onEdit = { onRequestRename(category.id, category.name) },
                onDelete = { onDelete(category.id, null) },
                isReorderMode = false,
                dragModifier = Modifier
            )
            
            Spacer(Modifier.height(12.dp))
        }
    }
    
    item {
        Spacer(Modifier.height(8.dp))
        AnimatedButtonsRow(
            isReorderMode = isReorderMode,
            onRequestCreate = onRequestCreate,
            onToggleReorderMode = onReorderModeChanged,
            onSaveOrder = onSaveOrder
        )
        Spacer(Modifier.height(8.dp))
    }
    
    item {
        Text(
            text = stringResource(id = R.string.categories_settings_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        
        Spacer(Modifier.height(18.dp))
    }
}

/**
 * Анимированная строка с кнопками
 */
@OptIn(androidx.compose.animation.ExperimentalAnimationApi::class)
@Composable
private fun AnimatedButtonsRow(
    isReorderMode: Boolean,
    onRequestCreate: () -> Unit,
    onToggleReorderMode: (Boolean) -> Unit,
    onSaveOrder: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val reorderButtonFadeDuration = 300
    var reorderButtonVisible by remember { mutableStateOf(!isReorderMode) }

    LaunchedEffect(isReorderMode) {
        if (!isReorderMode) {
            delay(reorderButtonFadeDuration.toLong())
            reorderButtonVisible = true
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Crossfade(
            targetState = isReorderMode,
            modifier = Modifier
                .weight(1f)
                .animateContentSize(),
            animationSpec = tween(300),
            label = "button_crossfade"
        ) { reorderMode ->
            if (reorderMode) {
                SaveOrderButton(
                    modifier = Modifier.fillMaxWidth(),
                    onSave = {
                        onSaveOrder()
                        onToggleReorderMode(false)
                    }
                )
            } else {
                CreateCategoryButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onRequestCreate
                )
            }
        }

        AnimatedVisibility(
            visible = reorderButtonVisible,
            enter = fadeIn(animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessLow
            )) + expandHorizontally(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                expandFrom = Alignment.End
            ),
            exit = fadeOut(animationSpec = tween(reorderButtonFadeDuration)) + shrinkHorizontally(
                animationSpec = tween(reorderButtonFadeDuration),
                shrinkTowards = Alignment.End
            )
        ) {
            ReorderButton(
                onClick = {
                    reorderButtonVisible = false
                    scope.launch {
                        delay(reorderButtonFadeDuration.toLong())
                        onToggleReorderMode(true) // расширяем Save после исчезновения
                    }
                }
            )
        }
    }
}

/**
 * Кнопка для входа в режим переупорядочивания
 */
@Composable
private fun ReorderButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    
    FilledTonalIconButton(
        onClick = {
            onClick()
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        },
        modifier = modifier.size(56.dp),
        colors = IconButtonDefaults.filledTonalIconButtonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Icon(
            imageVector = Icons.Outlined.Reorder,
            contentDescription = stringResource(R.string.reorder_categories),
            modifier = Modifier.size(24.dp)
        )
    }
}

/**
 * Кнопка сохранения порядка
 */
@Composable
private fun SaveOrderButton(
    modifier: Modifier = Modifier,
    onSave: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    
    FilledTonalButton(
        onClick = {
            onSave()
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        },
        modifier = modifier.height(56.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Done,
            contentDescription = null,
            modifier = Modifier.padding(end = 8.dp)
        )
        Text(
            text = stringResource(R.string.save_order),
            style = MaterialTheme.typography.labelLarge
        )
    }
} 