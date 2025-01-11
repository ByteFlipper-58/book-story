package com.byteflipper.everbook.domain.repository

import com.byteflipper.everbook.domain.browse.SelectableFile
import com.byteflipper.everbook.domain.library.book.NullableBook
import java.io.File

interface FileSystemRepository {

    suspend fun getFilesFromDevice(
        query: String = ""
    ): List<SelectableFile>

    suspend fun getBookFromFile(
        file: File
    ): NullableBook
}