package com.byteflipper.everbook.presentation.settings.reader.misc.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.byteflipper.everbook.R
import com.byteflipper.everbook.presentation.core.components.settings.SwitchWithTitle
import com.byteflipper.everbook.ui.main.MainEvent
import com.byteflipper.everbook.ui.main.MainModel

@Composable
fun FullscreenOption() {
    val mainModel = hiltViewModel<MainModel>()
    val state = mainModel.state.collectAsStateWithLifecycle()

    SwitchWithTitle(
        selected = state.value.fullscreen,
        title = stringResource(id = R.string.fullscreen_option),
        description = stringResource(id = R.string.fullscreen_option_desc),
        onClick = {
            mainModel.onEvent(
                MainEvent.OnChangeFullscreen(
                    !state.value.fullscreen
                )
            )
        }
    )
}