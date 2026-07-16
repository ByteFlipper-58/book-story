/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.data.local.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.byteflipper.everbook.data.local.dto.BookEntity
import com.byteflipper.everbook.data.local.dto.BookLastOpenedTime
import com.byteflipper.everbook.data.local.dto.BookmarkEntity
import com.byteflipper.everbook.data.local.dto.ColorPresetEntity
import com.byteflipper.everbook.data.local.dto.HistoryEntity
import com.byteflipper.everbook.data.local.dto.ReadingSessionEntity
import kotlinx.coroutines.flow.Flow

/**
 * Class to manipulate Room database.
 */
@Dao
interface BookDao {

    /* ------ BookEntity ------------------------ */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(
        book: BookEntity
    ): Long

    @Query(
        """
        SELECT * FROM bookentity
        WHERE LOWER(title) LIKE '%' || LOWER(:query) || '%'
    """
    )
    suspend fun searchBooks(query: String): List<BookEntity>

    @Query("SELECT * FROM bookentity WHERE id=:id")
    suspend fun findBookById(id: Int): BookEntity

    @Query("SELECT * FROM bookentity WHERE id IN (:ids)")
    suspend fun findBooksById(ids: List<Int>): List<BookEntity>

    @Query("SELECT id FROM bookentity WHERE categoryId = :categoryId")
    suspend fun getBookIdsByCategory(categoryId: Int): List<Int>

    @Query("UPDATE bookentity SET categoryId = :categoryId WHERE id IN (:bookIds)")
    suspend fun updateCategoryForBooks(bookIds: List<Int>, categoryId: Int)

    @Delete
    suspend fun deleteBooks(books: List<BookEntity>)

    @Update
    suspend fun updateBooks(books: List<BookEntity>)
    /* - - - - - - - - - - - - - - - - - - - - - - */


    /* ------ HistoryEntity --------------------- */
    @Query("SELECT * FROM historyentity")
    suspend fun getHistory(): List<HistoryEntity>

    @Query("SELECT * FROM historyentity WHERE bookId = :bookId ORDER BY time DESC LIMIT 1")
    fun getLatestHistoryForBook(bookId: Int): HistoryEntity?

    /** Latest open timestamp per book, in one query (batches the per-book lookup above). */
    @Query(
        "SELECT bookId, MAX(time) AS time FROM historyentity " +
                "WHERE bookId IN (:bookIds) GROUP BY bookId"
    )
    suspend fun getLatestHistoryTimes(bookIds: List<Int>): List<BookLastOpenedTime>

    @Query(
        "SELECT bookentity.id AS bookId, MAX(historyentity.time) AS time " +
                "FROM bookentity INNER JOIN historyentity ON historyentity.bookId = bookentity.id " +
                "WHERE bookentity.categoryId = :categoryId GROUP BY bookentity.id"
    )
    suspend fun getLatestHistoryTimesByCategory(categoryId: Int): List<BookLastOpenedTime>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(
        history: List<HistoryEntity>
    )

    @Query("DELETE FROM historyentity")
    suspend fun deleteWholeHistory()

    @Query("DELETE FROM historyentity WHERE bookId = :bookId")
    suspend fun deleteBookHistory(bookId: Int)

    @Delete
    suspend fun deleteHistory(history: List<HistoryEntity>)
    /* - - - - - - - - - - - - - - - - - - - - - - */


    /* ------ ReadingSessionEntity -------------- */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: ReadingSessionEntity)

    @Query("SELECT * FROM readingsessionentity")
    suspend fun getAllSessions(): List<ReadingSessionEntity>

    @Query("DELETE FROM readingsessionentity WHERE bookId = :bookId")
    suspend fun deleteBookSessions(bookId: Int)

    @Query("DELETE FROM readingsessionentity")
    suspend fun deleteAllSessions()
    /* - - - - - - - - - - - - - - - - - - - - - - */


    /* ------ BookmarkEntity --------------------- */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity): Long

    @Query(
        "SELECT * FROM bookmarkentity WHERE bookId = :bookId " +
                "ORDER BY paragraphIndex ASC, charStart ASC"
    )
    fun observeBookmarks(bookId: Int): Flow<List<BookmarkEntity>>

    @Query("SELECT * FROM bookmarkentity WHERE id = :id")
    suspend fun getBookmarkById(id: Int): BookmarkEntity?

    @Query("DELETE FROM bookmarkentity WHERE id = :id")
    suspend fun deleteBookmark(id: Int)

    @Query("DELETE FROM bookmarkentity WHERE bookId = :bookId")
    suspend fun deleteBookmarksForBook(bookId: Int)
    /* - - - - - - - - - - - - - - - - - - - - - - */


    /* ------ ColorPresetEntity ----------------- */
    @Upsert
    suspend fun updateColorPreset(colorPreset: ColorPresetEntity)

    @Query("SELECT `order` FROM colorpresetentity WHERE :id=id")
    suspend fun getColorPresetOrder(id: Int): Int

    @Query("SELECT COUNT(*) FROM colorpresetentity")
    suspend fun getColorPresetsSize(): Int

    @Query("SELECT * FROM colorpresetentity")
    suspend fun getColorPresets(): List<ColorPresetEntity>

    @Delete
    suspend fun deleteColorPreset(colorPreset: ColorPresetEntity)

    @Query("DELETE FROM colorpresetentity")
    suspend fun deleteColorPresets()
    /* - - - - - - - - - - - - - - - - - - - - - - */
}
