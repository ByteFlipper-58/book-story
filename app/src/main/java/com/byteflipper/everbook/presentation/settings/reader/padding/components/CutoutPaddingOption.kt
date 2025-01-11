package com.byteflipper.everbook.presentation.settings.reader.padding.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.byteflipper.everbook.R
import com.byteflipper.everbook.presentation.core.components.settings.SwitchWithTitle
import com.byteflipper.everbook.ui.main.MainEvent
import com.byteflipper.everbook.ui.main.MainModel

@Composable
fun CutoutPaddingOption() {
    val mainModel = hiltViewModel<MainModel>()
    val state = mainModel.state.collectAsStateWithLifecycle()

    SwitchWithTitle(
        selected = state.value.cutoutPadding,
        title = stringResource(id = R.string.cutout_padding_option),
        description = stringResource(id = R.string.cutout_padding_option_desc),
        onClick = {
            mainModel.onEvent(
                MainEvent.OnChangeCutoutPadding(
                    !state.value.cutoutPadding
                )
            )
        }
    )
}