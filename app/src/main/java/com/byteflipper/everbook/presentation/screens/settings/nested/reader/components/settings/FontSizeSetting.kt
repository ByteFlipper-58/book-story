package com.byteflipper.everbook.presentation.screens.settings.nested.reader.components.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.byteflipper.everbook.R
import com.byteflipper.everbook.presentation.core.components.settings.SliderWithTitle
import com.byteflipper.everbook.presentation.data.MainEvent
import com.byteflipper.everbook.presentation.data.MainViewModel

/**
 * Font Size setting.
 * Changes Reader's font size.
 */
@Composable
fun FontSizeSetting() {
    val state = MainViewModel.getState()
    val onMainEvent = MainViewModel.getEvent()

    SliderWithTitle(
        value = state.value.fontSize to "pt",
        fromValue = 10,
        toValue = 35,
        title = stringResource(id = R.string.font_size_option),
        onValueChange = {
            onMainEvent(
                MainEvent.OnChangeFontSize(it)
            )
        }
    )
}