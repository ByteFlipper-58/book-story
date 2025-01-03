@file:Suppress("FunctionName")

package com.byteflipper.everbook.presentation.screens.settings.nested.reader.components.subcategories

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import com.byteflipper.everbook.R
import com.byteflipper.everbook.presentation.screens.settings.components.SettingsSubcategory
import com.byteflipper.everbook.presentation.screens.settings.nested.reader.components.settings.PerceptionExpanderPaddingSetting
import com.byteflipper.everbook.presentation.screens.settings.nested.reader.components.settings.PerceptionExpanderSetting
import com.byteflipper.everbook.presentation.screens.settings.nested.reader.components.settings.PerceptionExpanderThicknessSetting

/**
 * Reading Speed subcategory.
 * Contains all settings that can improve your reading speed.
 */
fun LazyListScope.ReadingSpeedSubcategory(
    titleColor: @Composable () -> Color = { MaterialTheme.colorScheme.primary },
    title: @Composable () -> String = { stringResource(id = R.string.reading_speed_reader_settings) },
    showTitle: Boolean = true,
    showDivider: Boolean = true,
    topPadding: Dp,
    bottomPadding: Dp
) {
    SettingsSubcategory(
        titleColor = titleColor,
        title = title,
        showTitle = showTitle,
        showDivider = showDivider,
        topPadding = topPadding,
        bottomPadding = bottomPadding
    ) {
        item {
            PerceptionExpanderSetting()
        }

        item {
            PerceptionExpanderPaddingSetting()
        }

        item {
            PerceptionExpanderThicknessSetting()
        }
    }
}