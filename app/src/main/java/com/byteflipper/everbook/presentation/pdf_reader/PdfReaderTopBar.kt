/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.pdf_reader

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.byteflipper.everbook.R
import com.byteflipper.everbook.domain.library.book.Book
import com.byteflipper.everbook.presentation.core.components.common.IconButton
import com.byteflipper.everbook.presentation.core.components.common.StyledText
import com.byteflipper.everbook.presentation.core.util.LocalActivity
import com.byteflipper.everbook.presentation.core.util.noRippleClickable
import com.byteflipper.everbook.ui.pdf_reader.PdfReaderEvent
import com.byteflipper.everbook.ui.theme.readerBarsColor

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PdfReaderTopBar(
    book: Book,
    showPdfReadingModeBottomSheet: (PdfReaderEvent.OnShowPdfReadingModeBottomSheet) -> Unit,
    showSettingsBottomSheet: (PdfReaderEvent.OnShowSettingsBottomSheet) -> Unit,
    leave: (PdfReaderEvent.OnLeave) -> Unit,
    navigateBack: () -> Unit,
    navigateToBookInfo: () -> Unit
) {
    val activity = LocalActivity.current
    Column(
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.readerBarsColor)
            .noRippleClickable {}
    ) {
        TopAppBar(
            navigationIcon = {
                IconButton(
                    icon = R.drawable.ic_arrow_back_rounded_24px,
                    contentDescription = R.string.go_back_content_desc,
                    disableOnClick = true
                ) {
                    leave(
                        PdfReaderEvent.OnLeave(
                            activity = activity,
                            navigate = navigateBack
                        )
                    )
                }
            },
            title = {
                StyledText(
                    text = book.title,
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .clickable { navigateToBookInfo() },
                    style = LocalTextStyle.current.copy(
                        fontSize = 20.sp,
                        lineHeight = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    maxLines = 1
                )
            },
            subtitle = {
                StyledText(
                    text = stringResource(id = R.string.pdf_reading_mode_original_pdf),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    maxLines = 1
                )
            },
            actions = {
                if (book.filePath.endsWith(".pdf", ignoreCase = true)) {
                    IconButton(
                        icon = R.drawable.ic_subject_rounded_24px,
                        contentDescription = R.string.pdf_reading_mode_option,
                        disableOnClick = false
                    ) {
                        showPdfReadingModeBottomSheet(
                            PdfReaderEvent.OnShowPdfReadingModeBottomSheet
                        )
                    }
                }

                IconButton(
                    icon = R.drawable.ic_settings_rounded_24px,
                    contentDescription = R.string.open_reader_settings_content_desc,
                    disableOnClick = false
                ) {
                    showSettingsBottomSheet(PdfReaderEvent.OnShowSettingsBottomSheet)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent
            )
        )
    }
}
