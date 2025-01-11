package com.byteflipper.everbook.domain.use_case.color_preset

import com.byteflipper.everbook.domain.reader.ColorPreset
import com.byteflipper.everbook.domain.repository.ColorPresetRepository
import javax.inject.Inject

class DeleteColorPreset @Inject constructor(
    private val repository: ColorPresetRepository
) {

    suspend fun execute(colorPreset: ColorPreset) {
        repository.deleteColorPreset(colorPreset)
    }
}