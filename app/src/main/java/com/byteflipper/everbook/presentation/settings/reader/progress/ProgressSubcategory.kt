@file:Suppress("FunctionName")

package com.byteflipper.everbook.presentation.settings.reader.progress

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.byteflipper.everbook.R
import com.byteflipper.everbook.presentation.settings.components.SettingsSubcategory
import com.byteflipper.everbook.presentation.settings.reader.progress.components.ProgressBarAlignmentOption
import com.byteflipper.everbook.presentation.settings.reader.progress.components.ProgressBarOption
import com.byteflipper.everbook.presentation.settings.reader.progress.components.ProgressBarPaddingOption

fun LazyListScope.ProgressSubcategory(
    titleColor: @Composable () -> Color = { MaterialTheme.colorScheme.primary },
    title: @Composable () -> String = { stringResource(id = R.string.progress_reader_settings) },
    showTitle: Boolean = true,
    showDivider: Boolean = true
) {
    SettingsSubcategory(
        titleColor = titleColor,
        title = title,
        showTitle = showTitle,
        showDivider = showDivider,
    ) {
        item {
            ProgressBarOption()
        }

        item {
            ProgressBarPaddingOption()
        }

        item {
            ProgressBarAlignmentOption()
        }
    }
}