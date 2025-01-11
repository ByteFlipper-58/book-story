package com.byteflipper.everbook.presentation.settings.reader.reading_speed.components

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
fun PerceptionExpanderPaddingOption() {
    val mainModel = hiltViewModel<MainModel>()
    val state = mainModel.state.collectAsStateWithLifecycle()

    ExpandingTransition(visible = state.value.perceptionExpander) {
        SliderWithTitle(
            value = state.value.perceptionExpanderPadding to "pt",
            fromValue = 0,
            toValue = 24,
            title = stringResource(id = R.string.perception_expander_padding_option),
            onValueChange = {
                mainModel.onEvent(
                    MainEvent.OnChangePerceptionExpanderPadding(it)
                )
            }
        )
    }
}