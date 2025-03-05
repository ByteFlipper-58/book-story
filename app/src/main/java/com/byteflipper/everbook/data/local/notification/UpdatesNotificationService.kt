/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.data.local.notification

import com.byteflipper.everbook.data.remote.dto.LatestReleaseInfo

interface UpdatesNotificationService {

    fun postNotification(releaseInfo: LatestReleaseInfo)

    companion object {
        const val CHANNEL_ID = "updates_channel"
    }
}