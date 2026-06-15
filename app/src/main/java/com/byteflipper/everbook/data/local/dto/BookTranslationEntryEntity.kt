/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.data.local.dto

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    primaryKeys = ["translationId", "readerTextIndex"],
    foreignKeys = [
        ForeignKey(
            entity = BookTranslationEntity::class,
            parentColumns = ["id"],
            childColumns = ["translationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["translationId"]),
        Index(value = ["translationId", "readerTextIndex"])
    ]
)
data class BookTranslationEntryEntity(
    val translationId: Long,
    val readerTextIndex: Int,
    val type: String,
    val originalText: String,
    val translatedText: String,
    val sourceLanguageCode: String?,
    val targetLanguageCode: String,
    val updatedAt: Long
)
