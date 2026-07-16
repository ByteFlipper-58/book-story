/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.domain.repository

import com.byteflipper.everbook.domain.translation.TranslationCapability
import com.byteflipper.everbook.domain.translation.TranslationRequest
import com.byteflipper.everbook.domain.translation.TranslationResult
import kotlinx.coroutines.flow.StateFlow

interface TranslationRepository {
    val capability: TranslationCapability
    val isModelDownloadInProgress: StateFlow<Boolean>

    suspend fun translate(request: TranslationRequest): TranslationResult
}
