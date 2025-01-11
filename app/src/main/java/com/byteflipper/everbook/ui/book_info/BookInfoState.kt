package com.byteflipper.everbook.ui.book_info

import androidx.compose.runtime.Immutable
import com.byteflipper.everbook.domain.library.book.Book
import com.byteflipper.everbook.domain.util.BottomSheet
import com.byteflipper.everbook.domain.util.Dialog
import com.byteflipper.everbook.presentation.core.constants.Constants
import com.byteflipper.everbook.presentation.core.constants.provideEmptyBook

@Immutable
data class BookInfoState(
    val book: Book = Constants.provideEmptyBook(),

    val editTitle: Boolean = false,
    val hasTitleFocused: Boolean = false,
    val titleValue: String = "",

    val editAuthor: Boolean = false,
    val hasAuthorFocused: Boolean = false,
    val authorValue: String = "",

    val editDescription: Boolean = false,
    val hasDescriptionFocused: Boolean = false,
    val descriptionValue: String = "",

    val canResetCover: Boolean = false,

    val dialog: Dialog? = null,
    val bottomSheet: BottomSheet? = null
)