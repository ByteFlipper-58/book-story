/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.reader

import androidx.compose.runtime.Composable
import com.byteflipper.everbook.domain.library.book.Book
import com.byteflipper.everbook.domain.reader.PdfReadingMode
import com.byteflipper.everbook.domain.util.BottomSheet
import com.byteflipper.everbook.presentation.core.util.LocalActivity
import com.byteflipper.everbook.ui.reader.ReaderBookTranslationState
import com.byteflipper.everbook.ui.reader.ReaderEvent
import com.byteflipper.everbook.ui.reader.ReaderScreen

@Composable
fun ReaderBottomSheet(
    book: Book,
    bottomSheet: BottomSheet?,
    translation: com.byteflipper.everbook.ui.reader.ReaderTranslationState,
    bookTranslation: ReaderBookTranslationState,
    fullscreenMode: Boolean,
    pdfTextModeUnavailable: Boolean,
    changePdfReadingMode: (ReaderEvent.OnChangePdfReadingMode) -> Unit,
    changePdfDefaultReadingMode: (PdfReadingMode) -> Unit,
    menuVisibility: (ReaderEvent.OnMenuVisibility) -> Unit,
    openExternalTranslator: (ReaderEvent.OnOpenExternalTranslator) -> Unit,
    toggleTranslationOriginal: (ReaderEvent.OnToggleTranslationOriginal) -> Unit,
    dismissTranslation: (ReaderEvent.OnDismissTranslation) -> Unit,
    startBookTranslation: (ReaderEvent.OnStartBookTranslation) -> Unit,
    showTranslatedBook: (ReaderEvent.OnShowTranslatedBook) -> Unit,
    showOriginalBook: (ReaderEvent.OnShowOriginalBook) -> Unit,
    confirmBookTranslationGoogleWarning: (ReaderEvent.OnConfirmBookTranslationGoogleWarning) -> Unit,
    dismissBookTranslationGoogleWarning: (ReaderEvent.OnDismissBookTranslationGoogleWarning) -> Unit,
    cancelBookTranslation: (ReaderEvent.OnCancelBookTranslation) -> Unit,
    pauseBookTranslation: (ReaderEvent.OnPauseBookTranslation) -> Unit,
    resumeBookTranslation: (ReaderEvent.OnResumeBookTranslation) -> Unit,
    retryBookTranslation: (ReaderEvent.OnRetryBookTranslation) -> Unit,
    changeBookTranslationProviderMode: (ReaderEvent.OnChangeBookTranslationProviderMode) -> Unit,
    changeBookTranslationSourceLanguage: (ReaderEvent.OnChangeBookTranslationSourceLanguage) -> Unit,
    changeBookTranslationTargetLanguage: (ReaderEvent.OnChangeBookTranslationTargetLanguage) -> Unit,
    swapBookTranslationLanguages: (ReaderEvent.OnSwapBookTranslationLanguages) -> Unit,
    changeBookTranslationWifiOnly: (ReaderEvent.OnChangeBookTranslationWifiOnly) -> Unit,
    dismissBookTranslationError: (ReaderEvent.OnDismissBookTranslationError) -> Unit,
    dismissBottomSheet: (ReaderEvent.OnDismissBottomSheet) -> Unit
) {
    val activity = LocalActivity.current

    when (bottomSheet) {
        ReaderScreen.SETTINGS_BOTTOM_SHEET -> {
            ReaderSettingsBottomSheet(
                fullscreenMode = fullscreenMode,
                menuVisibility = { show, fullscreen ->
                    menuVisibility(
                        ReaderEvent.OnMenuVisibility(
                            show = show,
                            fullscreenMode = fullscreen,
                            saveCheckpoint = false,
                            activity = activity
                        )
                    )
                },
                dismissBottomSheet = {
                    dismissBottomSheet(ReaderEvent.OnDismissBottomSheet)
                }
            )
        }

        ReaderScreen.PDF_READING_MODE_BOTTOM_SHEET -> {
            PdfReadingModeBottomSheet(
                book = book,
                pdfTextModeUnavailable = pdfTextModeUnavailable,
                changePdfReadingMode = {
                    changePdfReadingMode(ReaderEvent.OnChangePdfReadingMode(it))
                },
                changePdfDefaultReadingMode = changePdfDefaultReadingMode,
                dismissBottomSheet = {
                    dismissBottomSheet(ReaderEvent.OnDismissBottomSheet)
                }
            )
        }

        ReaderScreen.TRANSLATION_BOTTOM_SHEET -> {
            ReaderTranslationBottomSheet(
                translation = translation,
                openExternalTranslator = {
                    openExternalTranslator(
                        ReaderEvent.OnOpenExternalTranslator(
                            textToTranslate = translation.text,
                            translateWholeParagraph = false,
                            activity = activity
                        )
                    )
                },
                toggleTranslationOriginal = {
                    toggleTranslationOriginal(ReaderEvent.OnToggleTranslationOriginal)
                },
                dismissBottomSheet = {
                    dismissTranslation(ReaderEvent.OnDismissTranslation)
                }
            )
        }

        ReaderScreen.BOOK_TRANSLATION_BOTTOM_SHEET -> {
            ReaderBookTranslationBottomSheet(
                state = bookTranslation,
                startTranslation = {
                    startBookTranslation(ReaderEvent.OnStartBookTranslation)
                },
                showTranslatedBook = {
                    showTranslatedBook(ReaderEvent.OnShowTranslatedBook)
                },
                showOriginalBook = {
                    showOriginalBook(ReaderEvent.OnShowOriginalBook)
                },
                confirmGoogleWarning = {
                    confirmBookTranslationGoogleWarning(
                        ReaderEvent.OnConfirmBookTranslationGoogleWarning
                    )
                },
                dismissGoogleWarning = {
                    dismissBookTranslationGoogleWarning(
                        ReaderEvent.OnDismissBookTranslationGoogleWarning
                    )
                },
                cancelTranslation = {
                    cancelBookTranslation(ReaderEvent.OnCancelBookTranslation(it))
                },
                pauseTranslation = {
                    pauseBookTranslation(ReaderEvent.OnPauseBookTranslation(it))
                },
                resumeTranslation = {
                    resumeBookTranslation(ReaderEvent.OnResumeBookTranslation(it))
                },
                retryTranslation = {
                    retryBookTranslation(ReaderEvent.OnRetryBookTranslation(it))
                },
                changeProviderMode = {
                    changeBookTranslationProviderMode(
                        ReaderEvent.OnChangeBookTranslationProviderMode(it)
                    )
                },
                changeSourceLanguage = {
                    changeBookTranslationSourceLanguage(
                        ReaderEvent.OnChangeBookTranslationSourceLanguage(it)
                    )
                },
                changeTargetLanguage = {
                    changeBookTranslationTargetLanguage(
                        ReaderEvent.OnChangeBookTranslationTargetLanguage(it)
                    )
                },
                swapLanguages = {
                    swapBookTranslationLanguages(ReaderEvent.OnSwapBookTranslationLanguages)
                },
                changeWifiOnly = {
                    changeBookTranslationWifiOnly(
                        ReaderEvent.OnChangeBookTranslationWifiOnly(it)
                    )
                },
                dismissError = {
                    dismissBookTranslationError(ReaderEvent.OnDismissBookTranslationError)
                },
                dismissBottomSheet = {
                    dismissBottomSheet(ReaderEvent.OnDismissBottomSheet)
                }
            )
        }
    }
}
