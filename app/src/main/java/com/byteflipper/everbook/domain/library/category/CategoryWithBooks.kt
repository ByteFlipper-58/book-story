package com.byteflipper.everbook.domain.library.category

import com.byteflipper.everbook.domain.library.book.SelectableBook
import com.byteflipper.everbook.domain.ui.UIText

data class CategoryWithBooks(
    val category: Category,
    val title: UIText,
    val books: List<SelectableBook>
)