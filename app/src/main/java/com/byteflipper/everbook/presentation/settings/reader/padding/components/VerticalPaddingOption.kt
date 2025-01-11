package com.byteflipper.everbook.presentation.settings.reader.padding.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.byteflipper.everbook.R
import com.byteflipper.everbook.presentation.core.components.settings.SliderWithTitle
import com.byteflipper.everbook.ui.main.MainEvent
import com.byteflipper.everbook.ui.main.MainModel

@Composable
fun VerticalPaddingOption() {
    val mainModel = hiltViewModel<MainModel>()
    val state = mainModel.state.collectAsStateWithLifecycle()

    SliderWithTitle(
        value = state.value.verticalPadding to "pt",
        fromValue = 0,
        toValue = 24,
        title = stringResource(id = R.string.vertical_padding_option),
        onValueChange = {
            mainModel.onEvent(
                MainEvent.OnChangeVerticalPadding(it)
            )
        }
    )
}