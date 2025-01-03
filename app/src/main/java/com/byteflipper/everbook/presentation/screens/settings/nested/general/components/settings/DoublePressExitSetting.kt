package com.byteflipper.everbook.presentation.screens.settings.nested.general.components.settings

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.byteflipper.everbook.R
import com.byteflipper.everbook.presentation.core.components.settings.SwitchWithTitle
import com.byteflipper.everbook.presentation.data.MainEvent
import com.byteflipper.everbook.presentation.data.MainViewModel

/**
 * Double Press Exit setting.
 * If true, to close the app you need to press back twice.
 */
@SuppressLint("InlinedApi")
@Composable
fun DoublePressExitSetting() {
    val state = MainViewModel.getState()
    val onMainEvent = MainViewModel.getEvent()

    SwitchWithTitle(
        selected = state.value.doublePressExit,
        title = stringResource(id = R.string.double_press_exit_option),
        description = stringResource(id = R.string.double_press_exit_option_desc)
    ) {
        onMainEvent(
            MainEvent.OnChangeDoublePressExit(!state.value.doublePressExit)
        )
    }
}