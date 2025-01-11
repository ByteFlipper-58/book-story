package com.byteflipper.everbook.domain.library.book

import androidx.compose.runtime.Immutable
import com.byteflipper.everbook.domain.util.CoverImage

@Immutable
data class BookWithCover(
    val book: Book,
    val coverImage: CoverImage?
)