/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.domain.distribution

/**
 * Background, activity-less update check used to post a localized "new version available"
 * notification. Unlike [ManualUpdateChecker], this never needs a [ComponentActivity] and is
 * safe to run from a WorkManager worker.
 *
 * Each distribution flavor provides its own implementation (GitHub releases for everbook,
 * the store In-App Update SDKs for playStore / ruStore).
 */
interface UpdateNotificationChecker {
    /** Returns the available update, or `null` if up to date or the check could not run. */
    suspend fun check(): AvailableUpdate?
}

/**
 * A newer version found in the background.
 *
 * @property versionLabel human-readable version shown in the notification text.
 * @property uri opened via `ACTION_VIEW` when the user taps the notification
 *   (GitHub release page or the app's store listing).
 */
data class AvailableUpdate(
    val versionLabel: String,
    val uri: String
)
