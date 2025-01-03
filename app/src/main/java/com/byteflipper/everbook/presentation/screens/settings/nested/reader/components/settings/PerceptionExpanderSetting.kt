package com.byteflipper.everbook.presentation.screens.settings.nested.reader.components.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.byteflipper.everbook.R
import com.byteflipper.everbook.presentation.core.components.settings.SwitchWithTitle
import com.byteflipper.everbook.presentation.data.MainEvent
import com.byteflipper.everbook.presentation.data.MainViewModel

/**
 * Perception Expander setting.
 * If true, shows vertical lines in Reader, which makes you read faster.
 */
@Composable
fun PerceptionExpanderSetting() {
    val state = MainViewModel.getState()
    val onMainEvent = MainViewModel.getEvent()

    SwitchWithTitle(
        selected = state.value.perceptionExpander,
        title = stringResource(id = R.string.perception_expander_option),
        description = stringResource(id = R.string.perception_expander_option_desc),
        onClick = {
            onMainEvent(
                MainEvent.OnChangePerceptionExpander(
                    !state.value.perceptionExpander
                )
            )
        }
    )
}