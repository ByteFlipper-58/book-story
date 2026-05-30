/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

@file:Suppress("FunctionName")

package com.byteflipper.everbook.presentation.settings.reader

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.byteflipper.everbook.presentation.settings.reader.chapters.ChaptersSubcategory
import com.byteflipper.everbook.presentation.settings.reader.experiments.ExperimentsSubcategory
import com.byteflipper.everbook.presentation.settings.reader.font.FontSubcategory
import com.byteflipper.everbook.presentation.settings.reader.images.ImagesSubcategory
import com.byteflipper.everbook.presentation.settings.reader.misc.MiscSubcategory
import com.byteflipper.everbook.presentation.settings.reader.padding.PaddingSubcategory
import com.byteflipper.everbook.presentation.settings.reader.pdf.PdfColorsSubcategory
import com.byteflipper.everbook.presentation.settings.reader.pdf.PdfDisplaySubcategory
import com.byteflipper.everbook.presentation.settings.reader.pdf.PdfPaddingSubcategory
import com.byteflipper.everbook.presentation.settings.reader.pdf.PdfSystemSubcategory
import com.byteflipper.everbook.presentation.settings.reader.progress.ProgressSubcategory
import com.byteflipper.everbook.presentation.settings.reader.reading_mode.ReadingModeSubcategory
import com.byteflipper.everbook.presentation.settings.reader.reading_speed.ReadingSpeedSubcategory
import com.byteflipper.everbook.presentation.settings.reader.system.SystemSubcategory
import com.byteflipper.everbook.presentation.settings.reader.text.TextSubcategory
import com.byteflipper.everbook.presentation.settings.reader.translator.TranslatorSubcategory

fun LazyListScope.ReaderSettingsCategory(
    titleColor: @Composable () -> Color = { MaterialTheme.colorScheme.primary }
) {
    FontSubcategory(
        titleColor = titleColor
    )
    TextSubcategory(
        titleColor = titleColor
    )
    ImagesSubcategory(
        titleColor = titleColor
    )
    ChaptersSubcategory(
        titleColor = titleColor
    )
    ReadingModeSubcategory(
        titleColor = titleColor
    )
    PdfDisplaySubcategory(
        titleColor = titleColor
    )
    PdfPaddingSubcategory(
        titleColor = titleColor
    )
    PdfSystemSubcategory(
        titleColor = titleColor
    )
    PdfColorsSubcategory(
        titleColor = titleColor
    )
    PaddingSubcategory(
        titleColor = titleColor
    )
    SystemSubcategory(
        titleColor = titleColor
    )
    ReadingSpeedSubcategory(
        titleColor = titleColor
    )
    ProgressSubcategory(
        titleColor = titleColor
    )
    TranslatorSubcategory(
        titleColor = titleColor
    )
    ExperimentsSubcategory(
        titleColor = titleColor
    )
    MiscSubcategory(
        titleColor = titleColor,
        showDivider = false
    )
}
