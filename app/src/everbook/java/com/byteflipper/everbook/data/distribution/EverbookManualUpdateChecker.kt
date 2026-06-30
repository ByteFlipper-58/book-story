/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.data.distribution

import android.content.Intent
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.core.net.toUri
import com.byteflipper.everbook.domain.distribution.ManualUpdateChecker
import com.byteflipper.everbook.domain.distribution.UpdateCheckResult
import javax.inject.Inject

private const val TAG = "EverbookUpdateCheck"

class EverbookManualUpdateChecker @Inject constructor(
    private val releaseSource: GitHubReleaseSource
) : ManualUpdateChecker {

    override suspend fun checkForUpdate(
        activity: ComponentActivity
    ): UpdateCheckResult {
        return try {
            val latest = releaseSource.fetchLatestRelease()
            if (latest != null && releaseSource.isNewerThanCurrent(latest.tagName)) {
                UpdateCheckResult.UpdateAvailable(
                    version = latest.tagName,
                    useStoreNativeFlow = false,
                    onStartUpdate = { act -> openUrl(act, latest.htmlUrl) }
                )
            } else {
                UpdateCheckResult.UpToDate
            }
        } catch (e: Exception) {
            Log.w(TAG, "Update check failed", e)
            UpdateCheckResult.Error(e.message)
        }
    }

    private fun openUrl(activity: ComponentActivity, url: String) {
        val intent = Intent(Intent.ACTION_VIEW, url.toUri())
        try {
            activity.startActivity(intent)
        } catch (_: Exception) {
            // fallback: browser intent failed, nothing to do
        }
    }
}
