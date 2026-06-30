/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.ui.settings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.byteflipper.everbook.domain.translation.normalizeTranslationLanguageCode
import com.byteflipper.everbook.domain.use_case.translation.DeleteTranslationModel
import com.byteflipper.everbook.domain.use_case.translation.DownloadTranslationModel
import com.byteflipper.everbook.domain.use_case.translation.GetAvailableTranslationModels
import com.byteflipper.everbook.domain.use_case.translation.GetTranslationModelAvailability
import com.byteflipper.everbook.domain.use_case.translation.GetTranslationModels
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.yield
import javax.inject.Inject

private const val TRANSLATION_MODELS_LOG = "TranslationModels"

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
        Log.i(TRANSLATION_MODELS_LOG, "Model manager availability: available=$available")
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
            TranslatorSettingsEvent.OnRefreshModels -> refreshModels(clearError = true)

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

            is TranslatorSettingsEvent.OnDownloadModel -> runModelOperation(
                operationName = "download",
                languageCode = event.languageCode
            ) { normalizedLanguageCode ->
                downloadTranslationModel.execute(
                    languageCode = normalizedLanguageCode,
                    requireWifi = event.requireWifi
                )
            }

            is TranslatorSettingsEvent.OnDeleteModel -> runModelOperation(
                operationName = "delete",
                languageCode = event.languageCode
            ) { normalizedLanguageCode ->
                deleteTranslationModel.execute(normalizedLanguageCode)
            }

            TranslatorSettingsEvent.OnDismissModelError -> {
                viewModelScope.launch {
                    _state.update { it.copy(errorMessage = null) }
                }
            }
        }
    }

    private fun refreshModels(clearError: Boolean) {
        if (!_state.value.modelManagerAvailable) {
            Log.w(TRANSLATION_MODELS_LOG, "Refresh ignored: model manager unavailable")
            return
        }

        refreshJob?.cancel()
        refreshJob = viewModelScope.launch(Dispatchers.IO) {
            Log.i(TRANSLATION_MODELS_LOG, "Refreshing models: clearError=$clearError")
            _state.update {
                it.copy(
                    isLoadingModels = true,
                    errorMessage = if (clearError) null else it.errorMessage
                )
            }

            runCatching {
                getTranslationModels.execute()
            }.onSuccess { models ->
                Log.i(
                    TRANSLATION_MODELS_LOG,
                    "Models refreshed: count=${models.size} downloaded=${models.count { it.downloaded }}"
                )
                _state.update {
                    it.copy(
                        models = models,
                        isLoadingModels = false,
                        errorMessage = if (clearError) null else it.errorMessage
                    )
                }
            }.onFailure { throwable ->
                Log.e(TRANSLATION_MODELS_LOG, "Models refresh failed", throwable)
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
        operationName: String,
        languageCode: String,
        operation: suspend (String) -> Unit
    ) {
        val normalizedLanguageCode = normalizeTranslationLanguageCode(languageCode)
        if (normalizedLanguageCode == null) {
            Log.w(
                TRANSLATION_MODELS_LOG,
                "Model operation rejected: operation=$operationName language=$languageCode"
            )
            viewModelScope.launch {
                _state.update {
                    it.copy(errorMessage = "Unsupported translation language.")
                }
            }
            return
        }
        if (normalizedLanguageCode in _state.value.busyLanguageCodes) {
            Log.i(
                TRANSLATION_MODELS_LOG,
                "Model operation ignored: operation=$operationName language=$normalizedLanguageCode busy=true"
            )
            return
        }
        if (!_state.value.modelManagerAvailable) {
            Log.w(
                TRANSLATION_MODELS_LOG,
                "Model operation rejected: operation=$operationName language=$normalizedLanguageCode unavailable=true"
            )
            viewModelScope.launch {
                _state.update {
                    it.copy(errorMessage = "Offline translation models are unavailable in this build.")
                }
            }
            return
        }

        modelOperationJobs[normalizedLanguageCode]?.cancel()
        modelOperationJobs[normalizedLanguageCode] = viewModelScope.launch(Dispatchers.IO) {
            Log.i(
                TRANSLATION_MODELS_LOG,
                "Model operation started: operation=$operationName language=$normalizedLanguageCode"
            )
            _state.update {
                it.copy(
                    busyLanguageCodes = it.busyLanguageCodes + normalizedLanguageCode,
                    errorMessage = null
                )
            }

            try {
                operation(normalizedLanguageCode)
                Log.i(
                    TRANSLATION_MODELS_LOG,
                    "Model operation finished: operation=$operationName language=$normalizedLanguageCode"
                )
            } catch (exception: CancellationException) {
                Log.i(
                    TRANSLATION_MODELS_LOG,
                    "Model operation cancelled: operation=$operationName language=$normalizedLanguageCode"
                )
                throw exception
            } catch (throwable: Throwable) {
                Log.e(
                    TRANSLATION_MODELS_LOG,
                    "Model operation failed: operation=$operationName language=$normalizedLanguageCode",
                    throwable
                )
                _state.update {
                    it.copy(errorMessage = throwable.message ?: "Could not update translation model.")
                }
            } finally {
                _state.update {
                    it.copy(
                        busyLanguageCodes = it.busyLanguageCodes - normalizedLanguageCode
                    )
                }
                modelOperationJobs.remove(normalizedLanguageCode)
                refreshModels(clearError = false)
            }
        }
    }

    private suspend inline fun <T> MutableStateFlow<T>.update(function: (T) -> T) {
        mutex.withLock {
            yield()
            this.value = function(this.value)
        }
    }
}
