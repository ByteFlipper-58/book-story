/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.ui.reader

import android.view.View
import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModel
import com.byteflipper.everbook.domain.distribution.ReaderInlineContentController
import com.byteflipper.everbook.domain.distribution.ReaderInlineContentMode
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ReaderInlineContentModel @Inject constructor(
    private val controller: ReaderInlineContentController
) : ViewModel() {
    val state = controller.state

    fun configure(activity: ComponentActivity) {
        controller.configure(activity)
    }

    fun onReaderProgress(
        activity: ComponentActivity,
        mode: ReaderInlineContentMode,
        progressUnit: Int,
        visibleEndProgressUnit: Int,
        lastProgressUnit: Int,
        readerAvailableForInlineContent: Boolean
    ) {
        controller.onReaderProgress(
            activity = activity,
            mode = mode,
            progressUnit = progressUnit,
            visibleEndProgressUnit = visibleEndProgressUnit,
            lastProgressUnit = lastProgressUnit,
            readerAvailableForInlineContent = readerAvailableForInlineContent
        )
    }

    fun createView(activity: ComponentActivity, placementId: Long): View? {
        return controller.createView(activity, placementId)
    }

    fun resetSession() {
        controller.resetSession()
    }
}
