/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.data.translation

import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.TranslateRemoteModel
import com.byteflipper.everbook.domain.repository.TranslationModelRepository
import com.byteflipper.everbook.domain.translation.TranslationException
import com.byteflipper.everbook.domain.translation.TranslationLanguage
import com.byteflipper.everbook.domain.translation.TranslationModelState
import com.byteflipper.everbook.domain.translation.nativeTranslationLanguageName
import com.byteflipper.everbook.domain.translation.normalizeTranslationLanguageCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MlKitTranslationModelRepository @Inject constructor() : TranslationModelRepository {
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
            val downloadedLanguages = modelManager
                .getDownloadedModels(TranslateRemoteModel::class.java)
                .await()
                .mapNotNull { normalizeTranslationLanguageCode(it.language) }
                .toSet()
            getAvailableModels().map {
                it.copy(
                    downloaded = it.language.code in downloadedLanguages,
                )
            }
        }

    override suspend fun downloadModel(languageCode: String, requireWifi: Boolean) {
        val model = languageCode.toTranslateRemoteModel()
        val conditions = DownloadConditions.Builder().run {
            if (requireWifi) requireWifi()
            build()
        }

        withContext(Dispatchers.IO) {
            modelManager.download(model, conditions).await()
        }
    }

    override suspend fun deleteModel(languageCode: String) {
        val model = languageCode.toTranslateRemoteModel()

        withContext(Dispatchers.IO) {
            modelManager.deleteDownloadedModel(model).await()
        }
    }

    private fun String.toTranslateRemoteModel(): TranslateRemoteModel =
        TranslateRemoteModel.Builder(toMlKitCode() ?: throw unsupportedLanguageException())
            .build()

    private fun String?.toMlKitCode(): String? =
        normalizeTranslationLanguageCode(this)
            ?.let { TranslateLanguage.fromLanguageTag(it) }

    private fun unsupportedLanguageException(): TranslationException =
        TranslationException("Unsupported translation language.")

}
