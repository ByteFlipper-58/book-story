/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.domain.repository

import com.byteflipper.everbook.domain.translation.BookTranslation
import com.byteflipper.everbook.domain.translation.BookTranslationEntry
import com.byteflipper.everbook.domain.translation.BookTranslationStatus
import com.byteflipper.everbook.domain.translation.TranslationProviderMode
import kotlinx.coroutines.flow.Flow

interface BookTranslationRepository {
    suspend fun getTranslations(bookId: Int): List<BookTranslation>

    fun observeTranslations(bookId: Int): Flow<List<BookTranslation>>

    suspend fun getAllTranslations(): List<BookTranslation>

    fun observeAllTranslations(): Flow<List<BookTranslation>>

    suspend fun getTranslation(id: Long): BookTranslation?

    suspend fun findTranslation(
        bookId: Int,
        providerMode: TranslationProviderMode,
        sourceLanguageCode: String?,
        targetLanguageCode: String,
        sourceFingerprint: String
    ): BookTranslation?

    suspend fun getEntries(translationId: Long): List<BookTranslationEntry>

    fun observeEntries(translationId: Long): Flow<List<BookTranslationEntry>>

    suspend fun countEntries(translationId: Long): Int

    suspend fun createTranslation(
        bookId: Int,
        providerMode: TranslationProviderMode,
        sourceLanguageCode: String?,
        targetLanguageCode: String,
        requireWifi: Boolean,
        sourceFingerprint: String,
        totalUnits: Int
    ): BookTranslation

    suspend fun updateTranslation(translation: BookTranslation)

    /**
     * Persists only progress counters, leaving [BookTranslation.status] untouched so a
     * concurrent pause/cancel can never be overwritten by a stale write.
     */
    suspend fun updateProgress(
        translationId: Long,
        completedUnits: Int,
        failedUnits: Int,
        detectedSourceLanguageCode: String?
    )

    suspend fun updateTranslationStatus(
        translationId: Long,
        status: BookTranslationStatus,
        errorMessage: String? = null,
        completedAt: Long? = null
    )

    suspend fun upsertEntries(entries: List<BookTranslationEntry>)

    suspend fun deleteTranslation(id: Long)

    suspend fun deleteTranslationsForBook(bookId: Int)
}
