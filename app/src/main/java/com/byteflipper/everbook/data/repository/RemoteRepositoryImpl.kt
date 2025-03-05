/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.data.repository

import android.app.Application
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.byteflipper.everbook.R
import com.byteflipper.everbook.data.local.notification.UpdatesNotificationService
import com.byteflipper.everbook.data.remote.GithubAPI
import com.byteflipper.everbook.data.remote.dto.LatestReleaseInfo
import com.byteflipper.everbook.domain.repository.RemoteRepository
import javax.inject.Inject
import javax.inject.Singleton

private const val CHECK_FOR_UPDATES = "CHECK FOR UPD, REPO"

/**
 * Remote repository.
 * Manages all API(online) related work.
 */
@Singleton
class RemoteRepositoryImpl @Inject constructor(
    private val application: Application,

    private val githubAPI: GithubAPI,
    private val updatesNotificationService: UpdatesNotificationService,
) : RemoteRepository {

    /**
     * Check for updates from GitHub.
     *
     * @param postNotification Whether notification should be send.
     */
    override suspend fun checkForUpdates(postNotification: Boolean): LatestReleaseInfo? {
        Log.i(CHECK_FOR_UPDATES, "Checking for updates. Post notification: $postNotification")

        return withContext(Dispatchers.IO) {
            try {
                val result = githubAPI.getLatestRelease()

                val version = result.tagName.substringAfter("v")
                val currentVersion = application.getString(R.string.app_version)

                if (version != currentVersion && postNotification) {
                    Log.i(CHECK_FOR_UPDATES, "Posting notification.")
                    updatesNotificationService.postNotification(
                        result
                    )
                }

                result
            } catch (e: Exception) {
                Log.e(CHECK_FOR_UPDATES, "Could not get latest release information.")
                e.printStackTrace()
                null
            }
        }
    }
}