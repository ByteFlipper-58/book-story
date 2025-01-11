package com.byteflipper.everbook.ui.settings

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Immutable
import com.byteflipper.everbook.domain.reader.ColorPreset
import com.byteflipper.everbook.presentation.core.constants.Constants
import com.byteflipper.everbook.presentation.core.constants.provideDefaultColorPreset

@Immutable
data class SettingsState(
    val colorPresets: List<ColorPreset> = emptyList(),
    val selectedColorPreset: ColorPreset = Constants.provideDefaultColorPreset(),
    val animateColorPreset: Boolean = false,
    val colorPresetListState: LazyListState = LazyListState()
)