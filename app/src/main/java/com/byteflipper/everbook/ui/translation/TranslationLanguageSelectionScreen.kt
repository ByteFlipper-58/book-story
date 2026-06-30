/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.ui.translation

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.byteflipper.everbook.domain.navigator.Screen
import com.byteflipper.everbook.presentation.navigator.LocalNavigator
import com.byteflipper.everbook.presentation.translation.language_selection.TranslationLanguageSelectionLayout
import com.byteflipper.everbook.presentation.translation.language_selection.TranslationLanguageSelectionRole
import com.byteflipper.everbook.ui.main.MainEvent
import com.byteflipper.everbook.ui.main.MainModel
import com.byteflipper.everbook.ui.reader.ReaderEvent
import com.byteflipper.everbook.ui.reader.ReaderModel
import com.byteflipper.everbook.ui.settings.TranslatorSettingsEvent
import com.byteflipper.everbook.ui.settings.TranslatorSettingsModel
import kotlinx.parcelize.Parcelize

@Parcelize
data class TranslationLanguageSelectionScreen(
    val target: TranslationLanguageSelectionTarget,
    val role: TranslationLanguageSelectionRole,
    val bookId: Int? = null
) : Screen, Parcelable {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current

        when (target) {
            TranslationLanguageSelectionTarget.SETTINGS -> {
                SettingsTranslationLanguageSelection(
                    role = role,
                    navigateBack = { navigator.pop() }
                )
            }

            TranslationLanguageSelectionTarget.BOOK_TRANSLATION -> {
                BookTranslationLanguageSelection(
                    role = role,
                    bookId = bookId,
                    navigateBack = { navigator.pop() }
                )
            }
        }
    }
}

enum class TranslationLanguageSelectionTarget {
    SETTINGS,
    BOOK_TRANSLATION
}

@Composable
private fun SettingsTranslationLanguageSelection(
    role: TranslationLanguageSelectionRole,
    navigateBack: () -> Unit
) {
    val mainModel = hiltViewModel<MainModel>()
    val mainState = mainModel.state.collectAsStateWithLifecycle()
    val settingsModel = hiltViewModel<TranslatorSettingsModel>()
    val settingsState = settingsModel.state.collectAsStateWithLifecycle()

    TranslationLanguageSelectionLayout(
        role = role,
        providerMode = mainState.value.translationProviderMode,
        selectedLanguageCode = when (role) {
            TranslationLanguageSelectionRole.SOURCE -> mainState.value.translationSourceLanguage
            TranslationLanguageSelectionRole.TARGET -> mainState.value.translationTargetLanguage
        },
        requireWifi = mainState.value.translationWifiOnly,
        translatorState = settingsState.value,
        navigateBack = navigateBack,
        onRefreshModels = {
            settingsModel.onEvent(TranslatorSettingsEvent.OnRefreshModels)
        },
        onDownloadModel = { languageCode ->
            settingsModel.onEvent(
                TranslatorSettingsEvent.OnDownloadModel(
                    languageCode = languageCode,
                    requireWifi = mainState.value.translationWifiOnly
                )
            )
        },
        onSelectLanguage = { languageCode ->
            mainModel.onEvent(
                when (role) {
                    TranslationLanguageSelectionRole.SOURCE ->
                        MainEvent.OnChangeTranslationSourceLanguage(languageCode)

                    TranslationLanguageSelectionRole.TARGET ->
                        MainEvent.OnChangeTranslationTargetLanguage(languageCode)
                }
            )
        }
    )
}

@Composable
private fun BookTranslationLanguageSelection(
    role: TranslationLanguageSelectionRole,
    bookId: Int?,
    navigateBack: () -> Unit
) {
    val readerModel = hiltViewModel<ReaderModel>()
    val readerState = readerModel.state.collectAsStateWithLifecycle()
    val settingsModel = hiltViewModel<TranslatorSettingsModel>()
    val settingsState = settingsModel.state.collectAsStateWithLifecycle()
    val bookTranslation = readerState.value.bookTranslation
    val isCurrentReaderBook = bookId == null || readerState.value.book.id == bookId

    TranslationLanguageSelectionLayout(
        role = role,
        providerMode = bookTranslation.providerMode,
        selectedLanguageCode = when (role) {
            TranslationLanguageSelectionRole.SOURCE -> bookTranslation.sourceLanguageCode
            TranslationLanguageSelectionRole.TARGET -> bookTranslation.targetLanguageCode
        },
        requireWifi = bookTranslation.requireWifi,
        translatorState = settingsState.value,
        navigateBack = navigateBack,
        onRefreshModels = {
            settingsModel.onEvent(TranslatorSettingsEvent.OnRefreshModels)
        },
        onDownloadModel = { languageCode ->
            settingsModel.onEvent(
                TranslatorSettingsEvent.OnDownloadModel(
                    languageCode = languageCode,
                    requireWifi = bookTranslation.requireWifi
                )
            )
        },
        onSelectLanguage = { languageCode ->
            if (isCurrentReaderBook) {
                readerModel.onEvent(
                    when (role) {
                        TranslationLanguageSelectionRole.SOURCE ->
                            ReaderEvent.OnChangeBookTranslationSourceLanguage(languageCode)

                        TranslationLanguageSelectionRole.TARGET ->
                            ReaderEvent.OnChangeBookTranslationTargetLanguage(languageCode)
                    }
                )
            }
        }
    )
}
