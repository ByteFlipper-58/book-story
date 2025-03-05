/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.data.remote

import retrofit2.http.GET
import com.byteflipper.everbook.data.remote.dto.LatestReleaseInfo

interface GithubAPI {
    @GET("repos/ByteFlipper-58/book-story/releases/latest")
    suspend fun getLatestRelease(): LatestReleaseInfo

    companion object {
        const val BASE_URL = "https://api.github.com/"
    }
}