/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.ui.about

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.byteflipper.everbook.R
import com.byteflipper.everbook.domain.distribution.ManualUpdateChecker
import com.byteflipper.everbook.domain.distribution.StoreUpdateController
import com.byteflipper.everbook.domain.distribution.UpdateCheckResult
import com.byteflipper.everbook.presentation.core.util.launchActivity
import com.byteflipper.everbook.presentation.core.util.showToast
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class AboutState(
    val isCheckingUpdate: Boolean = false,
    val updateSheet: UpdateCheckResult.UpdateAvailable? = null,
    val canLeaveReview: Boolean = false
)

@HiltViewModel
class AboutModel @Inject constructor(
    private val updateChecker: ManualUpdateChecker,
    private val storeUpdateController: StoreUpdateController
) : ViewModel() {

    private val _state = MutableStateFlow(
        AboutState(canLeaveReview = storeUpdateController.supportsInAppReview)
    )
    val state: StateFlow<AboutState> = _state.asStateFlow()

    fun onEvent(event: AboutEvent) {
        when (event) {
            is AboutEvent.OnNavigateToBrowserPage -> {
                viewModelScope.launch {
                    val intent = Intent(Intent.ACTION_VIEW, event.page.toUri())
                    intent.launchActivity(event.context as ComponentActivity) {
                        withContext(Dispatchers.Main) {
                            event.context.getString(R.string.error_no_browser)
                                .showToast(context = event.context, longToast = false)
                        }
                    }
                }
            }

            is AboutEvent.OnCheckForUpdate -> {
                if (_state.value.isCheckingUpdate) return
                checkForUpdate(event.context as ComponentActivity)
            }

            is AboutEvent.OnLeaveReview -> {
                storeUpdateController.requestReview(event.context as ComponentActivity)
            }
        }
    }

    fun dismissUpdateSheet() {
        _state.update { it.copy(updateSheet = null) }
    }

    fun startUpdate(activity: ComponentActivity) {
        val sheet = _state.value.updateSheet ?: return
        _state.update { it.copy(updateSheet = null) }
        sheet.onStartUpdate(activity)
    }

    private fun checkForUpdate(activity: ComponentActivity) {
        _state.update { it.copy(isCheckingUpdate = true) }
        viewModelScope.launch {
            val result = updateChecker.checkForUpdate(activity)
            _state.update { current ->
                when (result) {
                    is UpdateCheckResult.UpdateAvailable -> {
                        if (result.useStoreNativeFlow) {
                            // Store flavours: launch the native update flow immediately.
                            result.onStartUpdate(activity)
                            current.copy(isCheckingUpdate = false)
                        } else {
                            // everbook: show a bottom sheet first.
                            current.copy(isCheckingUpdate = false, updateSheet = result)
                        }
                    }
                    is UpdateCheckResult.UpToDate -> {
                        activity.getString(R.string.app_up_to_date)
                            .showToast(context = activity, longToast = false)
                        current.copy(isCheckingUpdate = false)
                    }
                    is UpdateCheckResult.Error -> {
                        activity.getString(R.string.error_something_went_wrong)
                            .showToast(context = activity, longToast = false)
                        current.copy(isCheckingUpdate = false)
                    }
                }
            }
        }
    }
}
