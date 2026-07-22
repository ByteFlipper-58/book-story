/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

@file:Suppress("FunctionName")

package com.byteflipper.everbook.presentation.settings.library.sort.components
import androidx.compose.ui.res.painterResource

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.byteflipper.everbook.R
import com.byteflipper.everbook.domain.library.display.LibrarySortOrder
import com.byteflipper.everbook.presentation.core.components.common.StyledText
import com.byteflipper.everbook.ui.main.MainEvent
import com.byteflipper.everbook.ui.main.MainModel

fun LazyListScope.LibrarySortOrderOption() {
    items(LibrarySortOrder.entries, key = { it.name }) { item ->
        val mainModel = hiltViewModel<MainModel>()
        val state = mainModel.state.collectAsStateWithLifecycle()

        LibrarySortOrderOptionItem(
            item = item,
            isSelected = state.value.librarySortOrder == item,
            isDescending = state.value.librarySortOrderDescending
        ) {
            if (state.value.librarySortOrder == item) {
                mainModel.onEvent(
                    MainEvent.OnChangeLibrarySortOrderDescending(
                        !state.value.librarySortOrderDescending
                    )
                )
            } else {
                mainModel.onEvent(MainEvent.OnChangeLibrarySortOrder(item.name))
                mainModel.onEvent(MainEvent.OnChangeLibrarySortOrderDescending(true))
            }
        }
    }
}

@Composable
private fun LibrarySortOrderOptionItem(
    item: LibrarySortOrder,
    isSelected: Boolean,
    isDescending: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = if (isDescending) painterResource(R.drawable.ic_arrow_downward_rounded_24px)
            else painterResource(R.drawable.ic_arrow_upward_rounded_24px),
            contentDescription = stringResource(id = R.string.sort_order_content_desc),
            modifier = Modifier.size(28.dp),
            tint = if (isSelected) MaterialTheme.colorScheme.secondary
            else Color.Transparent
        )
        Spacer(modifier = Modifier.width(24.dp))

        StyledText(
            text = stringResource(
                when (item) {
                    LibrarySortOrder.NAME -> R.string.sort_order_name
                    LibrarySortOrder.LAST_READ -> R.string.sort_order_last_read
                    LibrarySortOrder.PROGRESS -> R.string.sort_order_progress
                    LibrarySortOrder.AUTHOR -> R.string.sort_order_author
                }
            ),
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 1
        )
    }
}
