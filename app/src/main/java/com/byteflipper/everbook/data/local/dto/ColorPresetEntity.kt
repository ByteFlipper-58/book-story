package com.byteflipper.everbook.data.local.dto

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ColorPresetEntity(
    @PrimaryKey(true)
    val id: Int? = null,
    val name: String?,
    val backgroundColor: Long,
    val fontColor: Long,
    val isSelected: Boolean,
    val order: Int
)