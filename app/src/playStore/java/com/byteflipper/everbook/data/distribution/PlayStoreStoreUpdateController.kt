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
import com.google.android.gms.tasks.Task
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.AppUpdateType.FLEXIBLE
import com.google.android.play.core.install.model.AppUpdateType.IMMEDIATE
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.review.ReviewManagerFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "PlayStoreUpdate"

@Singleton
class PlayStoreStoreUpdateController @Inject constructor(
    private val dataStoreRepository: DataStoreRepository,
    private val getReadingStatistics: GetReadingStatistics,
    @ApplicationScope private val applicationScope: CoroutineScope
) : StoreUpdateController {

    private var updateChecked = false
    private var appUpdateManager: AppUpdateManager? = null

    private val installListener = InstallStateUpdatedListener { state ->
        if (state.installStatus() == InstallStatus.DOWNLOADED) {
            Log.i(TAG, "FLEXIBLE update downloaded, completing install")
            appUpdateManager?.completeUpdate()
        }
    }

    override fun onActivityReady(activity: ComponentActivity) {
        if (updateChecked) return
        updateChecked = true

        val manager = AppUpdateManagerFactory.create(activity).also {
            appUpdateManager = it
        }

        manager.appUpdateInfo
            .addOnSuccessListener { info ->
                if (info.updateAvailability() != UpdateAvailability.UPDATE_AVAILABLE) {
                    return@addOnSuccessListener
                }

                val type = if (info.updatePriority() >= IMMEDIATE_PRIORITY_THRESHOLD) {
                    IMMEDIATE
                } else {
                    FLEXIBLE
                }

                if (type == FLEXIBLE) {
                    manager.registerListener(installListener)
                }

                val options = AppUpdateOptions.newBuilder(type).build()
                manager.startUpdateFlowForResult(
                    info, activity, options, UPDATE_REQUEST_CODE
                )
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
        val reviewManager = ReviewManagerFactory.create(activity)
        val request = reviewManager.requestReviewFlow()

        var reviewInfo: com.google.android.play.core.review.ReviewInfo? = null
        request.addOnCompleteListener { task: Task<com.google.android.play.core.review.ReviewInfo> ->
            if (task.isSuccessful) {
                reviewInfo = task.result
                reviewManager.launchReviewFlow(activity, reviewInfo!!)
                    .addOnFailureListener { error ->
                        Log.w(TAG, "launchReviewFlow failed", error)
                    }
            } else {
                Log.w(TAG, "requestReviewFlow failed", task.exception)
            }
        }
    }

    private companion object {
        const val UPDATE_REQUEST_CODE = 1_002
        const val IMMEDIATE_PRIORITY_THRESHOLD = 4
        const val REVIEW_MIN_READER_OPENS = 5
        const val REVIEW_MIN_READING_MS = 30L * 60L * 1000L

        val READER_OPENS_KEY = intPreferencesKey("play_store_reader_open_count")
        val REVIEW_REQUESTED_KEY = booleanPreferencesKey("play_store_review_requested")
    }
}
