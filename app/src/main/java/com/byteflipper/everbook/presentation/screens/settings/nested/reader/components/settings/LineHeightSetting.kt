package com.byteflipper.everbook.presentation.screens.settings.nested.reader.components.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.byteflipper.everbook.R
import com.byteflipper.everbook.presentation.core.components.settings.SliderWithTitle
import com.byteflipper.everbook.presentation.data.MainEvent
import com.byteflipper.everbook.presentation.data.MainViewModel

/**
 * Line Height setting.
 * Changes Reader's line height.
 */
@Composable
fun LineHeightSetting() {
    val state = MainViewModel.getState()
    val onMainEvent = MainViewModel.getEvent()

    SliderWithTitle(
        value = state.value.lineHeight to "pt",
        fromValue = 1,
        toValue = 24,
        title = stringResource(id = R.string.line_height_option),
        onValueChange = {
            onMainEvent(
                MainEvent.OnChangeLineHeight(it)
            )
        }
    )
}