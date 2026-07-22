/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.settings.reader.reading_mode.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.byteflipper.everbook.R
import com.byteflipper.everbook.domain.reader.PdfReadingMode
import com.byteflipper.everbook.presentation.core.components.settings.SwitchWithTitle
import com.byteflipper.everbook.ui.main.MainEvent
import com.byteflipper.everbook.ui.main.MainModel

@Composable
fun PdfReadingModeOption() {
    val mainModel = hiltViewModel<MainModel>()
    val state = mainModel.state.collectAsStateWithLifecycle()
    val preferOriginalPdf = state.value.pdfDefaultReadingMode == PdfReadingMode.ORIGINAL_PDF

    SwitchWithTitle(
        selected = preferOriginalPdf,
        title = stringResource(id = R.string.pdf_prefer_original_pdf_option),
        description = stringResource(id = R.string.pdf_prefer_original_pdf_option_desc),
        onClick = {
            mainModel.onEvent(
                MainEvent.OnChangePdfDefaultReadingMode(
                    if (preferOriginalPdf) PdfReadingMode.PARSED_TEXT.name
                    else PdfReadingMode.ORIGINAL_PDF.name
                )
            )
        }
    )
}
