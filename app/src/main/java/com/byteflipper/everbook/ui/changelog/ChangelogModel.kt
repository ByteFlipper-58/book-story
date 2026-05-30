/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.ui.changelog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.byteflipper.everbook.domain.use_case.changelog.GetChangelogReleases
import javax.inject.Inject

@HiltViewModel
class ChangelogModel @Inject constructor(
    private val getChangelogReleases: GetChangelogReleases
) : ViewModel() {

    private val _state = MutableStateFlow(ChangelogState())
    val state = _state.asStateFlow()

    private var selectedLanguage: String? = null

    fun load(language: String, initialVersionCode: Int? = null) {
        viewModelScope.launch(Dispatchers.Main.immediate) {
            _state.update { it.copy(isLoading = true) }

            updateRelease(
                language = selectedLanguage ?: language,
                versionCode = initialVersionCode
            )
        }
    }

    fun onEvent(event: ChangelogEvent) {
        when (event) {
            is ChangelogEvent.OnSelectLanguage -> {
                selectedLanguage = event.language

                viewModelScope.launch(Dispatchers.Main.immediate) {
                    updateRelease(
                        language = event.language,
                        versionCode = state.value.selectedRelease?.versionCode
                    )
                }
            }
        }
    }

    private suspend fun updateRelease(language: String, versionCode: Int? = null) {
        val releases = getChangelogReleases.execute(language)
        val selectedRelease = releases.find { it.versionCode == versionCode }
            ?: releases.firstOrNull()

        _state.update {
            it.copy(
                isLoading = false,
                releases = releases,
                selectedRelease = selectedRelease
            )
        }
    }
}
