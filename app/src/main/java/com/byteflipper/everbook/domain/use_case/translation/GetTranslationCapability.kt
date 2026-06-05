/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.domain.use_case.translation

import com.byteflipper.everbook.domain.repository.TranslationRepository
import com.byteflipper.everbook.domain.translation.TranslationCapability
import javax.inject.Inject

class GetTranslationCapability @Inject constructor(
    private val translationRepository: TranslationRepository
) {
    fun execute(): TranslationCapability = translationRepository.capability
}
