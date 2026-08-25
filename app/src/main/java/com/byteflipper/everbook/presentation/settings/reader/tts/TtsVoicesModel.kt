/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.settings.reader.tts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.byteflipper.everbook.data.tts.TtsSessionManager
import com.byteflipper.everbook.domain.reader.tts.TtsVoice
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Exposes the voices installed on the device so the read-aloud settings can offer a picker. */
@HiltViewModel
class TtsVoicesModel @Inject constructor(
    private val sessionManager: TtsSessionManager
) : ViewModel() {

    val voices: StateFlow<List<TtsVoice>> = sessionManager.state
        .map { it.voices }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private var loadJob: Job? = null

    /**
     * Boots the engine if needed and re-reads its voice list. Safe to call again after the user
     * comes back from the system speech settings, where voice data may have been installed.
     */
    fun loadVoices() {
        if (loadJob?.isActive == true) return
        loadJob = viewModelScope.launch {
            sessionManager.loadVoices()
        }
    }
}
