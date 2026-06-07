/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.data.translation

import com.byteflipper.everbook.domain.translation.AUTO_TRANSLATION_LANGUAGE
import com.byteflipper.everbook.domain.translation.TranslationException
import com.byteflipper.everbook.domain.translation.TranslationRequest
import com.byteflipper.everbook.domain.translation.TranslationResult
import com.byteflipper.everbook.domain.translation.normalizeTranslationLanguageCode
import com.google.gson.JsonArray
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.OutputStreamWriter
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URI
import java.net.URLEncoder
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

private const val GOOGLE_TRANSLATE_ENDPOINT =
    "https://translate.googleapis.com/translate_a/single"
private const val GOOGLE_TRANSLATE_TIMEOUT_MS = 15_000
private val GOOGLE_TRANSLATE_USER_AGENTS = listOf(
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
            "(KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36",
    "Mozilla/5.0 (Macintosh; Intel Mac OS X 14_5) AppleWebKit/537.36 " +
            "(KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36",
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:127.0) Gecko/20100101 Firefox/127.0",
    "Mozilla/5.0 (Macintosh; Intel Mac OS X 14.5; rv:127.0) Gecko/20100101 Firefox/127.0",
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
            "(KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36 Edg/126.0.0.0"
)

@Singleton
class GoogleTranslateWebClient @Inject constructor() {
    suspend fun translate(request: TranslationRequest): TranslationResult =
        withContext(Dispatchers.IO) {
            val source = request.sourceLanguageCode
                ?.let(::normalizeTranslationLanguageCode)
                ?: AUTO_TRANSLATION_LANGUAGE
            val target = normalizeTranslationLanguageCode(request.targetLanguageCode)
                ?: throw TranslationException("Unsupported target language.")

            val connection = openConnection(
                sourceLanguageCode = source,
                targetLanguageCode = target,
                text = request.text
            )

            try {
                val responseCode = connection.responseCode
                val responseBody = connection.readBody(responseCode)
                if (responseCode !in 200..299) {
                    throw TranslationException("Google Translate request failed.")
                }

                parseTranslationResponse(
                    responseBody = responseBody,
                    fallbackSourceLanguageCode = source,
                    targetLanguageCode = target
                )
            } catch (exception: TranslationException) {
                throw exception
            } catch (exception: Exception) {
                throw TranslationException("Could not translate text.", exception)
            } finally {
                connection.disconnect()
            }
        }

    private fun openConnection(
        sourceLanguageCode: String,
        targetLanguageCode: String,
        text: String
    ): HttpURLConnection {
        val formBody = listOf(
            "client=gtx",
            "sl=${sourceLanguageCode.urlEncode()}",
            "tl=${targetLanguageCode.urlEncode()}",
            "dt=t",
            "q=${text.urlEncode()}"
        ).joinToString("&")
        val url = URI(GOOGLE_TRANSLATE_ENDPOINT).toURL()

        return (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = GOOGLE_TRANSLATE_TIMEOUT_MS
            readTimeout = GOOGLE_TRANSLATE_TIMEOUT_MS
            doOutput = true
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
            setRequestProperty("User-Agent", GOOGLE_TRANSLATE_USER_AGENTS.randomItem())
            OutputStreamWriter(outputStream, Charsets.UTF_8).use { it.write(formBody) }
        }
    }

    private fun HttpURLConnection.readBody(responseCode: Int): String {
        val stream = if (responseCode in 200..299) inputStream else errorStream
        return stream?.use { input ->
            BufferedReader(InputStreamReader(input)).use { it.readText() }
        }.orEmpty()
    }

    private fun parseTranslationResponse(
        responseBody: String,
        fallbackSourceLanguageCode: String,
        targetLanguageCode: String
    ): TranslationResult {
        val root = JsonParser.parseString(responseBody).asJsonArray
        val translatedText = root.getOrNull(0)
            ?.asJsonArrayOrNull()
            ?.mapNotNull { segment ->
                segment.asJsonArrayOrNull()?.getOrNull(0)?.asStringOrNull()
            }
            ?.joinToString("")
            ?.takeIf { it.isNotBlank() }
            ?: throw TranslationException("Google Translate returned an empty translation.")

        val detectedSource = root.getOrNull(2)
            ?.asStringOrNull()
            ?.let(::normalizeTranslationLanguageCode)
            ?: fallbackSourceLanguageCode

        return TranslationResult(
            sourceLanguageCode = detectedSource,
            targetLanguageCode = targetLanguageCode,
            translatedText = translatedText
        )
    }

    private fun JsonArray.getOrNull(index: Int) =
        takeIf { index in 0 until size() }?.get(index)

    private fun com.google.gson.JsonElement.asJsonArrayOrNull(): JsonArray? =
        takeIf { it.isJsonArray }?.asJsonArray

    private fun com.google.gson.JsonElement.asStringOrNull(): String? =
        runCatching {
            takeIf { it.isJsonPrimitive }?.asString
        }.getOrNull()

    private fun String.urlEncode(): String =
        URLEncoder.encode(this, Charsets.UTF_8.name())

    private fun List<String>.randomItem(): String =
        this[Random.nextInt(size)]
}
