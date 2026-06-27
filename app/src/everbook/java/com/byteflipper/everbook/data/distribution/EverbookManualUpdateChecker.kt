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
import com.byteflipper.everbook.BuildConfig
import com.byteflipper.everbook.domain.distribution.ManualUpdateChecker
import com.byteflipper.everbook.domain.distribution.UpdateCheckResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject

private const val TAG = "EverbookUpdateCheck"

class EverbookManualUpdateChecker @Inject constructor() : ManualUpdateChecker {

    override suspend fun checkForUpdate(
        activity: ComponentActivity
    ): UpdateCheckResult = withContext(Dispatchers.IO) {
        try {
            val latest = fetchLatestRelease()
            if (latest != null && isNewer(latest.tagName, BuildConfig.VERSION_NAME)) {
                UpdateCheckResult.UpdateAvailable(
                    version = latest.tagName,
                    useStoreNativeFlow = false,
                    onStartUpdate = { activity -> openUrl(activity, latest.htmlUrl) }
                )
            } else {
                UpdateCheckResult.UpToDate
            }
        } catch (e: Exception) {
            Log.w(TAG, "Update check failed", e)
            UpdateCheckResult.Error(e.message)
        }
    }

    private fun fetchLatestRelease(): GitHubRelease? {
        return try {
            val url = URL("$GITHUB_API/repos/$REPO_OWNER/$REPO_NAME/releases/latest")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("Accept", "application/vnd.github+json")
            connection.connectTimeout = 10_000
            connection.readTimeout = 10_000

            val response = BufferedReader(InputStreamReader(connection.inputStream)).readText()
            connection.disconnect()

            val json = JSONObject(response)
            GitHubRelease(
                tagName = json.getString("tag_name"),
                htmlUrl = json.getString("html_url")
            )
        } catch (e: Exception) {
            Log.w(TAG, "Failed to fetch release info", e)
            null
        }
    }

    private fun isNewer(remoteVersion: String, currentVersion: String): Boolean {
        val remote = parseVersion(remoteVersion)
        val current = parseVersion(currentVersion)
        if (remote.isEmpty() || current.isEmpty()) return false

        for (i in 0 until maxOf(remote.size, current.size)) {
            val r = remote.getOrElse(i) { 0 }
            val c = current.getOrElse(i) { 0 }
            if (r != c) return r > c
        }
        return false
    }

    private fun parseVersion(version: String): List<Int> {
        return version.trimStart('v', 'V').split('.').mapNotNull { it.toIntOrNull() }
    }

    private fun openUrl(activity: ComponentActivity, url: String) {
        val intent = Intent(Intent.ACTION_VIEW, url.toUri())
        try {
            activity.startActivity(intent)
        } catch (_: Exception) {
            // fallback: browser intent failed, nothing to do
        }
    }

    private data class GitHubRelease(val tagName: String, val htmlUrl: String)

    companion object {
        private const val GITHUB_API = "https://api.github.com"
        private const val REPO_OWNER = "ByteFlipper-58"
        private const val REPO_NAME = "book-story"
    }
}
