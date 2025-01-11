@file:Suppress("FunctionName")

package com.byteflipper.everbook.presentation.start

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.ui.res.stringResource
import com.byteflipper.everbook.R
import com.byteflipper.everbook.presentation.settings.appearance.theme_preferences.ThemePreferencesSubcategory

fun LazyListScope.StartSettingsLayoutAppearance() {
    ThemePreferencesSubcategory(
        title = { stringResource(id = R.string.start_theme_preferences) },
        showDivider = false
    )
}