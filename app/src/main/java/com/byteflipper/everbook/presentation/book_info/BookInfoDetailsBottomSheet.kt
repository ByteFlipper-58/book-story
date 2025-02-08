package com.byteflipper.everbook.presentation.book_info

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.byteflipper.everbook.R
import com.byteflipper.everbook.domain.library.book.Book
import com.byteflipper.everbook.presentation.core.components.common.LazyColumnWithScrollbar
import com.byteflipper.everbook.presentation.core.components.modal_bottom_sheet.ModalBottomSheet
import com.byteflipper.everbook.presentation.settings.components.SettingsSubcategoryTitle
import com.byteflipper.everbook.ui.book_info.BookInfoEvent
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BookInfoDetailsBottomSheet(
    book: Book,
    dismissBottomSheet: (BookInfoEvent.OnDismissBottomSheet) -> Unit
) {
    val pattern = remember { SimpleDateFormat("HH:mm dd MMM yyyy", Locale.getDefault()) }
    val lastOpened = remember(book.lastOpened) { pattern.format(Date(book.lastOpened ?: 0)) }

    val fileSize = remember(book.filePath) {
        val file = File(book.filePath)
        if (file.exists()) {
            val sizeBytes = file.length()
            val sizeKB = sizeBytes / 1024f
            val sizeMB = sizeKB / 1024f
            when {
                sizeMB >= 1f -> "%.2f MB".format(sizeMB)
                sizeKB > 0f -> "%.2f KB".format(sizeKB)
                else -> ""
            }
        } else {
            ""
        }
    }

    val fileExists = remember(book.filePath) {
        File(book.filePath).exists()
    }

    ModalBottomSheet(
        modifier = Modifier.fillMaxWidth(),
        onDismissRequest = {
            dismissBottomSheet(BookInfoEvent.OnDismissBottomSheet)
        },
        sheetGesturesEnabled = true
    ) {
        LazyColumnWithScrollbar(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(bottom = 8.dp)
        ) {
            item {
                SettingsSubcategoryTitle(
                    title = stringResource(id = R.string.file_details),
                    padding = 16.dp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(8.dp))
            }

            item {
                BookInfoDetailsBottomSheetItem(
                    label = stringResource(id = R.string.file_path),
                    text = book.filePath,
                    editable = false,
                    showError = !fileExists,
                    errorMessage = stringResource(id = R.string.error_no_file)
                )
            }

            item {
                BookInfoDetailsBottomSheetItem(
                    label = stringResource(id = R.string.file_last_opened),
                    text = if (book.lastOpened != null) lastOpened
                    else stringResource(id = R.string.never),
                    editable = false
                )
            }

            item {
                BookInfoDetailsBottomSheetItem(
                    label = stringResource(id = R.string.file_size),
                    text = fileSize.ifBlank { stringResource(id = R.string.unknown) },
                    editable = false
                )
            }
        }
    }
}