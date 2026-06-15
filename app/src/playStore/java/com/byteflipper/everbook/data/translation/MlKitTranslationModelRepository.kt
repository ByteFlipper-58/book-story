/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.data.translation

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
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
private const val MODEL_DOWNLOAD_VERIFY_ATTEMPTS = 15
private const val MODEL_DOWNLOAD_VERIFY_DELAY_MS = 1_000L

@Singleton
class MlKitTranslationModelRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val notificationController: TranslationModelNotificationController
) : TranslationModelRepository {
    override val available = true

    private val modelManager = RemoteModelManager.getInstance()

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
            withContext(Dispatchers.IO) {
                val downloadTask = modelManager.download(model, conditions)
                try {
                    withTimeout(MODEL_DOWNLOAD_TIMEOUT_MS) {
                        downloadTask.await()
                    }
                } catch (timeout: TimeoutCancellationException) {
                    Log.e(
                        TRANSLATION_MODELS_LOG,
                        "ML Kit model download timed out: language=$languageCode " +
                                "timeoutMs=$MODEL_DOWNLOAD_TIMEOUT_MS " +
                                "taskComplete=${downloadTask.isComplete} " +
                                "taskCancelled=${downloadTask.isCanceled} " +
                                "taskException=${downloadTask.exception?.message}"
                    )
                    throw timeout
                }
                verifyModelDownloaded(model, languageCode)
            }
            Log.i(
                TRANSLATION_MODELS_LOG,
                "ML Kit model download finished: language=$languageCode " +
                        "elapsedMs=${System.currentTimeMillis() - startedAt}"
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
        if (context.isWifiConnected()) return

        Log.w(
            TRANSLATION_MODELS_LOG,
            "ML Kit model download blocked: language=$languageCode wifiOnly=true wifiConnected=false"
        )
        throw TranslationException(
            "Connect to Wi-Fi or turn off Wi-Fi only model downloads, then try again."
        )
    }

    private fun Context.isWifiConnected(): Boolean {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return connectivityManager.allNetworks.any { network ->
                val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return@any false
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) &&
                    capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            }
        }

        @Suppress("DEPRECATION")
        return connectivityManager.allNetworks.any { network ->
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.getNetworkInfo(network) ?: return@any false
            @Suppress("DEPRECATION")
            networkInfo.isConnected && networkInfo.type == ConnectivityManager.TYPE_WIFI
        }
    }

    private fun String?.toMlKitCode(): String? =
        normalizeTranslationLanguageCode(this)
            ?.let { TranslateLanguage.fromLanguageTag(it) }

    private fun unsupportedLanguageException(): TranslationException =
        TranslationException("Unsupported translation language.")

}
