/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.ui.main

import android.app.Application
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.byteflipper.everbook.domain.library.book.NullableBook
import com.byteflipper.everbook.domain.library.book.SelectableNullableBook
import com.byteflipper.everbook.domain.use_case.file_system.ImportBookFromUri
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class ExternalImportModel @Inject constructor(
    private val application: Application,
    private val importBookFromUri: ImportBookFromUri
) : ViewModel() {

    private val _state = MutableStateFlow(ExternalImportState())
    val state = _state.asStateFlow()

    val openBookChannel = Channel<Int>(Channel.CONFLATED)
    val booksAddedChannel = Channel<Unit>(Channel.CONFLATED)
    val importFailedChannel = Channel<Unit>(Channel.CONFLATED)

    fun onEvent(event: ExternalImportEvent) {
        when (event) {
            is ExternalImportEvent.OnHandleUris -> {
                when {
                    event.uris.isEmpty() -> Unit
                    event.uris.size == 1 -> importSingle(event.uris.first())
                    else -> showAddDialog(event.uris)
                }
            }

            ExternalImportEvent.OnDismissAddDialog -> {
                deleteImportedCopies(_state.value.booksAddDialog)
                _state.value = ExternalImportState()
            }

            ExternalImportEvent.OnActionAddDialog -> addSelectedBooks()

            is ExternalImportEvent.OnSelectAddDialog -> {
                selectAddDialog(event.book)
            }
        }
    }

    private fun importSingle(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val bookId = importBookFromUri.execute(uri)

            if (bookId == null) {
                importFailedChannel.trySend(Unit)
                return@launch
            }

            openBookChannel.trySend(bookId)
        }
    }

    private fun showAddDialog(uris: List<Uri>) {
        viewModelScope.launch(Dispatchers.IO) {
            _state.value = ExternalImportState(
                showAddDialog = true,
                loadingAddDialog = true
            )
            _state.value = ExternalImportState(
                showAddDialog = true,
                booksAddDialog = uris.map { uri ->
                    SelectableNullableBook(
                        data = importBookFromUri.prepare(uri),
                        selected = true
                    )
                }
            )
        }
    }

    private fun selectAddDialog(book: SelectableNullableBook) {
        val books = _state.value.booksAddDialog
        val index = books.indexOf(book)
        if (index == -1) return

        val editedBooks = books.toMutableList()
        editedBooks[index] = book.copy(selected = !book.selected)

        if (!editedBooks.filter { it.data is NullableBook.NotNull }.any { it.selected }) {
            return
        }

        _state.value = _state.value.copy(booksAddDialog = editedBooks)
    }

    private fun addSelectedBooks() {
        viewModelScope.launch(Dispatchers.IO) {
            val books = _state.value.booksAddDialog
            val selectedBooks = books.filter {
                it.selected && it.data is NullableBook.NotNull
            }

            selectedBooks.forEach { book ->
                importBookFromUri.insert(book.data.bookWithCover!!)
            }

            deleteImportedCopies(
                books = books.filterNot { it in selectedBooks },
                keep = selectedBooks
            )
            _state.value = ExternalImportState()
            booksAddedChannel.trySend(Unit)
        }
    }

    private fun deleteImportedCopies(
        books: List<SelectableNullableBook>,
        keep: List<SelectableNullableBook> = emptyList()
    ) {
        val importsDir = File(application.filesDir, "imports").canonicalFile
        val keepPaths = keep.mapNotNull { it.importedFilePath() }.toSet()

        books.mapNotNull { it.importedFilePath() }
            .filterNot { it in keepPaths }
            .forEach { path ->
                val file = File(path).canonicalFile

                if (file.path.startsWith(importsDir.path)) {
                    file.delete()
                }
            }
    }

    private fun SelectableNullableBook.importedFilePath(): String? {
        return (data as? NullableBook.NotNull)?.bookWithCover?.book?.filePath
    }
}
