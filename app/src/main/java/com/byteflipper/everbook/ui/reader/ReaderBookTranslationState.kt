/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.ui.reader

import androidx.compose.runtime.Immutable
import com.byteflipper.everbook.domain.translation.AUTO_TRANSLATION_LANGUAGE
import com.byteflipper.everbook.domain.translation.BookTranslation
import com.byteflipper.everbook.domain.translation.BookTranslationStatus
import com.byteflipper.everbook.domain.translation.DEFAULT_TRANSLATION_TARGET_LANGUAGE
import com.byteflipper.everbook.domain.translation.TranslationFeature
import com.byteflipper.everbook.domain.translation.TranslationProviderMode

@Immutable
data class ReaderBookTranslationState(
    val currentTranslation: BookTranslation? = null,
    val providerMode: TranslationProviderMode = TranslationFeature.DEFAULT_FULL_BOOK_PROVIDER_MODE,
    val sourceLanguageCode: String = AUTO_TRANSLATION_LANGUAGE,
    val targetLanguageCode: String = DEFAULT_TRANSLATION_TARGET_LANGUAGE,
    val requireWifi: Boolean = true,
    val isStarting: Boolean = false,
    val isApplyingTranslation: Boolean = false,
    val displayMode: ReaderBookTranslationDisplayMode = ReaderBookTranslationDisplayMode.ORIGINAL,
    val activeTranslationId: Long? = null,
    val showGoogleWarning: Boolean = false,
    val recentSourceLanguageCodes: List<String> = emptyList(),
    val recentTargetLanguageCodes: List<String> = emptyList(),
    val isBookTextReadyForTranslation: Boolean = false,
    val errorMessage: String? = null
) {
    val runningTranslation: BookTranslation?
        get() = currentTranslation?.takeIf { it.isBusy }

    val pausedTranslation: BookTranslation?
        get() = currentTranslation?.takeIf { it.canResume }

    val completedTranslation: BookTranslation?
        get() = currentTranslation?.takeIf { it.canRead }

    val rateLimitedTranslation: BookTranslation?
        get() = currentTranslation?.takeIf { it.isRateLimited }

    val readableTranslation: BookTranslation?
        get() = completedTranslation ?: rateLimitedTranslation?.takeIf { it.completedUnits > 0 }

    val retryTranslation: BookTranslation?
        get() = currentTranslation?.takeIf {
            (it.status == BookTranslationStatus.FAILED && !it.isRateLimited) ||
                    (it.status == BookTranslationStatus.STALE && isBookTextReadyForTranslation)
        }

    val isTranslatedBookVisible: Boolean
        get() = displayMode == ReaderBookTranslationDisplayMode.TRANSLATED &&
                activeTranslationId != null
}

enum class ReaderBookTranslationDisplayMode {
    ORIGINAL,
    TRANSLATED
}
