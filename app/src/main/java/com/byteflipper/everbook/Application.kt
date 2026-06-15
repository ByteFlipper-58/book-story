/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook

import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.byteflipper.everbook.domain.distribution.DistributionStartup
import com.byteflipper.everbook.domain.use_case.translation.ReconcileBookTranslations
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
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

    override fun onCreate() {
        super.onCreate()

        distributionStartup.onAppCreate()

        // Recover translations whose worker died with a previous process (stuck RUNNING/QUEUED).
        applicationScope.launch {
            runCatching { reconcileBookTranslations.execute() }
                .onFailure { Log.e("BookTranslation", "Reconcile on startup failed", it) }
        }
    }
}






