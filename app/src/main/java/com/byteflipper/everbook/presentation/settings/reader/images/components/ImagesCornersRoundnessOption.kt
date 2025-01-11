package com.byteflipper.everbook.presentation.settings.reader.images.components

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
fun ImagesCornersRoundnessOption() {
    val mainModel = hiltViewModel<MainModel>()
    val state = mainModel.state.collectAsStateWithLifecycle()

    ExpandingTransition(visible = state.value.images) {
        SliderWithTitle(
            value = state.value.imagesCornersRoundness to "pt",
            fromValue = 0,
            toValue = 24,
            title = stringResource(id = R.string.images_corners_roundness_option),
            onValueChange = {
                mainModel.onEvent(
                    MainEvent.OnChangeImagesCornersRoundness(it)
                )
            }
        )
    }
}