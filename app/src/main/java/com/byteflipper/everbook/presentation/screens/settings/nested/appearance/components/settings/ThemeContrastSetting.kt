package com.byteflipper.everbook.presentation.screens.settings.nested.appearance.components.settings

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.byteflipper.everbook.R
import com.byteflipper.everbook.domain.model.ButtonItem
import com.byteflipper.everbook.presentation.core.components.settings.SegmentedButtonWithTitle
import com.byteflipper.everbook.presentation.data.MainEvent
import com.byteflipper.everbook.presentation.data.MainViewModel
import com.byteflipper.everbook.presentation.ui.BookStoryTheme
import com.byteflipper.everbook.presentation.ui.ExpandingTransition
import com.byteflipper.everbook.presentation.ui.ThemeContrast
import com.byteflipper.everbook.presentation.ui.isDark
import com.byteflipper.everbook.presentation.ui.isPureDark

/**
 * Theme Contrast setting.
 * Lets user change theme contrast levels.
 */
@Composable
fun ThemeContrastSetting() {
    val state = MainViewModel.getState()
    val onMainEvent = MainViewModel.getEvent()

    val themeContrastTheme = remember { mutableStateOf(state.value.theme) }

    LaunchedEffect(state.value.theme) {
        if (state.value.theme.hasThemeContrast) {
            themeContrastTheme.value = state.value.theme
        }
    }

    BookStoryTheme(
        theme = themeContrastTheme.value,
        isDark = state.value.darkTheme.isDark(),
        isPureDark = state.value.pureDark.isPureDark(context = LocalContext.current),
        themeContrast = state.value.themeContrast
    ) {
        ExpandingTransition(visible = state.value.theme.hasThemeContrast) {
            SegmentedButtonWithTitle(
                title = stringResource(id = R.string.theme_contrast_option),
                enabled = state.value.theme.hasThemeContrast,
                buttons = ThemeContrast.entries.map {
                    ButtonItem(
                        id = it.toString(),
                        title = when (it) {
                            ThemeContrast.STANDARD -> stringResource(id = R.string.theme_contrast_standard)
                            ThemeContrast.MEDIUM -> stringResource(id = R.string.theme_contrast_medium)
                            ThemeContrast.HIGH -> stringResource(id = R.string.theme_contrast_high)
                        },
                        textStyle = MaterialTheme.typography.labelLarge,
                        selected = it == state.value.themeContrast
                    )
                }
            ) {
                onMainEvent(
                    MainEvent.OnChangeThemeContrast(
                        it.id
                    )
                )
            }
        }
    }
}