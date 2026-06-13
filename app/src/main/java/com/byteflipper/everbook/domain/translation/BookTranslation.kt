/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.domain.translation

import androidx.compose.runtime.Immutable

const val BOOK_TRANSLATION_TEXT_CHANGED_MESSAGE = "Book text changed. Please translate it again."

@Immutable
data class BookTranslation(
    val id: Long = 0,
    val bookId: Int,
    val providerMode: TranslationProviderMode,
    val sourceLanguageCode: String?,
    val detectedSourceLanguageCode: String?,
    val targetLanguageCode: String,
    val requireWifi: Boolean,
    val status: BookTranslationStatus,
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
) {
    val progress: Float
        get() = if (totalUnits <= 0) 0f else completedUnits / totalUnits.toFloat()

    val canRead: Boolean
        get() = status == BookTranslationStatus.COMPLETED && completedUnits > 0

    val isBusy: Boolean
        get() = status == BookTranslationStatus.QUEUED ||
                status == BookTranslationStatus.PENDING ||
                status == BookTranslationStatus.RUNNING

    val canRetry: Boolean
        get() = status == BookTranslationStatus.FAILED ||
                status == BookTranslationStatus.CANCELLED

    val canResume: Boolean
        get() = status == BookTranslationStatus.PAUSED

    val isRateLimited: Boolean
        get() = providerMode == TranslationProviderMode.GOOGLE_TRANSLATE &&
                status == BookTranslationStatus.FAILED &&
                errorMessage?.isRateLimitMessage() == true
}

private fun String.isRateLimitMessage(): Boolean {
    val normalized = lowercase()
    return "rate limit" in normalized ||
            "too many requests" in normalized ||
            "blocked this request" in normalized ||
            "429" in normalized
}

@Immutable
data class BookTranslationEntry(
    val translationId: Long,
    val readerTextIndex: Int,
    val type: BookTranslationEntryType,
    val originalText: String,
    val translatedText: String,
    val sourceLanguageCode: String?,
    val targetLanguageCode: String,
    val updatedAt: Long
)

enum class BookTranslationStatus {
    QUEUED,
    PENDING,
    RUNNING,
    PAUSED,
    COMPLETED,
    FAILED,
    CANCELLED,
    STALE
}

fun String.toBookTranslationStatus(): BookTranslationStatus =
    BookTranslationStatus.entries.find { it.name == this } ?: BookTranslationStatus.FAILED

enum class BookTranslationEntryType {
    CHAPTER,
    TEXT
}

fun String.toBookTranslationEntryType(): BookTranslationEntryType =
    BookTranslationEntryType.entries.find { it.name == this } ?: BookTranslationEntryType.TEXT
