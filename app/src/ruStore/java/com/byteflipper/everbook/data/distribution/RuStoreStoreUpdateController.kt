/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.data.distribution

import android.util.Log
import androidx.activity.ComponentActivity
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import com.byteflipper.everbook.data.di.ApplicationScope
import com.byteflipper.everbook.domain.distribution.StoreUpdateController
import com.byteflipper.everbook.domain.repository.DataStoreRepository
import com.byteflipper.everbook.domain.use_case.statistics.GetReadingStatistics
import ru.rustore.sdk.appupdate.manager.RuStoreAppUpdateManager
import ru.rustore.sdk.appupdate.manager.factory.RuStoreAppUpdateManagerFactory
import ru.rustore.sdk.appupdate.model.AppUpdateOptions
import ru.rustore.sdk.appupdate.model.AppUpdateType
import ru.rustore.sdk.appupdate.model.InstallStatus
import ru.rustore.sdk.appupdate.model.UpdateAvailability
import ru.rustore.sdk.review.RuStoreReviewManager
import ru.rustore.sdk.review.RuStoreReviewManagerFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * RuStore In-App Update & Review.
 *
 * - Update: checks on activity start. High-priority updates use the IMMEDIATE (blocking) flow;
 *   everything else uses FLEXIBLE (background download, completed once downloaded).
 * - Review: requested once the user has opened the reader at least [REVIEW_MIN_READER_OPENS] times
 *   and accumulated at least [REVIEW_MIN_READING_MS] of total reading time. Requested only once.
 */
@Singleton
class RuStoreStoreUpdateController @Inject constructor(
    private val dataStoreRepository: DataStoreRepository,
    private val getReadingStatistics: GetReadingStatistics,
    @ApplicationScope private val applicationScope: CoroutineScope
) : StoreUpdateController {

    private var appUpdateManager: RuStoreAppUpdateManager? = null
    private var reviewManager: RuStoreReviewManager? = null
    private var updateChecked = false

    override fun onActivityReady(activity: ComponentActivity) {
        if (updateChecked) return
        updateChecked = true

        val manager = RuStoreAppUpdateManagerFactory.create(activity).also {
            appUpdateManager = it
        }

        manager.getAppUpdateInfo()
            .addOnSuccessListener { info ->
                if (info.updateAvailability != UpdateAvailability.UPDATE_AVAILABLE) return@addOnSuccessListener

                val immediate = info.updatePriority >= IMMEDIATE_PRIORITY_THRESHOLD
                if (immediate) {
                    manager.startUpdateFlow(
                        info,
                        AppUpdateOptions.Builder().appUpdateType(AppUpdateType.IMMEDIATE).build()
                    ).addOnFailureListener { error ->
                        Log.w(TAG, "Immediate update flow failed", error)
                    }
                } else {
                    manager.registerListener { state ->
                        if (state.installStatus == InstallStatus.DOWNLOADED) {
                            manager.completeUpdate(
                                AppUpdateOptions.Builder()
                                    .appUpdateType(AppUpdateType.FLEXIBLE)
                                    .build()
                            )
                        }
                    }
                    manager.startUpdateFlow(
                        info,
                        AppUpdateOptions.Builder().appUpdateType(AppUpdateType.FLEXIBLE).build()
                    ).addOnFailureListener { error ->
                        Log.w(TAG, "Flexible update flow failed", error)
                    }
                }
            }
            .addOnFailureListener { error ->
                Log.w(TAG, "getAppUpdateInfo failed", error)
            }
    }

    override fun onReaderOpened(activity: ComponentActivity) {
        applicationScope.launch {
            if (dataStoreRepository.getNullableDataFromDataStore(REVIEW_REQUESTED_KEY) == true) {
                return@launch
            }

            val opens = (dataStoreRepository.getNullableDataFromDataStore(READER_OPENS_KEY) ?: 0) + 1
            dataStoreRepository.putDataToDataStore(READER_OPENS_KEY, opens)
            if (opens < REVIEW_MIN_READER_OPENS) return@launch

            val totalReadingMs = runCatching {
                getReadingStatistics.execute().totalTimeMs
            }.getOrDefault(0L)
            if (totalReadingMs < REVIEW_MIN_READING_MS) return@launch

            dataStoreRepository.putDataToDataStore(REVIEW_REQUESTED_KEY, true)
            requestReview(activity)
        }
    }

    override val supportsInAppReview: Boolean = true

    override fun requestReview(activity: ComponentActivity) {
        val manager = reviewManager ?: RuStoreReviewManagerFactory.create(activity).also {
            reviewManager = it
        }

        manager.requestReviewFlow()
            .addOnSuccessListener { reviewInfo ->
                manager.launchReviewFlow(reviewInfo)
                    .addOnFailureListener { error ->
                        Log.w(TAG, "launchReviewFlow failed", error)
                    }
            }
            .addOnFailureListener { error ->
                Log.w(TAG, "requestReviewFlow failed", error)
            }
    }

    private companion object {
        const val TAG = "RuStoreUpdate"

        // RuStore priority is 0..5; treat the top tier as a blocking, immediate update.
        const val IMMEDIATE_PRIORITY_THRESHOLD = 4

        const val REVIEW_MIN_READER_OPENS = 5
        const val REVIEW_MIN_READING_MS = 30L * 60L * 1000L

        val READER_OPENS_KEY = intPreferencesKey("ru_store_reader_open_count")
        val REVIEW_REQUESTED_KEY = booleanPreferencesKey("ru_store_review_requested")
    }
}
