package com.byteflipper.everbook.presentation.settings.reader.system.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.byteflipper.everbook.R
import com.byteflipper.everbook.presentation.core.components.settings.SwitchWithTitle
import com.byteflipper.everbook.ui.main.MainEvent
import com.byteflipper.everbook.ui.main.MainModel

@Composable
fun CustomScreenBrightnessOption() {
    val mainModel = hiltViewModel<MainModel>()
    val state = mainModel.state.collectAsStateWithLifecycle()

    SwitchWithTitle(
        selected = state.value.customScreenBrightness,
        title = stringResource(id = R.string.custom_screen_brightness_option),
        onClick = {
            mainModel.onEvent(
                MainEvent.OnChangeCustomScreenBrightness(
                    !state.value.customScreenBrightness
                )
            )
        }
    )
}