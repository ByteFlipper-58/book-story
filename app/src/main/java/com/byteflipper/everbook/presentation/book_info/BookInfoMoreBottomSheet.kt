package com.byteflipper.everbook.presentation.book_info

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.byteflipper.everbook.R
import com.byteflipper.everbook.domain.util.Position
import com.byteflipper.everbook.presentation.core.components.modal_bottom_sheet.ModalBottomSheet
import com.byteflipper.everbook.presentation.navigator.NavigatorBottomSheetItem
import com.byteflipper.everbook.ui.book_info.BookInfoEvent

@Composable
fun BookInfoMoreBottomSheet(
    showDetailsBottomSheet: (BookInfoEvent.OnShowDetailsBottomSheet) -> Unit,
    showMoveDialog: (BookInfoEvent.OnShowMoveDialog) -> Unit,
    showDeleteDialog: (BookInfoEvent.OnShowDeleteDialog) -> Unit,
    dismissBottomSheet: (BookInfoEvent.OnDismissBottomSheet) -> Unit,
) {
    ModalBottomSheet(
        modifier = Modifier.fillMaxWidth(),
        onDismissRequest = {
            dismissBottomSheet(BookInfoEvent.OnDismissBottomSheet)
        },
        sheetGesturesEnabled = true
    ) {
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            item {
                NavigatorBottomSheetItem(
                    title = stringResource(id = R.string.details_book),
                    primary = false,
                    position = Position.SOLO
                ) {
                    showDetailsBottomSheet(BookInfoEvent.OnShowDetailsBottomSheet)
                }
            }

            item {
                Spacer(Modifier.height(18.dp))
            }

            item {
                NavigatorBottomSheetItem(
                    title = stringResource(id = R.string.delete_this_book),
                    primary = true,
                    position = Position.TOP
                ) {
                    showDeleteDialog(BookInfoEvent.OnShowDeleteDialog)
                }
            }

            item {
                NavigatorBottomSheetItem(
                    title = stringResource(id = R.string.move_this_book),
                    primary = true,
                    position = Position.BOTTOM
                ) {
                    showMoveDialog(BookInfoEvent.OnShowMoveDialog)
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}