/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.domain.assist

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * What the user is reading right now, as last published by the reader.
 *
 * Reading state lives in the reader's ViewModel, which neither the Activity's assist callbacks nor
 * the App Functions service can reach. This app-scoped holder is the bridge: the reader writes into
 * it while a book is open and clears it on exit.
 */
@Singleton
class ReadingContextTracker @Inject constructor() {

    private val _current = MutableStateFlow<OpenReadingContext?>(null)
    val current: StateFlow<OpenReadingContext?> = _current.asStateFlow()

    fun publish(context: OpenReadingContext) {
        _current.value = context
    }

    fun clear() {
        _current.value = null
    }
}

/**
 * Snapshot of an open book. [excerpt] is a short piece of the text at the reading position, kept
 * small on purpose: it may be handed to an assistant, which usually means leaving the device.
 */
data class OpenReadingContext(
    val bookId: Int,
    val title: String,
    val author: String?,
    val progress: Float,
    val chapterTitle: String?,
    val chapterProgress: Float,
    val excerpt: String?,
    val updatedAtMillis: Long = System.currentTimeMillis()
) {
    companion object {
        /** Upper bound for [excerpt], in characters. */
        const val EXCERPT_MAX_CHARS = 600
    }
}
