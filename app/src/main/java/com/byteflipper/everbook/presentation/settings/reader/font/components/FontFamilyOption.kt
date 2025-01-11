package com.byteflipper.everbook.presentation.settings.reader.font.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.byteflipper.everbook.R
import com.byteflipper.everbook.domain.ui.ButtonItem
import com.byteflipper.everbook.presentation.core.components.settings.ChipsWithTitle
import com.byteflipper.everbook.presentation.core.constants.Constants
import com.byteflipper.everbook.presentation.core.constants.provideFonts
import com.byteflipper.everbook.ui.main.MainEvent
import com.byteflipper.everbook.ui.main.MainModel

@Composable
fun FontFamilyOption() {
    val mainModel = hiltViewModel<MainModel>()
    val state = mainModel.state.collectAsStateWithLifecycle()

    val fontFamily = remember(state.value.fontFamily) {
        Constants.provideFonts(withRandom = true).find {
            it.id == state.value.fontFamily
        } ?: Constants.provideFonts(withRandom = false)[0]
    }

    ChipsWithTitle(
        title = stringResource(id = R.string.font_family_option),
        chips = Constants.provideFonts(withRandom = true)
            .map {
                ButtonItem(
                    id = it.id,
                    title = it.fontName.asString(),
                    textStyle = MaterialTheme.typography.labelLarge.copy(
                        fontFamily = when (it.id) {
                            "random" -> FontFamily.Default
                            else -> it.font
                        }
                    ),
                    selected = it.id == fontFamily.id
                )
            },
        onClick = {
            mainModel.onEvent(MainEvent.OnChangeFontFamily(it.id))
        }
    )
}