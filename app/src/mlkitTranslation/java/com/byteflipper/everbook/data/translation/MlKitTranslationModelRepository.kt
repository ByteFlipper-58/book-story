/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.data.translation

import android.content.Context
import android.app.DownloadManager
import android.util.Log
import com.google.mlkit.common.MlKitException
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.TranslateRemoteModel
import com.byteflipper.everbook.R
import com.byteflipper.everbook.domain.repository.TranslationModelRepository
import com.byteflipper.everbook.domain.translation.TranslationException
import com.byteflipper.everbook.domain.translation.TranslationLanguage
import com.byteflipper.everbook.domain.translation.TranslationModelState
import com.byteflipper.everbook.domain.translation.nativeTranslationLanguageName
import com.byteflipper.everbook.domain.translation.normalizeTranslationLanguageCode
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import dagger.hilt.android.qualifiers.ApplicationContext

private const val TRANSLATION_MODELS_LOG = "TranslationModels"
private const val MODEL_DOWNLOAD_TIMEOUT_MS = 5 * 60 * 1000L
private const val MODEL_DOWNLOAD_STALL_TIMEOUT_MS = 45 * 1000L
private const val MODEL_DOWNLOAD_POLL_DELAY_MS = 1_000L
private const val MODEL_DOWNLOAD_VERIFY_ATTEMPTS = 15
private const val MODEL_DOWNLOAD_VERIFY_DELAY_MS = 1_000L

