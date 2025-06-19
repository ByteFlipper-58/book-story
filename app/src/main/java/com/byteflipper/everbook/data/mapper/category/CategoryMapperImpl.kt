/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.data.mapper.category

import com.byteflipper.everbook.data.local.dto.CategoryEntity
import com.byteflipper.everbook.domain.library.custom_category.Category
import javax.inject.Inject

class CategoryMapperImpl @Inject constructor() : CategoryMapper {
    override fun toEntity(category: Category): CategoryEntity {
        return CategoryEntity(
            id = category.id,
            name = category.name,
            kind = category.kind,
            isVisible = category.isVisible,
            position = category.position,
            isDefault = category.isDefault
        )
    }

    override fun toDomain(entity: CategoryEntity): Category {
        val localizedName = if (entity.isDefault) {
            if (entity.id == 0) {
                com.byteflipper.everbook.domain.ui.UIText.StringResource(com.byteflipper.everbook.R.string.all_tab)
            } else
            when (entity.id) {
                1 -> com.byteflipper.everbook.domain.ui.UIText.StringResource(com.byteflipper.everbook.R.string.reading_tab)
                2 -> com.byteflipper.everbook.domain.ui.UIText.StringResource(com.byteflipper.everbook.R.string.already_read_tab)
                3 -> com.byteflipper.everbook.domain.ui.UIText.StringResource(com.byteflipper.everbook.R.string.planning_tab)
                4 -> com.byteflipper.everbook.domain.ui.UIText.StringResource(com.byteflipper.everbook.R.string.dropped_tab)
                else -> com.byteflipper.everbook.domain.ui.UIText.StringValue(entity.name)
            }
        } else com.byteflipper.everbook.domain.ui.UIText.StringValue(entity.name)
        return Category(
            id = entity.id,
            name = entity.name,
            kind = entity.kind,
            isVisible = entity.isVisible,
            position = entity.position,
            isDefault = entity.isDefault,
            title = localizedName
        )
    }
} 