/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.domain.library.book

import androidx.compose.runtime.Immutable
import com.byteflipper.everbook.domain.ui.UIText

@Immutable
sealed class NullableBook(
    val bookWithCover: BookWithCover?,
    val fileName: String?,
    val message: UIText?
) {
    class NotNull(
        bookWithCover: BookWithCover
    ) : NullableBook(
        bookWithCover = bookWithCover,
        fileName = null,
        message = null
    )

    class Null(
        fileName: String,
        message: UIText?
    ) : NullableBook(
        bookWithCover = null,
        fileName = fileName,
        message = message
    )
}