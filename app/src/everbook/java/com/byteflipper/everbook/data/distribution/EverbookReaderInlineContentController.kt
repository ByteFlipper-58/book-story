/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.data.distribution

import android.view.View
import androidx.activity.ComponentActivity
import com.byteflipper.everbook.domain.distribution.ReaderInlineContentController
import com.byteflipper.everbook.domain.distribution.ReaderInlineContentMode
import com.byteflipper.everbook.domain.distribution.ReaderInlineContentState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class EverbookReaderInlineContentController @Inject constructor() : ReaderInlineContentController {
    override val state: StateFlow<ReaderInlineContentState> =
        MutableStateFlow(ReaderInlineContentState())

    override fun configure(activity: ComponentActivity) = Unit

    override fun onReaderProgress(
        activity: ComponentActivity,
        mode: ReaderInlineContentMode,
        progressUnit: Int,
        visibleEndProgressUnit: Int,
        lastProgressUnit: Int,
        readerAvailableForInlineContent: Boolean
    ) = Unit

    override fun createView(activity: ComponentActivity, placementId: Long): View? = null

    override fun resetSession() = Unit
}
