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
import com.byteflipper.everbook.data.local.dto.BookCategoryCrossRef
import kotlinx.coroutines.flow.Flow

@Dao
interface BookCategoryDao {
    /** Добавить связи книга–категория */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(refs: List<BookCategoryCrossRef>)

    /** Удалить одну связь */
    @Query("DELETE FROM BookCategoryCrossRef WHERE bookId = :bookId AND categoryId = :categoryId")
    suspend fun delete(bookId: Int, categoryId: Int)

    /** Удалить все связи книги */
    @Query("DELETE FROM BookCategoryCrossRef WHERE bookId = :bookId")
    suspend fun deleteByBook(bookId: Int)

    /** Получить категории книги */
    @Query("SELECT categoryId FROM BookCategoryCrossRef WHERE bookId = :bookId")
    suspend fun getCategoriesForBook(bookId: Int): List<Int>

    /** Получить Flow категорий книги */
    @Query("SELECT categoryId FROM BookCategoryCrossRef WHERE bookId = :bookId")
    fun observeCategoriesForBook(bookId: Int): Flow<List<Int>>

    /** Получить книги, входящие в категорию */
    @Query("SELECT bookId FROM BookCategoryCrossRef WHERE categoryId = :categoryId")
    suspend fun getBooksForCategory(categoryId: Int): List<Int>

    /** Flow книг категории */
    @Query("SELECT bookId FROM BookCategoryCrossRef WHERE categoryId = :categoryId")
    fun observeBooksForCategory(categoryId: Int): Flow<List<Int>>
} 