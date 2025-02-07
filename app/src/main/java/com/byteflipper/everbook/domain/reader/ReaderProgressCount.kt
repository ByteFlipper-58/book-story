package com.byteflipper.everbook.domain.reader

import androidx.compose.runtime.Immutable

@Immutable
enum class ReaderProgressCount {
    PERCENTAGE,
    QUANTITY
}

fun String.toProgressCount(): ReaderProgressCount {
    return ReaderProgressCount.valueOf(this)
}