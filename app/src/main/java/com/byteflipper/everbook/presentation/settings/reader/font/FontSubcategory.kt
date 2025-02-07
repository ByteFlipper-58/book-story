@file:Suppress("FunctionName")

package com.byteflipper.everbook.presentation.settings.reader.font

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.byteflipper.everbook.R
import com.byteflipper.everbook.presentation.settings.components.SettingsSubcategory
import com.byteflipper.everbook.presentation.settings.reader.font.components.FontFamilyOption
import com.byteflipper.everbook.presentation.settings.reader.font.components.FontSizeOption
import com.byteflipper.everbook.presentation.settings.reader.font.components.FontStyleOption
import com.byteflipper.everbook.presentation.settings.reader.font.components.FontThicknessOption
import com.byteflipper.everbook.presentation.settings.reader.font.components.LetterSpacingOption

fun LazyListScope.FontSubcategory(
    titleColor: @Composable () -> Color = { MaterialTheme.colorScheme.primary },
    title: @Composable () -> String = { stringResource(id = R.string.font_reader_settings) },
    showTitle: Boolean = true,
    showDivider: Boolean = true,
) {
    SettingsSubcategory(
        titleColor = titleColor,
        title = title,
        showTitle = showTitle,
        showDivider = showDivider
    ) {
        item {
            FontFamilyOption()
        }

        item {
            FontThicknessOption()
        }

        item {
            FontStyleOption()
        }

        item {
            FontSizeOption()
        }

        item {
            LetterSpacingOption()
        }
    }
}