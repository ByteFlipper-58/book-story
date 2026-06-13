/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.data.local.dto

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    indices = [
        Index(value = ["bookId"]),
        Index(
            value = ["bookId", "providerMode", "sourceLanguageCode", "targetLanguageCode"]
        ),
        Index(
            value = [
                "bookId",
                "providerMode",
                "sourceLanguageCode",
                "targetLanguageCode",
                "sourceFingerprint"
            ]
        )
    ]
)
data class BookTranslationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bookId: Int,
    val providerMode: String,
    val sourceLanguageCode: String?,
    val detectedSourceLanguageCode: String?,
    val targetLanguageCode: String,
    val requireWifi: Boolean,
    val status: String,
    val sourceFingerprint: String,
    val totalUnits: Int,
    val completedUnits: Int,
    val failedUnits: Int,
    val errorMessage: String?,
    val createdAt: Long,
    val updatedAt: Long,
    val queuedAt: Long?,
    val startedAt: Long?,
    val lastAttemptAt: Long?,
    val retryCount: Int,
    val completedAt: Long?
)
