/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.data.repository

import com.byteflipper.everbook.data.local.dto.BookTranslationEntity
import com.byteflipper.everbook.data.local.dto.BookTranslationEntryEntity
import com.byteflipper.everbook.data.local.room.BookTranslationDao
import com.byteflipper.everbook.domain.repository.BookTranslationRepository
import com.byteflipper.everbook.domain.translation.BookTranslation
import com.byteflipper.everbook.domain.translation.BookTranslationEntry
import com.byteflipper.everbook.domain.translation.BookTranslationStatus
import com.byteflipper.everbook.domain.translation.TranslationProviderMode
import com.byteflipper.everbook.domain.translation.toBookTranslationEntryType
import com.byteflipper.everbook.domain.translation.toBookTranslationStatus
import com.byteflipper.everbook.domain.translation.toTranslationProviderMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookTranslationRepositoryImpl @Inject constructor(
    private val dao: BookTranslationDao
) : BookTranslationRepository {
    override suspend fun getTranslations(bookId: Int): List<BookTranslation> =
        withContext(Dispatchers.IO) {
            dao.getTranslations(bookId).map { it.toDomain() }
        }

    override fun observeTranslations(bookId: Int): Flow<List<BookTranslation>> =
        dao.observeTranslations(bookId).map { translations ->
            translations.map { it.toDomain() }
        }

    override suspend fun getAllTranslations(): List<BookTranslation> =
        withContext(Dispatchers.IO) {
            dao.getAllTranslations().map { it.toDomain() }
        }

    override fun observeAllTranslations(): Flow<List<BookTranslation>> =
        dao.observeAllTranslations().map { translations ->
            translations.map { it.toDomain() }
        }

    override suspend fun getTranslation(id: Long): BookTranslation? =
        withContext(Dispatchers.IO) {
            dao.getTranslation(id)?.toDomain()
        }

    override suspend fun findTranslation(
        bookId: Int,
        providerMode: TranslationProviderMode,
        sourceLanguageCode: String?,
        targetLanguageCode: String,
        sourceFingerprint: String
    ): BookTranslation? =
        withContext(Dispatchers.IO) {
            dao.findTranslation(
                bookId = bookId,
                providerMode = providerMode.name,
                sourceLanguageCode = sourceLanguageCode,
                targetLanguageCode = targetLanguageCode,
                sourceFingerprint = sourceFingerprint
            )?.toDomain()
        }

    override suspend fun getEntries(translationId: Long): List<BookTranslationEntry> =
        withContext(Dispatchers.IO) {
            dao.getEntries(translationId).map { it.toDomain() }
        }

    override fun observeEntries(translationId: Long): Flow<List<BookTranslationEntry>> =
        dao.observeEntries(translationId).map { entries ->
            entries.map { it.toDomain() }
        }

    override suspend fun countEntries(translationId: Long): Int =
        withContext(Dispatchers.IO) {
            dao.countEntries(translationId)
        }

    override suspend fun createTranslation(
        bookId: Int,
        providerMode: TranslationProviderMode,
        sourceLanguageCode: String?,
        targetLanguageCode: String,
        requireWifi: Boolean,
        sourceFingerprint: String,
        totalUnits: Int
    ): BookTranslation = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val entity = BookTranslationEntity(
            bookId = bookId,
            providerMode = providerMode.name,
            sourceLanguageCode = sourceLanguageCode,
            detectedSourceLanguageCode = null,
            targetLanguageCode = targetLanguageCode,
            requireWifi = requireWifi,
            status = BookTranslationStatus.QUEUED.name,
            sourceFingerprint = sourceFingerprint,
            totalUnits = totalUnits,
            completedUnits = 0,
            failedUnits = 0,
            errorMessage = null,
            createdAt = now,
            updatedAt = now,
            queuedAt = now,
            startedAt = null,
            lastAttemptAt = null,
            retryCount = 0,
            completedAt = null
        )
        val id = dao.insertTranslation(entity)
        dao.getTranslation(id)?.toDomain()
            ?: entity.copy(id = id).toDomain()
    }

    override suspend fun updateTranslation(translation: BookTranslation) {
        withContext(Dispatchers.IO) {
            dao.updateTranslation(translation.toEntity())
        }
    }

    override suspend fun updateTranslationStatus(
        translationId: Long,
        status: BookTranslationStatus,
        errorMessage: String?,
        completedAt: Long?
    ) {
        withContext(Dispatchers.IO) {
            val entity = dao.getTranslation(translationId) ?: return@withContext
            dao.updateTranslation(
                entity.copy(
                    status = status.name,
                    errorMessage = errorMessage,
                    updatedAt = System.currentTimeMillis(),
                    queuedAt = if (status == BookTranslationStatus.QUEUED) {
                        System.currentTimeMillis()
                    } else entity.queuedAt,
                    startedAt = if (status == BookTranslationStatus.RUNNING) {
                        entity.startedAt ?: System.currentTimeMillis()
                    } else entity.startedAt,
                    lastAttemptAt = if (
                        status == BookTranslationStatus.RUNNING ||
                        status == BookTranslationStatus.FAILED
                    ) {
                        System.currentTimeMillis()
                    } else entity.lastAttemptAt,
                    completedAt = completedAt ?: entity.completedAt
                )
            )
        }
    }

    override suspend fun upsertEntries(entries: List<BookTranslationEntry>) {
        if (entries.isEmpty()) return

        withContext(Dispatchers.IO) {
            dao.upsertEntries(entries.map { it.toEntity() })
        }
    }

    override suspend fun deleteTranslation(id: Long) {
        withContext(Dispatchers.IO) {
            dao.deleteEntries(id)
            dao.deleteTranslation(id)
        }
    }

    override suspend fun deleteTranslationsForBook(bookId: Int) {
        withContext(Dispatchers.IO) {
            dao.deleteEntriesForBook(bookId)
            dao.deleteTranslationsForBook(bookId)
        }
    }

    private fun BookTranslationEntity.toDomain(): BookTranslation =
        BookTranslation(
            id = id,
            bookId = bookId,
            providerMode = providerMode.toTranslationProviderMode(),
            sourceLanguageCode = sourceLanguageCode,
            detectedSourceLanguageCode = detectedSourceLanguageCode,
            targetLanguageCode = targetLanguageCode,
            requireWifi = requireWifi,
            status = status.toBookTranslationStatus(),
            sourceFingerprint = sourceFingerprint,
            totalUnits = totalUnits,
            completedUnits = completedUnits,
            failedUnits = failedUnits,
            errorMessage = errorMessage,
            createdAt = createdAt,
            updatedAt = updatedAt,
            queuedAt = queuedAt,
            startedAt = startedAt,
            lastAttemptAt = lastAttemptAt,
            retryCount = retryCount,
            completedAt = completedAt
        )

    private fun BookTranslation.toEntity(): BookTranslationEntity =
        BookTranslationEntity(
            id = id,
            bookId = bookId,
            providerMode = providerMode.name,
            sourceLanguageCode = sourceLanguageCode,
            detectedSourceLanguageCode = detectedSourceLanguageCode,
            targetLanguageCode = targetLanguageCode,
            requireWifi = requireWifi,
            status = status.name,
            sourceFingerprint = sourceFingerprint,
            totalUnits = totalUnits,
            completedUnits = completedUnits,
            failedUnits = failedUnits,
            errorMessage = errorMessage,
            createdAt = createdAt,
            updatedAt = updatedAt,
            queuedAt = queuedAt,
            startedAt = startedAt,
            lastAttemptAt = lastAttemptAt,
            retryCount = retryCount,
            completedAt = completedAt
        )

    private fun BookTranslationEntryEntity.toDomain(): BookTranslationEntry =
        BookTranslationEntry(
            translationId = translationId,
            readerTextIndex = readerTextIndex,
            type = type.toBookTranslationEntryType(),
            originalText = originalText,
            translatedText = translatedText,
            sourceLanguageCode = sourceLanguageCode,
            targetLanguageCode = targetLanguageCode,
            updatedAt = updatedAt
        )

    private fun BookTranslationEntry.toEntity(): BookTranslationEntryEntity =
        BookTranslationEntryEntity(
            translationId = translationId,
            readerTextIndex = readerTextIndex,
            type = type.name,
            originalText = originalText,
            translatedText = translatedText,
            sourceLanguageCode = sourceLanguageCode,
            targetLanguageCode = targetLanguageCode,
            updatedAt = updatedAt
        )
}
