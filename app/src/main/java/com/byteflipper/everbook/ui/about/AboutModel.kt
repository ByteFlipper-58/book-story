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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.byteflipper.everbook.R
import com.byteflipper.everbook.presentation.core.util.launchActivity
import com.byteflipper.everbook.presentation.core.util.showToast
import javax.inject.Inject

@HiltViewModel
class AboutModel @Inject constructor() : ViewModel() {

    fun onEvent(event: AboutEvent) {
        when (event) {
            is AboutEvent.OnNavigateToBrowserPage -> {
                viewModelScope.launch {
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        event.page.toUri()
                    )

                    intent.launchActivity(event.context as ComponentActivity) {
                        withContext(Dispatchers.Main) {
                            event.context.getString(R.string.error_no_browser)
                                .showToast(context = event.context, longToast = false)
                        }
                    }
                }
            }
        }
    }
}