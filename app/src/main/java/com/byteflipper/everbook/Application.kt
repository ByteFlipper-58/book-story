/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook

import android.app.Application
import android.util.Log
import androidx.compose.foundation.ComposeFoundationFlags
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.byteflipper.everbook.data.work.AutoCategoryWorker
import com.byteflipper.everbook.data.work.UpdateCheckWorker
import com.byteflipper.everbook.domain.distribution.DistributionStartup
import com.byteflipper.everbook.domain.use_case.translation.ReconcileBookTranslations
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class Application : Application(), Configuration.Provider {
    @Inject
    lateinit var distributionStartup: DistributionStartup

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var reconcileBookTranslations: ReconcileBookTranslations

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate() {
        // Compose Foundation 1.9+ ignores LocalTextToolbar when the new context menu is on.
        // EverBook's reader toolbar (highlight, note, bookmark, translate, …) is built on
        // LocalTextToolbar — keep the legacy path until we migrate to appendTextContextMenuComponents.
        ComposeFoundationFlags.isNewContextMenuEnabled = false

        super.onCreate()

        distributionStartup.onAppCreate()

        // Recover translations whose worker died with a previous process (stuck RUNNING/QUEUED).
        applicationScope.launch {
            runCatching { reconcileBookTranslations.execute() }
                .onFailure { Log.e("BookTranslation", "Reconcile on startup failed", it) }
        }

        // Daily maintenance: demote stale "Reading" books (untouched 7+ days) to "Dropped".
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            AutoCategoryWorker.UNIQUE_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<AutoCategoryWorker>(1, TimeUnit.DAYS).build()
        )

        // Daily background check for a new app version; posts a localized notification if found.
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            UpdateCheckWorker.UNIQUE_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<UpdateCheckWorker>(1, TimeUnit.DAYS)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()
        )
    }
}






