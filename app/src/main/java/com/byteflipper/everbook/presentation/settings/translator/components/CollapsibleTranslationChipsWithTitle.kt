/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.byteflipper.everbook.presentation.settings.translator.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.byteflipper.everbook.R
import com.byteflipper.everbook.domain.ui.ButtonItem
import com.byteflipper.everbook.presentation.core.components.common.StyledText
import com.byteflipper.everbook.presentation.settings.components.SettingsSubcategoryTitle
import kotlinx.coroutines.delay

private const val TRANSLATION_CHIPS_COLLAPSE_MS = 260L

@Composable
fun CollapsibleTranslationChipsWithTitle(
    stateKey: String,
    title: String,
    chips: List<ButtonItem>,
    onClick: (ButtonItem) -> Unit
) {
    var expanded by rememberSaveable(stateKey) { mutableStateOf(false) }
    var showExpandedContent by rememberSaveable(stateKey) { mutableStateOf(expanded) }
    val arrowRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = spring(
            dampingRatio = 0.82f,
            stiffness = 520f
        ),
        label = "TranslationChipsArrow"
    )

    LaunchedEffect(expanded) {
        if (expanded) {
            showExpandedContent = true
        } else {
            delay(TRANSLATION_CHIPS_COLLAPSE_MS)
            showExpandedContent = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = 0.86f,
                    stiffness = 420f
                )
            )
            .padding(horizontal = 18.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SettingsSubcategoryTitle(
                title = title,
                modifier = Modifier.weight(1f),
                padding = 0.dp
            )

            IconButton(onClick = { expanded = !expanded }) {
                Icon(
                    imageVector = Icons.Outlined.ExpandMore,
                    contentDescription = stringResource(
                        id = if (expanded) {
                            R.string.translation_collapse_languages_content_desc
                        } else {
                            R.string.translation_expand_languages_content_desc
                        }
                    ),
                    modifier = Modifier
                        .size(24.dp)
                        .rotate(arrowRotation)
                )
            }
        }

        if (showExpandedContent) {
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn(animationSpec = tween(durationMillis = 90)) +
                        expandVertically(
                            animationSpec = spring(
                                dampingRatio = 0.9f,
                                stiffness = 500f
                            ),
                            expandFrom = Alignment.Top
                        ),
                exit = shrinkVertically(
                    animationSpec = tween(durationMillis = TRANSLATION_CHIPS_COLLAPSE_MS.toInt()),
                    shrinkTowards = Alignment.Top
                ) + fadeOut(
                    animationSpec = tween(
                        durationMillis = 80,
                        delayMillis = (TRANSLATION_CHIPS_COLLAPSE_MS - 80).toInt()
                    )
                )
            ) {
                TranslationExpandedChips(
                    chips = chips,
                    onClick = onClick
                )
            }
        } else {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = chips,
                    key = { item -> item.id }
                ) { item ->
                    TranslationChip(
                        modifier = Modifier.animateItem(),
                        item = item,
                        onClick = onClick
                    )
                }
            }
        }
    }
}

@Composable
private fun TranslationExpandedChips(
    chips: List<ButtonItem>,
    onClick: (ButtonItem) -> Unit
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        chips.forEach { item ->
            TranslationChip(
                item = item,
                onClick = onClick
            )
        }
    }
}

@Composable
private fun TranslationChip(
    modifier: Modifier = Modifier,
    item: ButtonItem,
    onClick: (ButtonItem) -> Unit
) {
    FilterChip(
        modifier = modifier.height(36.dp),
        selected = item.selected,
        label = {
            StyledText(
                text = item.title,
                style = item.textStyle,
                maxLines = 1
            )
        },
        onClick = { onClick(item) }
    )
}
