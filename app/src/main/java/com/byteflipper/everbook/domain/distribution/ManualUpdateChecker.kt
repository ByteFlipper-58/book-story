/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.domain.distribution

import androidx.activity.ComponentActivity

/**
 * Result of a manual update check triggered from the About screen.
 */
sealed class UpdateCheckResult {
    /**
     * A newer version is available. Call [onStartUpdate] to begin the update flow.
     *
     * [useStoreNativeFlow] = true for store flavours (Play Store / RuStore): the store shows
     * its own native update UI, so the caller invokes [onStartUpdate] immediately without any
     * custom dialog. For everbook it is false: the caller shows its own bottom sheet first.
     */
    data class UpdateAvailable(
        val version: String,
        val useStoreNativeFlow: Boolean,
        val onStartUpdate: (ComponentActivity) -> Unit
    ) : UpdateCheckResult()

    /** The current version is the latest. */
    data object UpToDate : UpdateCheckResult()

    /** The check failed (network error, SDK unavailable, etc.). */
    data class Error(val message: String?) : UpdateCheckResult()
}

/**
 * Manual update checker — called when the user taps the app version on the About screen.
 */
interface ManualUpdateChecker {
    suspend fun checkForUpdate(activity: ComponentActivity): UpdateCheckResult
}
