package com.byteflipper.everbook.presentation.settings.reader.text.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.byteflipper.everbook.R
import com.byteflipper.everbook.domain.reader.ReaderTextAlignment
import com.byteflipper.everbook.domain.ui.ButtonItem
import com.byteflipper.everbook.presentation.core.components.settings.SegmentedButtonWithTitle
import com.byteflipper.everbook.ui.main.MainEvent
import com.byteflipper.everbook.ui.main.MainModel

@Composable
fun TextAlignmentOption() {
    val mainModel = hiltViewModel<MainModel>()
    val state = mainModel.state.collectAsStateWithLifecycle()

    SegmentedButtonWithTitle(
        title = stringResource(id = R.string.text_alignment_option),
        buttons = ReaderTextAlignment.entries.map {
            ButtonItem(
                id = it.toString(),
                title = when (it) {
                    ReaderTextAlignment.START -> stringResource(id = R.string.alignment_start)
                    ReaderTextAlignment.JUSTIFY -> stringResource(id = R.string.alignment_justify)
                    ReaderTextAlignment.CENTER -> stringResource(id = R.string.alignment_center)
                    ReaderTextAlignment.END -> stringResource(id = R.string.alignment_end)
                },
                textStyle = MaterialTheme.typography.labelLarge,
                selected = it == state.value.textAlignment
            )
        },
        onClick = {
            mainModel.onEvent(
                MainEvent.OnChangeTextAlignment(
                    it.id
                )
            )
        }
    )
}