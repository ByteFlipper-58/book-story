/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.domain.use_case.category

import com.byteflipper.everbook.domain.repository.CategoryRepository
import javax.inject.Inject

class CreateCategory @Inject constructor(
    private val repository: CategoryRepository
) {
    suspend operator fun invoke(name: String) {
        repository.createCategory(name)
    }
} 