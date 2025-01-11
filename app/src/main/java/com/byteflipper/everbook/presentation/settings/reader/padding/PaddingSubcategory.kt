@file:Suppress("FunctionName")

package com.byteflipper.everbook.presentation.settings.reader.padding

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.byteflipper.everbook.R
import com.byteflipper.everbook.presentation.settings.components.SettingsSubcategory
import com.byteflipper.everbook.presentation.settings.reader.padding.components.BottomBarPaddingOption
import com.byteflipper.everbook.presentation.settings.reader.padding.components.CutoutPaddingOption
import com.byteflipper.everbook.presentation.settings.reader.padding.components.SidePaddingOption
import com.byteflipper.everbook.presentation.settings.reader.padding.components.VerticalPaddingOption

fun LazyListScope.PaddingSubcategory(
    titleColor: @Composable () -> Color = { MaterialTheme.colorScheme.primary },
    title: @Composable () -> String = { stringResource(id = R.string.padding_reader_settings) },
    showTitle: Boolean = true,
    showDivider: Boolean = true
) {
    SettingsSubcategory(
        titleColor = titleColor,
        title = title,
        showTitle = showTitle,
        showDivider = showDivider
    ) {
        item {
            SidePaddingOption()
        }

        item {
            VerticalPaddingOption()
        }

        item {
            CutoutPaddingOption()
        }

        item {
            BottomBarPaddingOption()
        }
    }
}