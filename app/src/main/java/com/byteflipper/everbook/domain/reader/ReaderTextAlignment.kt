package com.byteflipper.everbook.domain.reader

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.style.TextAlign

@Immutable
enum class ReaderTextAlignment(val textAlignment: TextAlign) {
    START(TextAlign.Start),
    JUSTIFY(TextAlign.Justify),
    CENTER(TextAlign.Center),
    END(TextAlign.End)
}

fun String.toTextAlignment(): ReaderTextAlignment {
    return ReaderTextAlignment.valueOf(this)
}