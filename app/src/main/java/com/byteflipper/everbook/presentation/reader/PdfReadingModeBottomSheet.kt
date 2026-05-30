/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.reader

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Subject
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.byteflipper.everbook.R
import com.byteflipper.everbook.domain.library.book.Book
import com.byteflipper.everbook.domain.reader.PdfReadingMode
import com.byteflipper.everbook.presentation.core.components.common.StyledText
import com.byteflipper.everbook.presentation.core.components.modal_bottom_sheet.ModalBottomSheet
import com.byteflipper.everbook.presentation.settings.components.SettingsSubcategoryTitle

@Composable
fun PdfReadingModeBottomSheet(
    book: Book,
    pdfTextModeUnavailable: Boolean,
    changePdfReadingMode: (PdfReadingMode) -> Unit,
    changePdfDefaultReadingMode: (PdfReadingMode) -> Unit,
    dismissBottomSheet: () -> Unit
) {
    ModalBottomSheet(
        modifier = Modifier.fillMaxWidth(),
        onDismissRequest = dismissBottomSheet,
        sheetGesturesEnabled = true
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(bottom = 8.dp)
        ) {
            item {
                SettingsSubcategoryTitle(
                    title = stringResource(id = R.string.pdf_reading_mode_bottom_sheet_title),
                    padding = 18.dp,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                PdfReadingModeBottomSheetItem(
                    icon = Icons.AutoMirrored.Filled.Subject,
                    title = stringResource(id = R.string.pdf_reading_mode_parsed_text),
                    description = stringResource(id = R.string.pdf_reading_mode_text_version_desc),
                    selected = book.pdfReadingMode == PdfReadingMode.PARSED_TEXT,
                    enabled = !pdfTextModeUnavailable,
                    onClick = {
                        changePdfReadingMode(PdfReadingMode.PARSED_TEXT)
                        changePdfDefaultReadingMode(PdfReadingMode.PARSED_TEXT)
                        dismissBottomSheet()
                    }
                )
            }

            item {
                PdfReadingModeBottomSheetItem(
                    icon = Icons.Default.PictureAsPdf,
                    title = stringResource(id = R.string.pdf_reading_mode_original_pdf),
                    description = stringResource(id = R.string.pdf_reading_mode_original_pdf_desc),
                    selected = book.pdfReadingMode == PdfReadingMode.ORIGINAL_PDF,
                    enabled = true,
                    onClick = {
                        changePdfReadingMode(PdfReadingMode.ORIGINAL_PDF)
                        changePdfDefaultReadingMode(PdfReadingMode.ORIGINAL_PDF)
                        dismissBottomSheet()
                    }
                )
            }
        }
    }
}

@Composable
private fun LazyItemScope.PdfReadingModeBottomSheetItem(
    icon: ImageVector,
    title: String,
    description: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val contentColor = if (enabled) {
        MaterialTheme.colorScheme.onSurface
    } else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
    val descriptionColor = if (enabled) {
        MaterialTheme.colorScheme.onSurfaceVariant
    } else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)

    Row(
        modifier = Modifier
            .animateItem()
            .fillMaxWidth()
            .clickable(enabled = enabled && !selected) {
                onClick()
            }
            .padding(vertical = 12.dp, horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = when {
                !enabled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                selected -> MaterialTheme.colorScheme.secondary
                else -> MaterialTheme.colorScheme.tertiary
            }
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            StyledText(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = contentColor,
                    fontSize = 18.sp
                )
            )

            StyledText(
                text = description,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = descriptionColor
                )
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        RadioButton(
            selected = selected,
            onClick = null,
            modifier = Modifier.size(24.dp),
            enabled = enabled,
            colors = RadioButtonDefaults.colors(
                selectedColor = MaterialTheme.colorScheme.secondary,
                unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }
}
