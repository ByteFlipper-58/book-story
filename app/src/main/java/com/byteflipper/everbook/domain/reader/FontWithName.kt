package com.byteflipper.everbook.domain.reader

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.font.FontFamily
import com.byteflipper.everbook.domain.ui.UIText

@Immutable
data class FontWithName(
    val id: String,
    val fontName: UIText,
    val font: FontFamily
)