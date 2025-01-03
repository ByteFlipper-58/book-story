package com.byteflipper.everbook.presentation.screens.settings.nested.reader.components.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.byteflipper.everbook.R
import com.byteflipper.everbook.presentation.core.components.settings.SliderWithTitle
import com.byteflipper.everbook.presentation.data.MainEvent
import com.byteflipper.everbook.presentation.data.MainViewModel

/**
 * Paragraph Height setting.
 * Changes Reader's paragraph height.
 */
@Composable
fun ParagraphHeightSetting() {
    val state = MainViewModel.getState()
    val onMainEvent = MainViewModel.getEvent()

    SliderWithTitle(
        value = state.value.paragraphHeight to "pt",
        fromValue = 0,
        toValue = 36,
        title = stringResource(id = R.string.paragraph_height_option),
        onValueChange = {
            onMainEvent(
                MainEvent.OnChangeParagraphHeight(it)
            )
        }
    )
}