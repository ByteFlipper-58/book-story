/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.book_info

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.byteflipper.everbook.R
import com.byteflipper.everbook.domain.library.book.Book
import com.byteflipper.everbook.presentation.core.components.common.StyledText
import com.byteflipper.everbook.presentation.core.util.calculateProgress

@Composable
fun BookInfoLayoutButton(
    book: Book,
    navigateToReader: () -> Unit
) {
    Button(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp),
        shape = CircleShape,
        onClick = {
            if (book.id != -1) {
                navigateToReader()
            }
        }
    ) {
        StyledText(
            text = if (book.progress == 0f) stringResource(id = R.string.start_reading)
            else stringResource(
                id = R.string.continue_reading_query,
                "${book.progress.calculateProgress(1)}%"
            )
        )
    }
}