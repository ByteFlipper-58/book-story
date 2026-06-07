/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.byteflipper.everbook.domain.translation.DEFAULT_TRANSLATION_TARGET_LANGUAGE
import com.byteflipper.everbook.domain.translation.normalizeTranslationLanguageCode
import com.byteflipper.everbook.domain.use_case.translation.DeleteTranslationModel
import com.byteflipper.everbook.domain.use_case.translation.DownloadTranslationModel
import com.byteflipper.everbook.domain.use_case.translation.GetAvailableTranslationModels
import com.byteflipper.everbook.domain.use_case.translation.GetTranslationModelAvailability
import com.byteflipper.everbook.domain.use_case.translation.GetTranslationModels
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.yield
import javax.inject.Inject

@HiltViewModel
class TranslatorSettingsModel @Inject constructor(
    private val getTranslationModelAvailability: GetTranslationModelAvailability,
    private val getAvailableTranslationModels: GetAvailableTranslationModels,
    private val getTranslationModels: GetTranslationModels,
    private val downloadTranslationModel: DownloadTranslationModel,
    private val deleteTranslationModel: DeleteTranslationModel
) : ViewModel() {

    private val mutex = Mutex()
    private val _state = MutableStateFlow(TranslatorSettingsState())
    val state = _state.asStateFlow()

    private var refreshJob: Job? = null
    private val modelOperationJobs = mutableMapOf<String, Job>()

    init {
        val available = getTranslationModelAvailability.execute()
        _state.value = _state.value.copy(
            modelManagerAvailable = available,
            models = if (available) getAvailableTranslationModels.execute() else emptyList()
        )
        if (available) {
            onEvent(TranslatorSettingsEvent.OnRefreshModels)
        }
    }

    fun onEvent(event: TranslatorSettingsEvent) {
        when (event) {
            TranslatorSettingsEvent.OnRefreshModels -> refreshModels()

            is TranslatorSettingsEvent.OnChangeModelSearchQuery -> {
                viewModelScope.launch {
                    _state.update { it.copy(query = event.value) }
                }
            }

            is TranslatorSettingsEvent.OnChangeModelFilter -> {
                viewModelScope.launch {
                    _state.update { it.copy(filter = event.filter) }
                }
            }

            is TranslatorSettingsEvent.OnDownloadModel -> runModelOperation(event.languageCode) {
                downloadTranslationModel.execute(
                    languageCode = event.languageCode,
                    requireWifi = event.requireWifi
                )
            }

            is TranslatorSettingsEvent.OnDeleteModel -> runModelOperation(event.languageCode) {
                deleteTranslationModel.execute(event.languageCode)
            }

            TranslatorSettingsEvent.OnDismissModelError -> {
                viewModelScope.launch {
                    _state.update { it.copy(errorMessage = null) }
                }
            }
        }
    }

    private fun refreshModels() {
        if (!_state.value.modelManagerAvailable) return

        refreshJob?.cancel()
        refreshJob = viewModelScope.launch(Dispatchers.IO) {
            _state.update {
                it.copy(isLoadingModels = true, errorMessage = null)
            }

            runCatching {
                getTranslationModels.execute()
            }.onSuccess { models ->
                _state.update {
                    it.copy(
                        models = models,
                        isLoadingModels = false,
                        errorMessage = null
                    )
                }
            }.onFailure { throwable ->
                _state.update {
                    it.copy(
                        isLoadingModels = false,
                        errorMessage = throwable.message
                    )
                }
            }
        }
    }

    private fun runModelOperation(
        languageCode: String,
        operation: suspend () -> Unit
    ) {
        val normalizedLanguageCode = normalizeTranslationLanguageCode(languageCode)
            ?: DEFAULT_TRANSLATION_TARGET_LANGUAGE
        if (
            !_state.value.modelManagerAvailable ||
            normalizedLanguageCode in _state.value.busyLanguageCodes
        ) {
            return
        }

        modelOperationJobs[normalizedLanguageCode]?.cancel()
        modelOperationJobs[normalizedLanguageCode] = viewModelScope.launch(Dispatchers.IO) {
            _state.update {
                it.copy(
                    busyLanguageCodes = it.busyLanguageCodes + normalizedLanguageCode,
                    errorMessage = null
                )
            }

            runCatching {
                operation()
            }.onFailure { throwable ->
                _state.update {
                    it.copy(errorMessage = throwable.message)
                }
            }

            _state.update {
                it.copy(
                    busyLanguageCodes = it.busyLanguageCodes - normalizedLanguageCode
                )
            }
            modelOperationJobs.remove(normalizedLanguageCode)
            refreshModels()
        }
    }

    private suspend inline fun <T> MutableStateFlow<T>.update(function: (T) -> T) {
        mutex.withLock {
            yield()
            this.value = function(this.value)
        }
    }
}
