package com.byteflipper.everbook.data.mapper.color_preset

import com.byteflipper.everbook.data.local.dto.ColorPresetEntity
import com.byteflipper.everbook.domain.reader.ColorPreset

interface ColorPresetMapper {
    suspend fun toColorPresetEntity(colorPreset: ColorPreset, order: Int): ColorPresetEntity

    suspend fun toColorPreset(colorPresetEntity: ColorPresetEntity): ColorPreset
}