package com.byteflipper.everbook.domain.use_case.file_system

import com.byteflipper.everbook.domain.library.book.NullableBook
import com.byteflipper.everbook.domain.repository.FileSystemRepository
import java.io.File
import javax.inject.Inject

class GetBookFromFile @Inject constructor(
    private val repository: FileSystemRepository
) {

    suspend fun execute(file: File): NullableBook {
        return repository.getBookFromFile(file)
    }
}