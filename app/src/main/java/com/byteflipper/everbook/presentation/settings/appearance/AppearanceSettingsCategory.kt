@file:Suppress("FunctionName")

package com.byteflipper.everbook.presentation.settings.appearance

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.byteflipper.everbook.presentation.settings.appearance.colors.ColorsSubcategory
import com.byteflipper.everbook.presentation.settings.appearance.theme_preferences.ThemePreferencesSubcategory

fun LazyListScope.AppearanceSettingsCategory(
    titleColor: @Composable () -> Color = { MaterialTheme.colorScheme.primary }
) {
    ThemePreferencesSubcategory(
        titleColor = titleColor
    )
    ColorsSubcategory(
        titleColor = titleColor,
        showDivider = false
    )
}