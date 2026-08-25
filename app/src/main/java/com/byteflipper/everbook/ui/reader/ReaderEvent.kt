/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.ui.reader

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Immutable
import com.byteflipper.everbook.domain.reader.Bookmark
import com.byteflipper.everbook.domain.reader.PdfReadingMode
import com.byteflipper.everbook.domain.reader.ReaderText.Chapter
import com.byteflipper.everbook.domain.reader.tts.TtsPreferences

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

    /**
     * Switch the bottom sheet to a different already-existing book translation (provider/source/
     * target). The reader picks up the matching row via [refreshBookTranslations].
     */
    data class OnSelectBookTranslation(
        val translationId: Long
    ) : ReaderEvent()

    data class OnDeleteBookTranslation(
        val translationId: Long
    ) : ReaderEvent()

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

    data class OnDeleteBookmark(val id: Int) : ReaderEvent()

    data class OnScrollToBookmark(val bookmark: Bookmark) : ReaderEvent()

    data class OnBookmarkScrollFinished(val bookmark: Bookmark) : ReaderEvent()

    data object OnShowBookmarksDrawer : ReaderEvent()

    data class OnCreateBookmark(val selectedText: String) : ReaderEvent()

    data class OnShowHighlightPalette(
        val selectedText: String,
        val anchorX: Int,
        val anchorY: Int,
        val annotation: Bookmark? = null
    ) : ReaderEvent()

    data class OnCreateHighlight(val selectedText: String, val colorArgb: Int) : ReaderEvent()

    data class OnRequestAnnotationEditor(
        val selectedText: String,
        val initialColorArgb: Int? = null
    ) : ReaderEvent()

    data object OnDismissHighlightPalette : ReaderEvent()

    data class OnShowHighlightPaletteEditor(
        val selectedText: String? = null,
        val annotation: Bookmark? = null
    ) : ReaderEvent()

    data object OnDismissHighlightPaletteEditor : ReaderEvent()

    data class OnUpdateHighlightPalette(val colors: List<Int>) : ReaderEvent()

    data class OnApplyHighlightPaletteColor(val colorArgb: Int) : ReaderEvent()

    data class OnSaveAnnotation(val note: String, val colorArgb: Int?) : ReaderEvent()

    data object OnDismissAnnotationEditor : ReaderEvent()

    /** Recolor an existing highlight. */
    data class OnChangeHighlightColor(val id: Int, val colorArgb: Int) : ReaderEvent()

    /** Removes only the highlight color while preserving the bookmark and its note. */
    data class OnClearHighlightColor(val id: Int) : ReaderEvent()

    data class OnEditAnnotation(val bookmark: Bookmark) : ReaderEvent()
    data class OnSetAutoScrolling(val active: Boolean) : ReaderEvent()
    data class OnSetAutoScrollPaused(val paused: Boolean) : ReaderEvent()

    /** Starts reading aloud from the paragraph the reader is currently showing. */
    data class OnStartTts(val preferences: TtsPreferences) : ReaderEvent()
    data object OnStopTts : ReaderEvent()
    data object OnToggleTtsPlayback : ReaderEvent()
    data object OnTtsNextParagraph : ReaderEvent()
    data object OnTtsPreviousParagraph : ReaderEvent()
    data object OnTtsNextSentence : ReaderEvent()
    data object OnTtsPreviousSentence : ReaderEvent()
    data class OnApplyTtsPreferences(val preferences: TtsPreferences) : ReaderEvent()
}
