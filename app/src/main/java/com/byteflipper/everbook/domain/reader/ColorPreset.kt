package com.byteflipper.everbook.domain.reader

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.byteflipper.everbook.domain.util.Selected

@Immutable
data class ColorPreset(
    val id: Int,
    val name: String?,
    val backgroundColor: Color,
    val fontColor: Color,
    val isSelected: Selected
)