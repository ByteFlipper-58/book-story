package com.byteflipper.everbook.presentation.settings.reader.font.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.byteflipper.everbook.R
import com.byteflipper.everbook.presentation.core.components.settings.SliderWithTitle
import com.byteflipper.everbook.ui.main.MainEvent
import com.byteflipper.everbook.ui.main.MainModel

@Composable
fun FontSizeOption() {
    val mainModel = hiltViewModel<MainModel>()
    val state = mainModel.state.collectAsStateWithLifecycle()

    SliderWithTitle(
        value = state.value.fontSize to "pt",
        fromValue = 10,
        toValue = 35,
        title = stringResource(id = R.string.font_size_option),
        onValueChange = {
            mainModel.onEvent(
                MainEvent.OnChangeFontSize(it)
            )
        }
    )
}