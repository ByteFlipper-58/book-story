package com.byteflipper.book_story.presentation.screens.settings.nested.reader.components.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.byteflipper.book_story.R
import com.byteflipper.book_story.presentation.core.components.settings.SwitchWithTitle
import com.byteflipper.book_story.presentation.data.MainEvent
import com.byteflipper.book_story.presentation.data.MainViewModel

/**
 * Keep Screen On setting.
 * If true, keeps screen awake in Reader.
 */
@Composable
fun KeepScreenOnSetting() {
    val state = MainViewModel.getState()
    val onMainEvent = MainViewModel.getEvent()

    SwitchWithTitle(
        selected = state.value.keepScreenOn,
        title = stringResource(id = R.string.keep_screen_on_option),
        onClick = {
            onMainEvent(
                MainEvent.OnChangeKeepScreenOn(
                    !state.value.keepScreenOn
                )
            )
        }
    )
}