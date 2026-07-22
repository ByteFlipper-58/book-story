/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.settings.reader.pdf.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.byteflipper.everbook.R
import com.byteflipper.everbook.domain.reader.PdfPageDisplayMode
import com.byteflipper.everbook.domain.ui.ButtonItem
import com.byteflipper.everbook.presentation.core.components.settings.SegmentedButtonWithTitle
import com.byteflipper.everbook.ui.main.MainEvent
import com.byteflipper.everbook.ui.main.MainModel

@Composable
fun PdfPageDisplayModeOption() {
    val mainModel = hiltViewModel<MainModel>()
    val state = mainModel.state.collectAsStateWithLifecycle()

    SegmentedButtonWithTitle(
        title = stringResource(id = R.string.pdf_page_display_mode_option),
        buttons = PdfPageDisplayMode.entries.map {
            ButtonItem(
                id = it.name,
                title = when (it) {
                    PdfPageDisplayMode.CONTINUOUS -> stringResource(
                        id = R.string.pdf_page_display_mode_continuous
                    )

                    PdfPageDisplayMode.PAGED -> stringResource(
                        id = R.string.pdf_page_display_mode_paged
                    )
                },
                textStyle = MaterialTheme.typography.labelLarge,
                selected = it == state.value.pdfPageDisplayMode
            )
        },
        onClick = {
            mainModel.onEvent(
                MainEvent.OnChangePdfPageDisplayMode(it.id)
            )
        }
    )
}
