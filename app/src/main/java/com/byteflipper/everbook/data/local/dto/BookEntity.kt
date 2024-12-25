package com.byteflipper.everbook.data.local.dto

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.byteflipper.everbook.data.local.converter.ChapterConverter
import com.byteflipper.everbook.domain.model.Category
import com.byteflipper.everbook.domain.model.Chapter

@Entity
@TypeConverters(ChapterConverter::class)
data class BookEntity(
    @PrimaryKey(true) val id: Int = 0,
    val title: String,
    val author: String?,
    val description: String?,
    val textPath: String,
    @ColumnInfo(defaultValue = "[]")
    val chapters: List<Chapter>,
    val filePath: String,
    val scrollIndex: Int,
    val scrollOffset: Int,
    val progress: Float,
    val image: String? = null,
    val category: Category
)