/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.data.distribution

import com.byteflipper.everbook.domain.distribution.AvailableUpdate
import com.byteflipper.everbook.domain.distribution.UpdateNotificationChecker
import javax.inject.Inject

/**
 * Background update check for the everbook (direct APK / GitHub) flavor. Tapping the
 * notification opens the GitHub release page.
 */
class EverbookUpdateNotificationChecker @Inject constructor(
    private val releaseSource: GitHubReleaseSource
) : UpdateNotificationChecker {

    override suspend fun check(): AvailableUpdate? {
        val latest = releaseSource.fetchLatestRelease() ?: return null
        if (!releaseSource.isNewerThanCurrent(latest.tagName)) return null
        return AvailableUpdate(
            versionLabel = latest.tagName,
            uri = latest.htmlUrl
        )
    }
}
