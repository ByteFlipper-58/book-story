package com.byteflipper.book_story.presentation.screens.settings.nested.reader.components.settings

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import com.byteflipper.book_story.R
import com.byteflipper.book_story.domain.model.ButtonItem
import com.byteflipper.book_story.presentation.core.components.settings.SegmentedButtonWithTitle
import com.byteflipper.book_story.presentation.core.constants.Constants
import com.byteflipper.book_story.presentation.core.constants.provideFonts
import com.byteflipper.book_story.presentation.data.MainEvent
import com.byteflipper.book_story.presentation.data.MainViewModel

/**
 * Font Style setting.
 * Changes Reader's font style (Normal/Italic).
 */
@Composable
fun FontStyleSetting() {
    val state = MainViewModel.getState()
    val onMainEvent = MainViewModel.getEvent()

    val fontFamily = remember(state.value.fontFamily) {
        Constants.provideFonts(withRandom = false).find {
            it.id == state.value.fontFamily
        } ?: Constants.provideFonts(withRandom = false)[0]
    }

    SegmentedButtonWithTitle(
        title = stringResource(id = R.string.font_style_option),
        buttons = listOf(
            ButtonItem(
                id = "normal",
                title = stringResource(id = R.string.font_style_normal),
                textStyle = MaterialTheme.typography.labelLarge.copy(
                    fontFamily = fontFamily.font,
                    fontStyle = FontStyle.Normal
                ),
                selected = !state.value.isItalic
            ),
            ButtonItem(
                id = "italic",
                title = stringResource(id = R.string.font_style_italic),
                textStyle = MaterialTheme.typography.labelLarge.copy(
                    fontFamily = fontFamily.font,
                    fontStyle = FontStyle.Italic
                ),
                selected = state.value.isItalic
            ),
        ),
        onClick = {
            onMainEvent(
                MainEvent.OnChangeFontStyle(
                    when (it.id) {
                        "italic" -> true
                        else -> false
                    }
                )
            )
        }
    )
}