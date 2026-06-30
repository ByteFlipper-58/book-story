/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.data.distribution

import android.util.Log
import com.byteflipper.everbook.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject

private const val TAG = "GitHubReleaseSource"

/**
 * Shared GitHub Releases access for the everbook flavor. Used both by the manual update check
 * (About screen) and the background update-notification check, so the fetch + semantic-version
 * comparison live in one place.
 */
class GitHubReleaseSource @Inject constructor() {

    data class GitHubRelease(val tagName: String, val htmlUrl: String)

    /** Fetches the latest published release, or `null` on any network/parse error. */
    suspend fun fetchLatestRelease(): GitHubRelease? = withContext(Dispatchers.IO) {
        try {
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

    /** Whether [remoteVersion] is a newer semantic version than the installed build. */
    fun isNewerThanCurrent(remoteVersion: String): Boolean =
        isNewer(remoteVersion, BuildConfig.VERSION_NAME)

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

    companion object {
        private const val GITHUB_API = "https://api.github.com"
        private const val REPO_OWNER = "ByteFlipper-58"
        private const val REPO_NAME = "book-story"
    }
}
