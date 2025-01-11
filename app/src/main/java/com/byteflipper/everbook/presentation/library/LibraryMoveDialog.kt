package com.byteflipper.everbook.presentation.library

import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.DriveFileMove
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.byteflipper.everbook.R
import com.byteflipper.everbook.domain.library.book.SelectableBook
import com.byteflipper.everbook.domain.library.category.CategoryWithBooks
import com.byteflipper.everbook.presentation.core.components.dialog.Dialog
import com.byteflipper.everbook.presentation.core.components.dialog.SelectableDialogItem
import com.byteflipper.everbook.ui.library.LibraryEvent

@Composable
fun LibraryMoveDialog(
    books: List<SelectableBook>,
    categories: List<CategoryWithBooks>,
    selectedItemsCount: Int,
    actionMoveDialog: (LibraryEvent.OnActionMoveDialog) -> Unit,
    dismissDialog: (LibraryEvent.OnDismissDialog) -> Unit
) {
    val context = LocalContext.current
    val selectedBooks = remember {
        derivedStateOf {
            books.filter { it.selected }
        }
    }
    val moveCategories = remember {
        derivedStateOf {
            categories.mapNotNull { category ->
                if (!selectedBooks.value.all { it.data.category == category.category }) {
                    return@mapNotNull category
                }
                return@mapNotNull null
            }
        }
    }
    var selectedCategory = remember {
        mutableStateOf(moveCategories.value[0])
    }

    Dialog(
        title = stringResource(id = R.string.move_books),
        imageVectorIcon = Icons.AutoMirrored.Outlined.DriveFileMove,
        description = stringResource(
            id = R.string.move_books_description,
            selectedItemsCount
        ),
        actionText = stringResource(id = R.string.move),
        actionEnabled = true,
        onDismiss = {
            dismissDialog(LibraryEvent.OnDismissDialog)
        },
        onAction = {
            actionMoveDialog(
                LibraryEvent.OnActionMoveDialog(
                    selectedCategory = selectedCategory.value.category,
                    categories = categories,
                    context = context
                )
            )
        },
        withDivider = false,
        items = {
            items(moveCategories.value) { category ->
                SelectableDialogItem(
                    selected = category == selectedCategory.value,
                    title = category.title.asString()
                ) {
                    selectedCategory.value = category
                }
            }
        }
    )
}