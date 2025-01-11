package com.byteflipper.everbook.domain.history

import androidx.compose.runtime.Immutable
import com.byteflipper.everbook.domain.library.book.Book

@Immutable
data class History(
    val id: Int = 0,
    val bookId: Int,
    val book: Book?,
    val time: Long
)