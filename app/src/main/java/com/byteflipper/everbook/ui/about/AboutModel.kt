/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.ui.about

import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import com.byteflipper.everbook.R
import com.byteflipper.everbook.domain.use_case.remote.CheckForUpdates
import com.byteflipper.everbook.presentation.core.util.launchActivity
import com.byteflipper.everbook.presentation.core.util.showToast
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class AboutModel @Inject constructor(
    private val checkForUpdates: CheckForUpdates
) : ViewModel() {

    private val mutex = Mutex()

    private val _state = MutableStateFlow(AboutState())
    val state = _state.asStateFlow()

    fun onEvent(event: AboutEvent) {
        when (event) {
            is AboutEvent.OnNavigateToBrowserPage -> {
                viewModelScope.launch {
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(event.page)
                    )

                    intent.launchActivity(event.context as ComponentActivity) {
                        withContext(Dispatchers.Main) {
                            event.context.getString(R.string.error_no_browser)
                                .showToast(context = event.context, longToast = false)
                        }
                    }
                }
            }

            is AboutEvent.OnCheckForUpdate -> {
                viewModelScope.launch {
                    if (_state.value.updateChecked) {
                        withContext(Dispatchers.Main) {
                            event.context.getString(R.string.no_updates)
                                .showToast(context = event.context)
                        }
                        return@launch
                    }

                    _state.update {
                        it.copy(updateLoading = true)
                    }
                    val result = checkForUpdates.execute(false)

                    if (result == null) {
                        _state.update {
                            it.copy(updateLoading = false)
                        }
                        withContext(Dispatchers.Main) {
                            event.context.getString(R.string.error_check_internet)
                                .showToast(context = event.context)
                        }
                        return@launch
                    }

                    val version = result.tagName.substringAfterLast("v")
                    val currentVersion = event.context.getString(R.string.app_version)

                    if (version == currentVersion) {
                        withContext(Dispatchers.Main) {
                            event.context.getString(R.string.no_updates)
                                .showToast(context = event.context)
                        }
                        _state.update {
                            it.copy(
                                updateLoading = false,
                                updateChecked = true
                            )
                        }
                        return@launch
                    }

                    _state.update {
                        it.copy(
                            dialog = AboutScreen.UPDATE_DIALOG,
                            updateLoading = false,
                            updateInfo = result
                        )
                    }
                }
            }

            is AboutEvent.OnDismissDialog -> {
                viewModelScope.launch {
                    _state.update {
                        it.copy(dialog = null)
                    }
                }
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