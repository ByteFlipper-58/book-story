package com.byteflipper.everbook.domain.use_case.file_system

import com.byteflipper.everbook.domain.browse.SelectableFile
import com.byteflipper.everbook.domain.repository.FileSystemRepository
import javax.inject.Inject

class GetFilesFromDevice @Inject constructor(
    private val repository: FileSystemRepository
) {

    suspend fun execute(query: String): List<SelectableFile> {
        return repository.getFilesFromDevice(query)
    }
}