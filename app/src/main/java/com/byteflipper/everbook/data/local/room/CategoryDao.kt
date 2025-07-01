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
import androidx.room.Transaction
import androidx.room.Update
import com.byteflipper.everbook.data.local.dto.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    /* ------------------- Insert -------------------- */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: CategoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(categories: List<CategoryEntity>)

    /* ------------------- Update -------------------- */
    @Update
    suspend fun update(category: CategoryEntity)

    @Update
    suspend fun updateAll(categories: List<CategoryEntity>)

    /**
     * Обновляет позицию категории по её ID.
     */
    @Query("UPDATE CategoryEntity SET position = :position WHERE id = :id")
    suspend fun updatePosition(id: Int, position: Int)

    /**
     * Массовое обновление позиций в рамках одной транзакции.
     */
    @Transaction
    suspend fun updatePositions(updates: Map<Int, Int>) {
        updates.forEach { (id, position) ->
            updatePosition(id, position)
        }
    }

    /* ------------------- Delete -------------------- */
    @Delete
    suspend fun delete(category: CategoryEntity)

    @Query("DELETE FROM CategoryEntity WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Int>)

    /* ------------------- Queries ------------------- */
    @Query("SELECT * FROM CategoryEntity ORDER BY position ASC")
    fun observeCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM CategoryEntity WHERE id = :id")
    suspend fun findById(id: Int): CategoryEntity?

    @Query("SELECT COUNT(*) FROM CategoryEntity")
    suspend fun count(): Int

    @Query("SELECT MAX(position) FROM CategoryEntity")
    suspend fun getMaxPosition(): Int?
} 