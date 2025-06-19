/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.data.mapper.category

import com.byteflipper.everbook.data.local.dto.CategoryEntity
import com.byteflipper.everbook.domain.library.custom_category.Category

interface CategoryMapper {
    fun toEntity(category: Category): CategoryEntity

    fun toDomain(entity: CategoryEntity): Category
} 