@Singleton
class MlKitTranslationModelRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val notificationController: TranslationModelNotificationController
) : TranslationModelRepository {
    override val available = true

    private val modelManager = RemoteModelManager.getInstance()
    private val downloadScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun getAvailableModels(): List<TranslationModelState> =
        TranslateLanguage.getAllLanguages().mapNotNull { mlKitCode ->
            val languageCode = normalizeTranslationLanguageCode(mlKitCode)
                ?: return@mapNotNull null
            TranslationModelState(
                language = TranslationLanguage(
                    code = languageCode,
                    name = nativeTranslationLanguageName(languageCode)
                ),
                downloaded = false,
                supported = true
            )
        }.distinctBy { it.language.code }
            .sortedBy { it.language.name.lowercase(Locale.ROOT) }

    override suspend fun getModels(): List<TranslationModelState> =
        withContext(Dispatchers.IO) {
            Log.i(TRANSLATION_MODELS_LOG, "ML Kit get downloaded models started")
            val downloadedLanguages = modelManager
                .getDownloadedModels(TranslateRemoteModel::class.java)
                .await()
                .mapNotNull { normalizeTranslationLanguageCode(it.language) }
                .toSet()
            val models = getAvailableModels().map {
                it.copy(
                    downloaded = it.language.code in downloadedLanguages,
                )
            }
            Log.i(
                TRANSLATION_MODELS_LOG,
                "ML Kit get downloaded models finished: available=${models.size} " +
                        "downloaded=${downloadedLanguages.size}"
            )
            models
        }

    override suspend fun downloadModel(languageCode: String, requireWifi: Boolean) {
        val normalizedLanguageCode = normalizeTranslationLanguageCode(languageCode)
            ?: throw unsupportedLanguageException()
        val download = downloadScope.async {
            performModelDownload(normalizedLanguageCode, requireWifi)
        }

        // RemoteModelManager is thread-safe and returns the current Task when this model is already
        // downloading. Keep only the coroutine application-scoped; an extra Deferred cache can retain
        // a stale first attempt and make a later tap appear to be randomly ignored.
        download.await()
    }

    private suspend fun performModelDownload(languageCode: String, requireWifi: Boolean) {
        val model = languageCode.toTranslateRemoteModel()
        ensureDownloadNetworkAvailable(languageCode, requireWifi)
        val conditions = DownloadConditions.Builder().run {
            if (requireWifi) requireWifi()
            build()
        }

        try {
            val startedAt = System.currentTimeMillis()
            Log.i(
                TRANSLATION_MODELS_LOG,
                "ML Kit model download started: language=$languageCode wifiOnly=$requireWifi"
            )
            notificationController.showDownload(languageCode)
            var lastStall: ModelDownloadStalledException? = null
            var completed = false
            for (attempt in 0..1) {
                val downloadTask = modelManager.download(model, conditions)
                try {
                    awaitModelDownload(downloadTask, languageCode)
                    completed = true
                    break
                } catch (stall: ModelDownloadStalledException) {
                    lastStall = stall
                    if (attempt == 0) {
                        Log.w(
                            TRANSLATION_MODELS_LOG,
                            "ML Kit download stalled; cancelled system request and retrying once: " +
                                "language=$languageCode"
                        )
                        delay(MODEL_DOWNLOAD_POLL_DELAY_MS)
                    }
                }
            }
            check(completed || lastStall != null)
            lastStall?.let { throw it }
            verifyModelDownloaded(model, languageCode)
            Log.i(
                TRANSLATION_MODELS_LOG,
                "ML Kit model download finished: language=$languageCode " +
                    "elapsedMs=${System.currentTimeMillis() - startedAt}"
            )
        } catch (exception: ModelDownloadStalledException) {
            throw TranslationException(
                context.getString(R.string.translation_model_error_stalled),
                exception
            )
        } catch (exception: TimeoutCancellationException) {
            throw TranslationException(
                context.getString(R.string.translation_model_error_timeout),
                exception
            )
        } catch (exception: CancellationException) {
            Log.i(TRANSLATION_MODELS_LOG, "ML Kit model download cancelled: language=$languageCode")
            throw exception
        } catch (exception: MlKitException) {
            Log.e(
                TRANSLATION_MODELS_LOG,
                "ML Kit model download failed: language=$languageCode code=${exception.errorCode}",
                exception
            )
            throw TranslationException(
                "ML Kit model download failed (${exception.errorCode}): " +
                    (exception.message ?: "Unknown ML Kit error."),
                exception
            )
        } catch (throwable: Throwable) {
            Log.e(TRANSLATION_MODELS_LOG, "ML Kit model download failed: language=$languageCode", throwable)
            throw throwable
        } finally {
            notificationController.clear(languageCode)
        }
    }

    private suspend fun awaitModelDownload(
        downloadTask: com.google.android.gms.tasks.Task<Void>,
        languageCode: String
    ) {
        val startedAt = System.currentTimeMillis()
        var sawTransferredBytes = false
        withTimeout(MODEL_DOWNLOAD_TIMEOUT_MS) {
            while (!downloadTask.isComplete) {
                val activeDownloads = findTranslationDownloads(languageCode)
                if (activeDownloads.any { it.bytesDownloaded > 0L }) {
                    sawTransferredBytes = true
                }
                if (!sawTransferredBytes &&
                    System.currentTimeMillis() - startedAt >= MODEL_DOWNLOAD_STALL_TIMEOUT_MS
                ) {
                    val cancelledIds = activeDownloads.map { it.id }.toLongArray()
                    if (cancelledIds.isNotEmpty()) {
                        downloadManager.remove(*cancelledIds)
                    }
                    Log.e(
                        TRANSLATION_MODELS_LOG,
                        "ML Kit download made no progress: language=$languageCode " +
                            "downloadIds=${cancelledIds.joinToString()}"
                    )
                    throw ModelDownloadStalledException()
                }
                delay(MODEL_DOWNLOAD_POLL_DELAY_MS)
            }
            downloadTask.await()
        }
    }

    private val downloadManager: DownloadManager
        get() = context.getSystemService(DownloadManager::class.java)

    private fun findTranslationDownloads(languageCode: String): List<SystemDownload> {
        val result = mutableListOf<SystemDownload>()
        val cursor = downloadManager.query(DownloadManager.Query()) ?: return result
        cursor.use {
            val idColumn = it.getColumnIndexOrThrow(DownloadManager.COLUMN_ID)
            val uriColumn = it.getColumnIndexOrThrow(DownloadManager.COLUMN_URI)
            val bytesColumn = it.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
            while (it.moveToNext()) {
                val uri = it.getString(uriColumn) ?: continue
                if (uri.endsWith("_${languageCode}.zip")) {
                    result += SystemDownload(it.getLong(idColumn), it.getLong(bytesColumn))
                }
            }
        }
        return result
    }

    private data class SystemDownload(val id: Long, val bytesDownloaded: Long)

    private class ModelDownloadStalledException : Exception()

    override suspend fun deleteModel(languageCode: String) {
        val model = languageCode.toTranslateRemoteModel()

        try {
            Log.i(TRANSLATION_MODELS_LOG, "ML Kit model delete started: language=$languageCode")
            notificationController.showDelete(languageCode)
            withContext(Dispatchers.IO) {
                modelManager.deleteDownloadedModel(model).await()
            }
            Log.i(TRANSLATION_MODELS_LOG, "ML Kit model delete finished: language=$languageCode")
        } catch (throwable: Throwable) {
            Log.e(TRANSLATION_MODELS_LOG, "ML Kit model delete failed: language=$languageCode", throwable)
            throw throwable
        } finally {
            notificationController.clear(languageCode)
        }
    }

    private fun String.toTranslateRemoteModel(): TranslateRemoteModel =
        TranslateRemoteModel.Builder(toMlKitCode() ?: throw unsupportedLanguageException())
            .build()

    private suspend fun verifyModelDownloaded(
        model: TranslateRemoteModel,
        languageCode: String
    ) {
        repeat(MODEL_DOWNLOAD_VERIFY_ATTEMPTS) { attempt ->
            if (modelManager.isModelDownloaded(model).await()) {
                Log.i(
                    TRANSLATION_MODELS_LOG,
                    "ML Kit model download verification: language=$languageCode " +
                            "attempt=${attempt + 1} downloaded=true"
                )
                return
            }

            val downloadedLanguages = modelManager
                .getDownloadedModels(TranslateRemoteModel::class.java)
                .await()
                .mapNotNull { normalizeTranslationLanguageCode(it.language) }
                .toSet()
            if (languageCode in downloadedLanguages) {
                Log.i(
                    TRANSLATION_MODELS_LOG,
                    "ML Kit model download verification: language=$languageCode " +
                            "attempt=${attempt + 1} downloaded=true via model list"
                )
                return
            }

            Log.i(
                TRANSLATION_MODELS_LOG,
                "ML Kit model download verification: language=$languageCode " +
                        "attempt=${attempt + 1} downloaded=false"
            )
            delay(MODEL_DOWNLOAD_VERIFY_DELAY_MS)
        }

        val downloadedLanguages = modelManager
            .getDownloadedModels(TranslateRemoteModel::class.java)
            .await()
            .mapNotNull { normalizeTranslationLanguageCode(it.language) }
            .sorted()
            .joinToString()
        Log.e(
            TRANSLATION_MODELS_LOG,
            "ML Kit model download verification failed: language=$languageCode " +
                    "downloadedLanguages=[$downloadedLanguages]"
        )
        throw TranslationException(
            "ML Kit reported the download as finished, but the model is not available. " +
                    "Check Google Play services and free storage, then try again."
        )
    }

    private fun ensureDownloadNetworkAvailable(languageCode: String, requireWifi: Boolean) {
        if (!requireWifi) return
        if (context.isConnectedToValidatedWifi()) return

        Log.w(
            TRANSLATION_MODELS_LOG,
            "ML Kit model download blocked: language=$languageCode wifiOnly=true wifiConnected=false"
        )
        throw TranslationException(
            "Connect to Wi-Fi or turn off Wi-Fi only model downloads, then try again."
        )
    }

    private fun String?.toMlKitCode(): String? =
        normalizeTranslationLanguageCode(this)
            ?.let { TranslateLanguage.fromLanguageTag(it) }

    private fun unsupportedLanguageException(): TranslationException =
        TranslationException("Unsupported translation language.")

}
