package com.byteflipper.everbook.data.mapper.color_preset

import androidx.compose.ui.graphics.Color
import com.byteflipper.everbook.data.local.dto.ColorPresetEntity
import com.byteflipper.everbook.domain.reader.ColorPreset
import javax.inject.Inject

class ColorPresetMapperImpl @Inject constructor() : ColorPresetMapper {
    override suspend fun toColorPresetEntity(
        colorPreset: ColorPreset,
        order: Int
    ): ColorPresetEntity {
        return ColorPresetEntity(
            id = if (colorPreset.id != -1) colorPreset.id
            else null,
            name = colorPreset.name,
            backgroundColor = colorPreset.backgroundColor.value.toLong(),
            fontColor = colorPreset.fontColor.value.toLong(),
            isSelected = colorPreset.isSelected,
            order = order
        )
    }

    override suspend fun toColorPreset(colorPresetEntity: ColorPresetEntity): ColorPreset {
        return ColorPreset(
            id = colorPresetEntity.id!!,
            name = colorPresetEntity.name,
            backgroundColor = Color(colorPresetEntity.backgroundColor.toULong()),
            fontColor = Color(colorPresetEntity.fontColor.toULong()),
            isSelected = colorPresetEntity.isSelected
        )
    }
}