/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.data.translation

import android.util.Log
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import com.byteflipper.everbook.domain.repository.TranslationRepository
import com.byteflipper.everbook.domain.translation.TranslationCapability
import com.byteflipper.everbook.domain.translation.TranslationException
import com.byteflipper.everbook.domain.translation.TranslationProviderMode
import com.byteflipper.everbook.domain.translation.TranslationRequest
import com.byteflipper.everbook.domain.translation.TranslationResult
import com.byteflipper.everbook.domain.translation.normalizeTranslationLanguageCode
import com.byteflipper.everbook.domain.translation.resolveTranslationLanguageCode
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import javax.inject.Inject
import javax.inject.Singleton

private const val UNDETERMINED_LANGUAGE = "und"
private const val BOOK_TRANSLATION_LOG = "BookTranslation"
private const val MODEL_DOWNLOAD_TIMEOUT_MS = 5 * 60 * 1000L

@Singleton
class MlKitTranslationRepository @Inject constructor(
    private val googleTranslateWebClient: GoogleTranslateWebClient,
    private val notificationController: TranslationModelNotificationController
) : TranslationRepository {
    override val capability = TranslationCapability(
        inAppAvailable = true,
        googleTranslateAvailable = true
    )

    private val languageIdentifier = LanguageIdentification.getClient()
    private val translatorMutex = Mutex()
    private val preparedTranslatorKeys = mutableSetOf<Pair<String, String>>()

    // Bounded LRU cache: ML Kit Translator holds a native model handle and is Closeable. Caching
    // unbounded (one per language pair, forever) leaks native memory over a long session, so the
    // least-recently-used translator is closed once the cache exceeds MAX_CACHED_TRANSLATORS.
    private val translators =
        object : LinkedHashMap<Pair<String, String>, Translator>(8, 0.75f, true) {
            override fun removeEldestEntry(
                eldest: MutableMap.MutableEntry<Pair<String, String>, Translator>
            ): Boolean {
                if (size <= MAX_CACHED_TRANSLATORS) return false
                eldest.value.close()
                // Drop the prepared flag too so a future re-created instance re-verifies the model
                // (a no-op when it's already on disk).
                preparedTranslatorKeys -= eldest.key
                return true
            }
        }

    override suspend fun translate(request: TranslationRequest): TranslationResult =
        when (request.providerMode) {
            TranslationProviderMode.IN_APP -> translateWithMlKit(request)
            TranslationProviderMode.GOOGLE_TRANSLATE -> googleTranslateWebClient.translate(request)
            TranslationProviderMode.EXTERNAL ->
                throw TranslationException("External translation uses installed apps.")
        }

    private suspend fun translateWithMlKit(request: TranslationRequest): TranslationResult =
        withContext(Dispatchers.IO) {
            val source = resolveSourceLanguage(request)
            val target = resolveTranslationLanguageCode(request.targetLanguageCode).toMlKitCode()
                ?: throw TranslationException("Unsupported target language.")
            if (Log.isLoggable(BOOK_TRANSLATION_LOG, Log.VERBOSE)) {
                Log.v(
                    BOOK_TRANSLATION_LOG,
                    "ML Kit translate requested: source=$source target=$target " +
                            "wifiOnly=${request.requireWifi}"
                )
            }

            if (source == target) {
                if (Log.isLoggable(BOOK_TRANSLATION_LOG, Log.VERBOSE)) {
                    Log.v(BOOK_TRANSLATION_LOG, "ML Kit translate skipped: source equals target=$target")
                }
                return@withContext TranslationResult(
                    sourceLanguageCode = source,
                    targetLanguageCode = target,
                    translatedText = request.text
                )
            }

            val translator = getTranslator(source, target)
            prepareTranslator(
                source = source,
                target = target,
                translator = translator,
                requireWifi = request.requireWifi
            )
            val translatedText = translator.translate(request.text).await()
            if (Log.isLoggable(BOOK_TRANSLATION_LOG, Log.VERBOSE)) {
                Log.v(BOOK_TRANSLATION_LOG, "ML Kit translate finished: source=$source target=$target")
            }

            TranslationResult(
                sourceLanguageCode = source,
                targetLanguageCode = target,
                translatedText = translatedText
            )
        }

    private suspend fun prepareTranslator(
        source: String,
        target: String,
        translator: Translator,
        requireWifi: Boolean
    ) {
        val key = source to target
        val alreadyPrepared = translatorMutex.withLock {
            key in preparedTranslatorKeys
        }
        if (alreadyPrepared) {
            if (Log.isLoggable(BOOK_TRANSLATION_LOG, Log.VERBOSE)) {
                Log.v(
                    BOOK_TRANSLATION_LOG,
                    "ML Kit downloadModelIfNeeded skipped: source=$source target=$target prepared=true"
                )
            }
            return
        }

        Log.i(
            BOOK_TRANSLATION_LOG,
            "ML Kit downloadModelIfNeeded started: source=$source target=$target"
        )
        val downloadStartedAt = System.currentTimeMillis()
        try {
            val conditions = DownloadConditions.Builder().run {
                if (requireWifi) requireWifi()
                build()
            }
            notificationController.showDownload(target)
            withTimeout(MODEL_DOWNLOAD_TIMEOUT_MS) {
                translator.downloadModelIfNeeded(conditions).await()
            }
            translatorMutex.withLock {
                preparedTranslatorKeys += key
            }
            Log.i(
                BOOK_TRANSLATION_LOG,
                "ML Kit downloadModelIfNeeded finished: source=$source target=$target " +
                        "elapsedMs=${System.currentTimeMillis() - downloadStartedAt}"
            )
        } catch (exception: TimeoutCancellationException) {
            Log.e(
                BOOK_TRANSLATION_LOG,
                "ML Kit downloadModelIfNeeded timed out: source=$source target=$target " +
                        "timeoutMs=$MODEL_DOWNLOAD_TIMEOUT_MS",
                exception
            )
            throw TranslationException(
                "ML Kit model download is taking too long. Check Google Play services and your network, then try again.",
                exception
            )
        } catch (exception: CancellationException) {
            Log.i(
                BOOK_TRANSLATION_LOG,
                "ML Kit downloadModelIfNeeded cancelled: source=$source target=$target"
            )
            throw exception
        } finally {
            notificationController.clear(target)
        }
    }

    private suspend fun resolveSourceLanguage(request: TranslationRequest): String {
        val explicitSource = request.sourceLanguageCode.toMlKitCode()
        if (explicitSource != null) return explicitSource

        return identifyLanguage(request.text)
            ?: throw TranslationException("Could not detect the source language.")
    }

    private suspend fun identifyLanguage(text: String): String? =
        withContext(Dispatchers.IO) {
            val identified = languageIdentifier.identifyLanguage(text).await()
            normalizeTranslationLanguageCode(identified).takeIf {
                it != null && it != UNDETERMINED_LANGUAGE && it.toMlKitCode() != null
            }
        }

    private suspend fun getTranslator(source: String, target: String): Translator =
        translatorMutex.withLock {
            val key = source to target
            translators.getOrPut(key) {
                val options = TranslatorOptions.Builder()
                    .setSourceLanguage(source)
                    .setTargetLanguage(target)
                    .build()

                Translation.getClient(options)
            }
        }

    private fun String?.toMlKitCode(): String? =
        normalizeTranslationLanguageCode(this)
            ?.let { TranslateLanguage.fromLanguageTag(it) }

    private companion object {
        const val MAX_CACHED_TRANSLATORS = 6
    }
}
