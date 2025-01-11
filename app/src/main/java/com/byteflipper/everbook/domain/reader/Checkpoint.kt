package com.byteflipper.everbook.domain.reader

import androidx.compose.runtime.Immutable

@Immutable
data class Checkpoint(
    val index: Int,
    val offset: Int
)