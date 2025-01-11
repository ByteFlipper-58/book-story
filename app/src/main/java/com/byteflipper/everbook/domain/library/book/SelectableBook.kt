package com.byteflipper.everbook.domain.library.book

import androidx.compose.runtime.Immutable
import com.byteflipper.everbook.domain.util.Selected

@Immutable
data class SelectableBook(
    val data: Book,
    val selected: Selected
)