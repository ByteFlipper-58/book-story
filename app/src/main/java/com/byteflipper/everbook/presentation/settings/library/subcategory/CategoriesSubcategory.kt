/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.settings.library.subcategory

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.byteflipper.everbook.R
import com.byteflipper.everbook.domain.library.custom_category.Category
import com.byteflipper.everbook.presentation.settings.components.SettingsSubcategory
import com.byteflipper.everbook.presentation.settings.library.CategoryDeleteDialog
import com.byteflipper.everbook.presentation.settings.library.components.CategoryItem
import com.byteflipper.everbook.presentation.settings.library.components.CreateCategoryButton
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

@OptIn(ExperimentalFoundationApi::class)
fun LazyListScope.CategoriesSubcategory(
    categories: List<Category>,
    onToggleVisibility: (Int, Boolean) -> Unit,
    onRequestRename: (Int, String) -> Unit,
    onDelete: (Int, Int?) -> Unit,
    onRequestCreate: () -> Unit
) {
    item { Spacer(Modifier.height(4.dp)) }

    SettingsSubcategory(
        titleColor = { MaterialTheme.colorScheme.primary },
        title = { "" },
        showTitle = false,
        showDivider = false
    ) {
        categories.forEach { cat ->
            item(key = cat.id) {
                var askDelete by remember { mutableStateOf(false) }
                val scope = rememberCoroutineScope()

                val dismissState = rememberSwipeToDismissBoxState(
                    positionalThreshold = { totalDistance -> totalDistance * 0.65f },
                    confirmValueChange = { newValue ->
                        if (newValue == SwipeToDismissBoxValue.EndToStart) {
                            askDelete = true
                        }
                        false
                    }
                )

                if (cat.isDefault) {
                    CategoryItem(
                        category = cat,
                        onToggleVisibility = { onToggleVisibility(cat.id, !cat.isVisible) },
                        onEdit = { onRequestRename(cat.id, cat.name) }
                    )
                } else {
                    SwipeToDismissBox(
                        state = dismissState,
                        enableDismissFromStartToEnd = false,
                        backgroundContent = {
                            val p = dismissState.progress
                            if (p > 0f) {
                                SwipeDeleteBackground(progress = p)
                            }
                        }
                    ) {
                        CategoryItem(
                            category = cat,
                            onToggleVisibility = { onToggleVisibility(cat.id, !cat.isVisible) },
                            onEdit = { onRequestRename(cat.id, cat.name) }
                        )
                    }

                    if (askDelete) {
                        CategoryDeleteDialog(
                            onConfirm = {
                                askDelete = false
                                onDelete(cat.id, null)
                            },
                            onDismiss = {
                                askDelete = false
                                scope.launch { dismissState.reset() }
                            }
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))
            }
        }

        item {
            Spacer(Modifier.height(8.dp))
            CreateCategoryButton {
                onRequestCreate()
            }
            Spacer(Modifier.height(8.dp))
        }

        item {
            androidx.compose.material3.Text(
                text = stringResource(id = R.string.categories_settings_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}

@Composable
private fun SwipeDeleteBackground(
    progress: Float
) {
    val startThreshold = 0.15f
    val midThreshold = 0.35f
    val fullThreshold = 0.65f

    var hapticTriggered by remember { mutableStateOf(false) }

    if (progress < startThreshold) {
        hapticTriggered = false
    }

    if (progress >= midThreshold && !hapticTriggered) {
        LocalHapticFeedback.current.performHapticFeedback(HapticFeedbackType.LongPress)
        hapticTriggered = true
    }

    val iconScale by animateFloatAsState(
        targetValue = when {
            progress < startThreshold -> 0.3f
            progress < midThreshold -> 0.3f + (progress - startThreshold) / (midThreshold - startThreshold) * 0.4f
            progress < fullThreshold -> 0.7f + (progress - midThreshold) / (fullThreshold - midThreshold) * 0.5f
            else -> 1.2f + sin((progress - fullThreshold) * PI * 4).toFloat() * 0.1f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "iconScale"
    )

    val progressBarProgress by animateFloatAsState(
        targetValue = when {
            progress < midThreshold -> 0f
            progress < fullThreshold -> (progress - midThreshold) / (fullThreshold - midThreshold)
            else -> 1f
        },
        animationSpec = tween(
            durationMillis = 200,
            easing = LinearOutSlowInEasing
        ),
        label = "progressBarProgress"
    )

    val contentColor = MaterialTheme.colorScheme.error

    val backgroundAlpha = 0f
    val backgroundColor = Color.Transparent

    Surface(
        color = backgroundColor,
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(end = 24.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            if (progress >= startThreshold) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .scale(iconScale * 0.8f)
                        .clip(CircleShape)
                        .background(
                            contentColor.copy(alpha = 0.2f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (progress >= midThreshold) {
                        CircularProgressIndicator(
                        progress = { progressBarProgress },
                        modifier = Modifier
                            .size(44.dp)
                            .graphicsLayer {
                                rotationZ = progress * 180f
                                           },
                        color = contentColor,
                        strokeWidth = 2.dp,
                        trackColor = contentColor.copy(alpha = 0.4f),
                        strokeCap = StrokeCap.Round,
                        )
                    }

                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = null,
                        modifier = Modifier
                            .size(20.dp)
                            .scale(iconScale)
                            .graphicsLayer {
                                if (progress >= fullThreshold) {
                                    val shake = sin(progress * PI * 6).toFloat() * 2f
                                    translationX = shake
                                    translationY = shake * 0.5f
                                }
                            },
                        tint = contentColor
                    )
                }
            }
        }
    }
}