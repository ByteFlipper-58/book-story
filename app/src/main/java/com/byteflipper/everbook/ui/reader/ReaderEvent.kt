/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.ui.reader

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Immutable
import com.byteflipper.everbook.domain.reader.PdfReadingMode
import com.byteflipper.everbook.domain.reader.ReaderText.Chapter

@Immutable
sealed class ReaderEvent {

    data class OnLoadText(
        val activity: ComponentActivity,
        val fullscreenMode: Boolean
    ) : ReaderEvent()

    data class OnChangePdfReadingMode(
        val mode: PdfReadingMode
    ) : ReaderEvent()

    data object OnShowPdfReadingModeBottomSheet : ReaderEvent()

    data object OnShowBookTranslationBottomSheet : ReaderEvent()

    data class OnMenuVisibility(
        val show: Boolean,
        val fullscreenMode: Boolean,
        val saveCheckpoint: Boolean,
        val activity: ComponentActivity
    ) : ReaderEvent()

    data class OnChangeProgress(
        val progress: Float,
        val firstVisibleItemIndex: Int,
        val firstVisibleItemOffset: Int
    ) : ReaderEvent()

    data class OnScrollToChapter(
        val chapter: Chapter
    ) : ReaderEvent()

    data class OnScroll(
        val progress: Float
    ) : ReaderEvent()

    data object OnRestoreCheckpoint : ReaderEvent()

    data class OnLeave(
        val activity: ComponentActivity,
        val navigate: () -> Unit
    ) : ReaderEvent()

    data class OnOpenTranslator(
        val textToTranslate: String,
        val translateWholeParagraph: Boolean,
        val activity: ComponentActivity
    ) : ReaderEvent()

    data class OnOpenExternalTranslator(
        val textToTranslate: String,
        val translateWholeParagraph: Boolean,
        val activity: ComponentActivity
    ) : ReaderEvent()

    data class OnTranslateText(
        val textToTranslate: String,
        val readerTextIndex: Int? = null,
        val sourceLanguageCode: String,
        val targetLanguageCode: String,
        val providerMode: String,
        val requireWifi: Boolean,
        val activity: ComponentActivity,
        val translateWholeParagraph: Boolean
    ) : ReaderEvent()

    data object OnDismissTranslation : ReaderEvent()

    data object OnToggleTranslationOriginal : ReaderEvent()

    data class OnChangeBookTranslationProviderMode(
        val providerMode: String
    ) : ReaderEvent()

    data class OnChangeBookTranslationSourceLanguage(
        val languageCode: String
    ) : ReaderEvent()

    data class OnChangeBookTranslationTargetLanguage(
        val languageCode: String
    ) : ReaderEvent()

    data object OnSwapBookTranslationLanguages : ReaderEvent()

    data class OnChangeBookTranslationWifiOnly(
        val requireWifi: Boolean
    ) : ReaderEvent()

    data object OnStartBookTranslation : ReaderEvent()

    data object OnShowTranslatedBook : ReaderEvent()

    data object OnShowOriginalBook : ReaderEvent()

    data object OnConfirmBookTranslationGoogleWarning : ReaderEvent()

    data object OnDismissBookTranslationGoogleWarning : ReaderEvent()

    data class OnCancelBookTranslation(
        val translationId: Long
    ) : ReaderEvent()

    data class OnPauseBookTranslation(
        val translationId: Long
    ) : ReaderEvent()

    data class OnResumeBookTranslation(
        val translationId: Long
    ) : ReaderEvent()

    data class OnRetryBookTranslation(
        val translationId: Long
    ) : ReaderEvent()

    data object OnDismissBookTranslationError : ReaderEvent()

    data class OnOpenShareApp(
        val textToShare: String,
        val activity: ComponentActivity
    ) : ReaderEvent()

    data class OnOpenWebBrowser(
        val textToSearch: String,
        val activity: ComponentActivity
    ) : ReaderEvent()

    data class OnOpenDictionary(
        val textToDefine: String,
        val activity: ComponentActivity
    ) : ReaderEvent()

    data object OnShowSettingsBottomSheet : ReaderEvent()

    data object OnDismissBottomSheet : ReaderEvent()

    data object OnShowChaptersDrawer : ReaderEvent()

    data object OnDismissDrawer : ReaderEvent()
}
