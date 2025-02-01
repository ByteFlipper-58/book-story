package com.byteflipper.everbook.domain.reader

enum class ReaderColorEffects {
    OFF,
    GRAYSCALE,
    FONT,
    BACKGROUND
}

fun String.toColorEffects(): ReaderColorEffects {
    return ReaderColorEffects.valueOf(this)
}