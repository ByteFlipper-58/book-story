package com.byteflipper.book_story.domain.use_case.file_system

import com.byteflipper.book_story.domain.model.NullableBook
import com.byteflipper.book_story.domain.repository.FileSystemRepository
import java.io.File
import javax.inject.Inject

class GetBookFromFile @Inject constructor(
    private val repository: FileSystemRepository
) {

    suspend fun execute(file: File): NullableBook {
        return repository.getBookFromFile(file)
    }
}