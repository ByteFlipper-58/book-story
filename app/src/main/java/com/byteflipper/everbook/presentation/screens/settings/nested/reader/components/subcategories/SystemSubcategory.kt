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
import com.byteflipper.everbook.presentation.screens.settings.nested.reader.components.settings.CustomScreenBrightnessSetting
import com.byteflipper.everbook.presentation.screens.settings.nested.reader.components.settings.ScreenBrightnessSetting
import com.byteflipper.everbook.presentation.screens.settings.nested.reader.components.settings.ScreenOrientationSetting

/**
 * System subcategory.
 * Contains all settings that are related to system (e.g. screen orientation).
 */
fun LazyListScope.SystemSubcategory(
    titleColor: @Composable () -> Color = { MaterialTheme.colorScheme.primary },
    title: @Composable () -> String = { stringResource(id = R.string.system_reader_settings) },
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
            CustomScreenBrightnessSetting()
        }

        item {
            ScreenBrightnessSetting()
        }

        item {
            ScreenOrientationSetting()
        }
    }
}