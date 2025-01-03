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
import com.byteflipper.everbook.presentation.screens.settings.nested.reader.components.settings.LineHeightSetting
import com.byteflipper.everbook.presentation.screens.settings.nested.reader.components.settings.ParagraphHeightSetting
import com.byteflipper.everbook.presentation.screens.settings.nested.reader.components.settings.ParagraphIndentationSetting
import com.byteflipper.everbook.presentation.screens.settings.nested.reader.components.settings.TextAlignmentSetting

/**
 * Text subcategory.
 * Contains all settings from Text.
 */
fun LazyListScope.TextSubcategory(
    titleColor: @Composable () -> Color = { MaterialTheme.colorScheme.primary },
    title: @Composable () -> String = { stringResource(id = R.string.text_reader_settings) },
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
            TextAlignmentSetting()
        }

        item {
            LineHeightSetting()
        }

        item {
            ParagraphHeightSetting()
        }

        item {
            ParagraphIndentationSetting()
        }
    }
}