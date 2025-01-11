package com.byteflipper.everbook.presentation.settings.reader.images.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.byteflipper.everbook.R
import com.byteflipper.everbook.domain.reader.ReaderImagesAlignment
import com.byteflipper.everbook.domain.ui.ButtonItem
import com.byteflipper.everbook.presentation.core.components.settings.SegmentedButtonWithTitle
import com.byteflipper.everbook.ui.main.MainEvent
import com.byteflipper.everbook.ui.main.MainModel
import com.byteflipper.everbook.ui.theme.ExpandingTransition

@Composable
fun ImagesAlignmentOption() {
    val mainModel = hiltViewModel<MainModel>()
    val state = mainModel.state.collectAsStateWithLifecycle()

    ExpandingTransition(visible = state.value.images) {
        SegmentedButtonWithTitle(
            title = stringResource(id = R.string.images_alignment_option),
            buttons = ReaderImagesAlignment.entries.map {
                ButtonItem(
                    id = it.toString(),
                    title = when (it) {
                        ReaderImagesAlignment.START -> stringResource(id = R.string.alignment_start)
                        ReaderImagesAlignment.CENTER -> stringResource(id = R.string.alignment_center)
                        ReaderImagesAlignment.END -> stringResource(id = R.string.alignment_end)
                    },
                    textStyle = MaterialTheme.typography.labelLarge,
                    selected = it == state.value.imagesAlignment
                )
            },
            onClick = {
                mainModel.onEvent(
                    MainEvent.OnChangeImagesAlignment(
                        it.id
                    )
                )
            }
        )
    }
}