/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.data.local.dto

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room-сущность для пользовательских и системных категорий библиотеки.
 *
 * 1. Системные (стандартные) категории имеют фиксированные id [1-4] и флаг [isDefault] = true.
 * 2. Пользовательские категории создаются с AUTOINCREMENT-id, их можно переименовывать, скрывать,
 *    менять порядок или удалять.
 */
@Entity
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val kind: String = "CUSTOM",
    val isVisible: Boolean = true,
    val position: Int = 0,
    val isDefault: Boolean = false,
) 