@file:Suppress("FunctionName")

package com.byteflipper.everbook.presentation.settings.reader.images

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.byteflipper.everbook.R
import com.byteflipper.everbook.presentation.settings.components.SettingsSubcategory
import com.byteflipper.everbook.presentation.settings.reader.images.components.ImagesAlignmentOption
import com.byteflipper.everbook.presentation.settings.reader.images.components.ImagesColorEffectsOption
import com.byteflipper.everbook.presentation.settings.reader.images.components.ImagesCornersRoundnessOption
import com.byteflipper.everbook.presentation.settings.reader.images.components.ImagesOption
import com.byteflipper.everbook.presentation.settings.reader.images.components.ImagesWidthOption

fun LazyListScope.ImagesSubcategory(
    titleColor: @Composable () -> Color = { MaterialTheme.colorScheme.primary },
    title: @Composable () -> String = { stringResource(id = R.string.images_reader_settings) },
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
            ImagesOption()
        }

        item {
            ImagesColorEffectsOption()
        }

        item {
            ImagesCornersRoundnessOption()
        }

        item {
            ImagesAlignmentOption()
        }

        item {
            ImagesWidthOption()
        }
    }
}