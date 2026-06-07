/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.data.translation

import com.byteflipper.everbook.domain.repository.TranslationRepository
import com.byteflipper.everbook.domain.translation.TranslationCapability
import com.byteflipper.everbook.domain.translation.TranslationException
import com.byteflipper.everbook.domain.translation.TranslationProviderMode
import com.byteflipper.everbook.domain.translation.TranslationRequest
import com.byteflipper.everbook.domain.translation.TranslationResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EverbookTranslationRepository @Inject constructor(
    private val googleTranslateWebClient: GoogleTranslateWebClient
) : TranslationRepository {
    override val capability = TranslationCapability(
        inAppAvailable = false,
        googleTranslateAvailable = true
    )

    override suspend fun translate(request: TranslationRequest): TranslationResult =
        when (request.providerMode) {
            TranslationProviderMode.GOOGLE_TRANSLATE -> googleTranslateWebClient.translate(request)
            TranslationProviderMode.IN_APP ->
                throw TranslationException("In-app translation is unavailable in this build.")

            TranslationProviderMode.EXTERNAL ->
                throw TranslationException("External translation uses installed apps.")
        }
}
