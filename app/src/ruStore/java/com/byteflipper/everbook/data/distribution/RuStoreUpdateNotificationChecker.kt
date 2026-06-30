/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.data.distribution

import android.content.Context
import com.byteflipper.everbook.BuildConfig
import com.byteflipper.everbook.domain.distribution.AvailableUpdate
import com.byteflipper.everbook.domain.distribution.UpdateNotificationChecker
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import ru.rustore.sdk.appupdate.manager.factory.RuStoreAppUpdateManagerFactory
import ru.rustore.sdk.appupdate.model.UpdateAvailability
import javax.inject.Inject
import kotlin.coroutines.resume

/**
 * Background update check via the RuStore In-App Update SDK (no Activity required for the query).
 * Tapping the notification opens the RuStore app listing.
 */
class RuStoreUpdateNotificationChecker @Inject constructor(
    @ApplicationContext private val context: Context
) : UpdateNotificationChecker {

    override suspend fun check(): AvailableUpdate? {
        val manager = RuStoreAppUpdateManagerFactory.create(context)

        val info = suspendCancellableCoroutine { cont ->
            manager.getAppUpdateInfo()
                .addOnSuccessListener { cont.resume(it) }
                .addOnFailureListener { cont.resume(null) }
        }

        if (info == null || info.updateAvailability != UpdateAvailability.UPDATE_AVAILABLE) {
            return null
        }

        return AvailableUpdate(
            versionLabel = info.availableVersionName ?: "",
            uri = "https://apps.rustore.ru/app/${BuildConfig.APPLICATION_ID}"
        )
    }
}
