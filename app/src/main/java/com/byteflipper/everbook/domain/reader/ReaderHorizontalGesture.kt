package com.byteflipper.everbook.domain.reader

enum class ReaderHorizontalGesture {
    OFF,
    ON,
    INVERSE
}

fun String.toHorizontalGesture(): ReaderHorizontalGesture {
    return ReaderHorizontalGesture.valueOf(this)
}