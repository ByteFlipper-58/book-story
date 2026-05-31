/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.domain.distribution

import android.view.View
import androidx.activity.ComponentActivity
import kotlinx.coroutines.flow.StateFlow

interface ReaderInlineContentController {
    val state: StateFlow<ReaderInlineContentState>

    fun configure(activity: ComponentActivity)

    fun onReaderProgress(
        activity: ComponentActivity,
        mode: ReaderInlineContentMode,
        progressUnit: Int,
        visibleEndProgressUnit: Int,
        lastProgressUnit: Int,
        readerAvailableForInlineContent: Boolean
    )

    fun createView(activity: ComponentActivity, placementId: Long): View?

    fun resetSession()
}

data class ReaderInlineContentState(
    val placements: List<ReaderInlineContentPlacement> = emptyList()
)

data class ReaderInlineContentPlacement(
    val id: Long,
    val mode: ReaderInlineContentMode,
    val progressUnit: Int
)

enum class ReaderInlineContentMode {
    TEXT,
    PDF
}
