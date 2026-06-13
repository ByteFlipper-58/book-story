/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.data.local.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.byteflipper.everbook.data.local.dto.BookTranslationEntity
import com.byteflipper.everbook.data.local.dto.BookTranslationEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookTranslationDao {
    @Query("SELECT * FROM BookTranslationEntity WHERE bookId = :bookId ORDER BY updatedAt DESC")
    suspend fun getTranslations(bookId: Int): List<BookTranslationEntity>

    @Query("SELECT * FROM BookTranslationEntity WHERE bookId = :bookId ORDER BY updatedAt DESC")
    fun observeTranslations(bookId: Int): Flow<List<BookTranslationEntity>>

    @Query("SELECT * FROM BookTranslationEntity ORDER BY updatedAt DESC")
    suspend fun getAllTranslations(): List<BookTranslationEntity>

    @Query("SELECT * FROM BookTranslationEntity ORDER BY updatedAt DESC")
    fun observeAllTranslations(): Flow<List<BookTranslationEntity>>

    @Query("SELECT * FROM BookTranslationEntity WHERE id = :id")
    suspend fun getTranslation(id: Long): BookTranslationEntity?

    @Query(
        """
        SELECT * FROM BookTranslationEntity
        WHERE bookId = :bookId
            AND providerMode = :providerMode
            AND (
                (:sourceLanguageCode IS NULL AND sourceLanguageCode IS NULL)
                OR sourceLanguageCode = :sourceLanguageCode
            )
            AND targetLanguageCode = :targetLanguageCode
            AND sourceFingerprint = :sourceFingerprint
        ORDER BY
            CASE status
                WHEN 'COMPLETED' THEN 0
                WHEN 'RUNNING' THEN 1
                WHEN 'QUEUED' THEN 2
                WHEN 'PENDING' THEN 3
                WHEN 'FAILED' THEN 4
                WHEN 'CANCELLED' THEN 5
                ELSE 6
            END,
            updatedAt DESC
        LIMIT 1
        """
    )
    suspend fun findTranslation(
        bookId: Int,
        providerMode: String,
        sourceLanguageCode: String?,
        targetLanguageCode: String,
        sourceFingerprint: String
    ): BookTranslationEntity?

    @Query(
        """
        SELECT * FROM BookTranslationEntryEntity
        WHERE translationId = :translationId
        ORDER BY readerTextIndex ASC
        """
    )
    suspend fun getEntries(translationId: Long): List<BookTranslationEntryEntity>

    @Query(
        """
        SELECT * FROM BookTranslationEntryEntity
        WHERE translationId = :translationId
        ORDER BY readerTextIndex ASC
        """
    )
    fun observeEntries(translationId: Long): Flow<List<BookTranslationEntryEntity>>

    @Query("SELECT COUNT(*) FROM BookTranslationEntryEntity WHERE translationId = :translationId")
    suspend fun countEntries(translationId: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTranslation(entity: BookTranslationEntity): Long

    @Update
    suspend fun updateTranslation(entity: BookTranslationEntity)

    @Upsert
    suspend fun upsertEntries(entries: List<BookTranslationEntryEntity>)

    @Query("DELETE FROM BookTranslationEntryEntity WHERE translationId = :translationId")
    suspend fun deleteEntries(translationId: Long)

    @Query("DELETE FROM BookTranslationEntity WHERE id = :id")
    suspend fun deleteTranslation(id: Long)

    @Query(
        """
        DELETE FROM BookTranslationEntryEntity
        WHERE translationId IN (
            SELECT id FROM BookTranslationEntity WHERE bookId = :bookId
        )
        """
    )
    suspend fun deleteEntriesForBook(bookId: Int)

    @Query("DELETE FROM BookTranslationEntity WHERE bookId = :bookId")
    suspend fun deleteTranslationsForBook(bookId: Int)
}
