package com.byteflipper.everbook.presentation.screens.library.components.dialog

import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.DriveFileMove
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.byteflipper.everbook.R
import com.byteflipper.everbook.domain.model.Category
import com.byteflipper.everbook.presentation.core.components.dialog.DialogWithLazyColumn
import com.byteflipper.everbook.presentation.core.components.dialog.SelectableDialogItem
import com.byteflipper.everbook.presentation.core.util.showToast
import com.byteflipper.everbook.presentation.screens.history.data.HistoryEvent
import com.byteflipper.everbook.presentation.screens.history.data.HistoryViewModel
import com.byteflipper.everbook.presentation.screens.library.data.LibraryEvent
import com.byteflipper.everbook.presentation.screens.library.data.LibraryViewModel

/**
 * Move dialog. Moves all selected books to the selected category.
 */
@Composable
fun LibraryMoveDialog(pagerState: PagerState) {
    val context = LocalContext.current
    val state = LibraryViewModel.getState()
    val onEvent = LibraryViewModel.getEvent()
    val onHistoryEvent = HistoryViewModel.getEvent()

    DialogWithLazyColumn(
        title = stringResource(id = R.string.move_books),
        imageVectorIcon = Icons.AutoMirrored.Outlined.DriveFileMove,
        description = stringResource(
            id = R.string.move_books_description,
            state.value.books.filter { it.second }.size
        ),
        actionText = stringResource(id = R.string.move),
        isActionEnabled = true,
        onDismiss = { onEvent(LibraryEvent.OnShowHideMoveDialog) },
        onAction = {
            onEvent(
                LibraryEvent.OnMoveBooks(
                    pagerState,
                    refreshList = {
                        onHistoryEvent(HistoryEvent.OnLoadList)
                    }
                )
            )
            context.getString(R.string.books_moved)
                .showToast(context = context)
        },
        withDivider = false,
        items = {
            items(state.value.categories, key = { it.name }) {
                val category = when (it) {
                    Category.READING -> stringResource(id = R.string.reading_tab)
                    Category.ALREADY_READ -> stringResource(id = R.string.already_read_tab)
                    Category.PLANNING -> stringResource(id = R.string.planning_tab)
                    Category.DROPPED -> stringResource(id = R.string.dropped_tab)
                }

                SelectableDialogItem(
                    selected = it == state.value.selectedCategory,
                    title = category
                ) {
                    onEvent(LibraryEvent.OnSelectCategory(it))
                }
            }
        }
    )
}