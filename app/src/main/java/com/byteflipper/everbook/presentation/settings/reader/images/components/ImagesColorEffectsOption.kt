package com.byteflipper.everbook.presentation.settings.reader.images.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.byteflipper.everbook.R
import com.byteflipper.everbook.domain.reader.ReaderColorEffects
import com.byteflipper.everbook.domain.ui.ButtonItem
import com.byteflipper.everbook.presentation.core.components.settings.ChipsWithTitle
import com.byteflipper.everbook.ui.main.MainEvent
import com.byteflipper.everbook.ui.main.MainModel
import com.byteflipper.everbook.ui.theme.ExpandingTransition

@Composable
fun ImagesColorEffectsOption() {
    val mainModel = hiltViewModel<MainModel>()
    val state = mainModel.state.collectAsStateWithLifecycle()

    ExpandingTransition(visible = state.value.images) {
        ChipsWithTitle(
            title = stringResource(id = R.string.images_color_effects_option),
            chips = ReaderColorEffects.entries.map {
                ButtonItem(
                    id = it.toString(),
                    title = when (it) {
                        ReaderColorEffects.OFF -> {
                            stringResource(R.string.color_effects_off)
                        }

                        ReaderColorEffects.GRAYSCALE -> {
                            stringResource(R.string.color_effects_grayscale)
                        }

                        ReaderColorEffects.FONT -> {
                            stringResource(R.string.color_effects_font)
                        }

                        ReaderColorEffects.BACKGROUND -> {
                            stringResource(R.string.color_effects_background)
                        }
                    },
                    textStyle = MaterialTheme.typography.labelLarge,
                    selected = it == state.value.imagesColorEffects
                )
            },
            onClick = {
                mainModel.onEvent(
                    MainEvent.OnChangeImagesColorEffects(
                        it.id
                    )
                )
            }
        )
    }
}