@file:Suppress("FunctionName")

package com.byteflipper.everbook.presentation.screens.settings.nested.reader.components

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.byteflipper.everbook.presentation.screens.settings.components.SettingsCategoryTitle
import com.byteflipper.everbook.presentation.screens.settings.nested.reader.components.subcategories.FontSubcategory
import com.byteflipper.everbook.presentation.screens.settings.nested.reader.components.subcategories.MiscSubcategory
import com.byteflipper.everbook.presentation.screens.settings.nested.reader.components.subcategories.PaddingSubcategory
import com.byteflipper.everbook.presentation.screens.settings.nested.reader.components.subcategories.ReadingSpeedSubcategory
import com.byteflipper.everbook.presentation.screens.settings.nested.reader.components.subcategories.SystemSubcategory
import com.byteflipper.everbook.presentation.screens.settings.nested.reader.components.subcategories.TextSubcategory
import com.byteflipper.everbook.presentation.screens.settings.nested.reader.components.subcategories.TranslatorSubcategory

/**
 * Reader Settings Category.
 * Contains all Reader settings.
 *
 * @param titleColor Color that [SettingsCategoryTitle] has.
 * @param topPadding Top padding to be applied.
 * @param bottomPadding Bottom padding to be applied.
 */
fun LazyListScope.ReaderSettingsCategory(
    titleColor: @Composable () -> Color = { MaterialTheme.colorScheme.primary },
    topPadding: Dp = 16.dp,
    bottomPadding: Dp = 48.dp
) {
    FontSubcategory(
        titleColor = titleColor,
        topPadding = topPadding,
        bottomPadding = 0.dp
    )
    TextSubcategory(
        titleColor = titleColor,
        topPadding = 22.dp,
        bottomPadding = 0.dp
    )
    PaddingSubcategory(
        titleColor = titleColor,
        topPadding = 22.dp,
        bottomPadding = 0.dp
    )
    SystemSubcategory(
        titleColor = titleColor,
        topPadding = 22.dp,
        bottomPadding = 0.dp
    )
    ReadingSpeedSubcategory(
        titleColor = titleColor,
        topPadding = 22.dp,
        bottomPadding = 0.dp
    )
    TranslatorSubcategory(
        titleColor = titleColor,
        topPadding = 22.dp,
        bottomPadding = 0.dp
    )
    MiscSubcategory(
        titleColor = titleColor,
        showDivider = false,
        topPadding = 22.dp,
        bottomPadding = bottomPadding
    )
}