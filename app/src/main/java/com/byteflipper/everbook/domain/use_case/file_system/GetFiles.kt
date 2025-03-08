/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.domain.use_case.file_system

import com.byteflipper.everbook.domain.browse.SelectableFile
import com.byteflipper.everbook.domain.repository.FileSystemRepository
import javax.inject.Inject

class GetFiles @Inject constructor(
    private val repository: FileSystemRepository
) {

    suspend fun execute(query: String): List<SelectableFile> {
        return repository.getFiles(query)
    }
}