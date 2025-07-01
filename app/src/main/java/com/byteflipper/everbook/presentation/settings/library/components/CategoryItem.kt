/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.settings.library.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DragIndicator
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.byteflipper.everbook.R
import com.byteflipper.everbook.domain.library.custom_category.Category
import sh.calvin.reorderable.ReorderableCollectionItemScope
import com.byteflipper.everbook.presentation.core.components.dialog.Dialog
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalDensity
import kotlinx.coroutines.launch

/**
 * Элемент категории с поддержкой drag and drop
 */
@Composable
fun ReorderableCollectionItemScope.DraggableCategoryItem(
    category: Category,
    onToggleVisibility: () -> Unit,
    onEdit: () -> Unit
) {
    val hapticFeedback = LocalHapticFeedback.current
    
    CategoryItem(
        category = category,
        onToggleVisibility = onToggleVisibility,
        onEdit = onEdit,
        onDelete = {},
        isReorderMode = true,
        dragModifier = Modifier.longPressDraggableHandle(
            onDragStarted = {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
            },
            onDragStopped = {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
        )
    )
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CategoryItem(
    category: Category,
    onToggleVisibility: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit = {},
    isReorderMode: Boolean = false,
    dragModifier: Modifier = Modifier,
) {
    val hapticFeedback = LocalHapticFeedback.current
    
    var showConfirmDelete by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                showConfirmDelete = true
            }
            false
        }
    )
    
    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val maxOffset = with(LocalDensity.current){ 120.dp.toPx() }
            val rawOffset = runCatching { dismissState.requireOffset() }.getOrElse { 0f }
            val progress = (kotlin.math.abs(rawOffset)/maxOffset).coerceIn(0f,1f)
            val scale by animateFloatAsState(
                targetValue = when {
                    progress > 0.7f -> 1.2f
                    else -> 1f
                },
                animationSpec = tween(200),
                label = "delete_icon_scale"
            )
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = MaterialTheme.colorScheme.errorContainer.copy(
                            alpha = animateFloatAsState(
                                targetValue = if (progress > 0.01f) 1f else 0f,
                                animationSpec = tween(200),
                                label = "background_alpha"
                            ).value
                        ),
                        shape = RoundedCornerShape(0.dp)
                    )
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = progress,
                        strokeWidth = 2.dp,
                        modifier = Modifier
                            .size(36.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = stringResource(R.string.delete_category),
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier
                            .scale(scale)
                            .alpha(
                                animateFloatAsState(
                                    targetValue = if (progress > 0.01f) 1f else 0f,
                                    animationSpec = tween(200),
                                    label = "icon_alpha"
                                ).value
                            )
                            .size(20.dp)
                    )
                }
            }
        },
        content = {
            CategoryContent(
                category = category,
                onToggleVisibility = onToggleVisibility,
                onEdit = onEdit,
                isReorderMode = isReorderMode,
                dragModifier = dragModifier
            )
        }
    )

    if (showConfirmDelete) {
        Dialog(
            title = stringResource(R.string.delete_category),
            icon = Icons.Outlined.Delete,
            description = stringResource(R.string.delete_category_confirm),
            onDismiss = {
                showConfirmDelete = false
                scope.launch { dismissState.reset() }
            },
            actionEnabled = true,
            onAction = {
                showConfirmDelete = false
                onDelete()
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                scope.launch { dismissState.reset() }
            },
            withContent = false
        )
    }
}

@Composable
private fun CategoryContent(
    category: Category,
    onToggleVisibility: () -> Unit,
    onEdit: () -> Unit,
    isReorderMode: Boolean,
    dragModifier: Modifier
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = if (category.isDefault) 1.dp else 2.dp
        ),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (category.isDefault) {
                MaterialTheme.colorScheme.surfaceContainerLowest
            } else {
                MaterialTheme.colorScheme.surfaceContainerLowest
            }
        ),
        shape = RoundedCornerShape(0.dp)
    ) {
        ListItem(
            headlineContent = {
                Text(text = category.title.asString())
            },
            supportingContent = {
                if (category.isDefault) {
                    Text(text = stringResource(id = R.string.default_category_label))
                }
            },
            leadingContent = {
                CategoryIcon(category.isDefault)
            },
            trailingContent = {
                Crossfade(targetState = isReorderMode, animationSpec = tween(300), label = "trailing_crossfade") { reorderMode ->
                    if (reorderMode) {
                        DragHandle(dragModifier)
                    } else {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            if (!category.isDefault) {
                                SmallIconButton(Icons.Outlined.Edit, R.string.edit_category, onEdit)
                            }
                            VisibilityButton(category.isVisible, onToggleVisibility)
                        }
                    }
                }
            }
        )
    }
}

@Composable
private fun DragHandle(modifier: Modifier = Modifier) {
    FilledTonalIconButton(
        onClick = { /* будет перехвачено draggableHandle модификатором */ },
        modifier = modifier
            .size(40.dp)
            .clearAndSetSemantics { },
        colors = IconButtonDefaults.filledTonalIconButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Icon(
            imageVector = Icons.Outlined.DragIndicator,
            contentDescription = stringResource(id = R.string.drag_to_reorder),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun CategoryIcon(isDefault: Boolean) {
    androidx.compose.material3.Surface(
        shape = CircleShape,
        color = if (isDefault) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer,
        modifier = Modifier.size(48.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Outlined.Folder,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = if (isDefault) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
private fun VisibilityButton(visible: Boolean, onClick: () -> Unit) {
    val hapticFeedback = LocalHapticFeedback.current
    
    val targetContainer = if (visible) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
    val targetContent = if (visible) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant

    val containerColor by animateColorAsState(targetContainer, label = "container_anim")
    val contentColor by animateColorAsState(targetContent, label = "content_anim")

    FilledTonalIconButton(
        onClick = {
            onClick()
            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        },
        modifier = Modifier.size(40.dp),
        colors = IconButtonDefaults.filledTonalIconButtonColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    ) {
        Crossfade(targetState = visible, animationSpec = tween(200), label = "icon_visibility") { state ->
            Icon(
                imageVector = if (state) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                contentDescription = if (state) stringResource(id = R.string.hide_category) else stringResource(id = R.string.show_category),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun SmallIconButton(icon: ImageVector, descRes: Int, onClick: () -> Unit, isError: Boolean = false) {
    val hapticFeedback = LocalHapticFeedback.current
    
    val targetContainer = if (isError) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant
    val targetContent = if (isError) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurfaceVariant

    val containerColor by animateColorAsState(targetContainer, label = "small_container_anim")
    val contentColor by animateColorAsState(targetContent, label = "small_content_anim")

    FilledTonalIconButton(
        onClick = {
            onClick()
            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        },
        modifier = Modifier.size(40.dp),
        colors = IconButtonDefaults.filledTonalIconButtonColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    ) {
        Icon(imageVector = icon, contentDescription = stringResource(id = descRes), modifier = Modifier.size(20.dp))
    }
}