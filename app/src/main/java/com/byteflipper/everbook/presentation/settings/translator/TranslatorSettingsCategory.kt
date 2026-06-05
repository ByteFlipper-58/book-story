/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

@file:Suppress("FunctionName")

package com.byteflipper.everbook.presentation.settings.translator

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import com.byteflipper.everbook.presentation.settings.translator.components.TranslationProviderOption
import com.byteflipper.everbook.presentation.settings.translator.components.TranslationSourceLanguageOption
import com.byteflipper.everbook.presentation.settings.translator.components.TranslationTargetLanguageOption
import com.byteflipper.everbook.presentation.settings.translator.components.TranslationWifiOnlyOption

fun LazyListScope.TranslatorSettingsCategory(
    topPadding: Dp = 16.dp,
    bottomPadding: Dp = 16.dp
) {
    item {
        Spacer(modifier = Modifier.height((topPadding - 8.dp).coerceAtLeast(0.dp)))
    }

    item {
        TranslationProviderOption()
    }
    item {
        TranslationSourceLanguageOption()
    }
    item {
        TranslationTargetLanguageOption()
    }
    item {
        TranslationWifiOnlyOption()
    }

    item {
        Spacer(modifier = Modifier.height(bottomPadding))
    }
}
