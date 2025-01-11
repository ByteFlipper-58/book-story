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
fun SidePaddingOption() {
    val mainModel = hiltViewModel<MainModel>()
    val state = mainModel.state.collectAsStateWithLifecycle()

    SliderWithTitle(
        value = state.value.sidePadding to "pt",
        fromValue = 1,
        toValue = 20,
        title = stringResource(id = R.string.side_padding_option),
        onValueChange = {
            mainModel.onEvent(
                MainEvent.OnChangeSidePadding(it)
            )
        }
    )
}