/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.data.translation

import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import com.byteflipper.everbook.domain.repository.TranslationRepository
import com.byteflipper.everbook.domain.translation.TranslationCapability
import com.byteflipper.everbook.domain.translation.TranslationException
import com.byteflipper.everbook.domain.translation.TranslationRequest
import com.byteflipper.everbook.domain.translation.TranslationResult
import com.byteflipper.everbook.domain.translation.normalizeTranslationLanguageCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

private const val UNDETERMINED_LANGUAGE = "und"

@Singleton
class MlKitTranslationRepository @Inject constructor() : TranslationRepository {
    override val capability = TranslationCapability(
        inAppAvailable = true
    )

    private val languageIdentifier = LanguageIdentification.getClient()
    private val translatorMutex = Mutex()
    private val translators = mutableMapOf<Pair<String, String>, Translator>()

    override suspend fun translate(request: TranslationRequest): TranslationResult =
        withContext(Dispatchers.IO) {
            val source = resolveSourceLanguage(request)
            val target = request.targetLanguageCode.toMlKitCode()
                ?: throw TranslationException("Unsupported target language.")

            if (source == target) {
                return@withContext TranslationResult(
                    sourceLanguageCode = source,
                    targetLanguageCode = target,
                    translatedText = request.text
                )
            }

            val translator = getTranslator(source, target)
            val conditions = DownloadConditions.Builder().run {
                if (request.requireWifi) requireWifi()
                build()
            }

            translator.downloadModelIfNeeded(conditions).await()
            val translatedText = translator.translate(request.text).await()

            TranslationResult(
                sourceLanguageCode = source,
                targetLanguageCode = target,
                translatedText = translatedText
            )
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
}
