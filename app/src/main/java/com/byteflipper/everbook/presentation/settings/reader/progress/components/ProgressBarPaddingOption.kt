package com.byteflipper.everbook.presentation.settings.reader.progress.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.byteflipper.everbook.R
import com.byteflipper.everbook.presentation.core.components.settings.SliderWithTitle
import com.byteflipper.everbook.ui.main.MainEvent
import com.byteflipper.everbook.ui.main.MainModel
import com.byteflipper.everbook.ui.theme.ExpandingTransition

@Composable
fun ProgressBarPaddingOption() {
    val mainModel = hiltViewModel<MainModel>()
    val state = mainModel.state.collectAsStateWithLifecycle()

    ExpandingTransition(visible = state.value.progressBar) {
        SliderWithTitle(
            value = state.value.progressBarPadding to "pt",
            fromValue = 1,
            toValue = 12,
            title = stringResource(id = R.string.progress_bar_padding_option),
            onValueChange = {
                mainModel.onEvent(
                    MainEvent.OnChangeProgressBarPadding(it)
                )
            }
        )
    }
}