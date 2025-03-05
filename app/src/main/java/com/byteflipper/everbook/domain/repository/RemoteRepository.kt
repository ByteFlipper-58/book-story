/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.domain.repository

import com.byteflipper.everbook.data.remote.dto.LatestReleaseInfo

interface RemoteRepository {

    suspend fun checkForUpdates(
        postNotification: Boolean
    ): LatestReleaseInfo?
}