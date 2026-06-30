/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.domain.use_case.translation

import com.byteflipper.everbook.domain.repository.TranslationModelRepository
import com.byteflipper.everbook.domain.translation.TranslationModelState
import javax.inject.Inject

class GetAvailableTranslationModels @Inject constructor(
    private val repository: TranslationModelRepository
) {
    fun execute(): List<TranslationModelState> =
        repository.getAvailableModels()
}
