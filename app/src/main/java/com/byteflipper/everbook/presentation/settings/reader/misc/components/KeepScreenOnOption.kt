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
fun KeepScreenOnOption() {
    val mainModel = hiltViewModel<MainModel>()
    val state = mainModel.state.collectAsStateWithLifecycle()

    SwitchWithTitle(
        selected = state.value.keepScreenOn,
        title = stringResource(id = R.string.keep_screen_on_option),
        onClick = {
            mainModel.onEvent(
                MainEvent.OnChangeKeepScreenOn(
                    !state.value.keepScreenOn
                )
            )
        }
    )
}