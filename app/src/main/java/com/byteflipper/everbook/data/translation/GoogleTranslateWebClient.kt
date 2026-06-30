/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.data.translation

import android.util.Log
import com.byteflipper.everbook.domain.translation.AUTO_TRANSLATION_LANGUAGE
import com.byteflipper.everbook.domain.translation.TranslationException
import com.byteflipper.everbook.domain.translation.TranslationRateLimitedException
import com.byteflipper.everbook.domain.translation.TranslationRequest
import com.byteflipper.everbook.domain.translation.TranslationResult
import com.byteflipper.everbook.domain.translation.normalizeTranslationLanguageCode
import com.byteflipper.everbook.domain.translation.resolveTranslationLanguageCode
import com.google.gson.JsonArray
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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
private const val BOOK_TRANSLATION_LOG = "BookTranslation"

// Adaptive pacing (AIMD): the unofficial endpoint rate-limits primarily by request rate, so a
// single shared pacer keeps all Google requests spaced. On every success the spacing shrinks a
// little (additive), on every 429/503 it doubles (multiplicative) — converging on a sustainable
// rate without any masking.
private const val PACER_INITIAL_DELAY_MS = 1_200L
private const val PACER_MIN_DELAY_MS = 500L
private const val PACER_MAX_DELAY_MS = 30_000L
private const val PACER_SUCCESS_STEP_MS = 120L
private const val PACER_MAX_RETRY_AFTER_MS = 120_000L
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
    private val pacerMutex = Mutex()
    private var nextAllowedAtMs = 0L
    private var currentDelayMs = PACER_INITIAL_DELAY_MS

    /** Reserve the next request slot and wait for it, so concurrent callers stay spaced apart. */
    private suspend fun awaitPace() {
        val waitMs = pacerMutex.withLock {
            val now = System.currentTimeMillis()
            val slot = maxOf(now, nextAllowedAtMs)
            nextAllowedAtMs = slot + currentDelayMs
            slot - now
        }
        if (waitMs > 0) delay(waitMs)
    }

    private suspend fun notifySuccess() {
        pacerMutex.withLock {
            currentDelayMs = (currentDelayMs - PACER_SUCCESS_STEP_MS).coerceAtLeast(PACER_MIN_DELAY_MS)
        }
    }

    private suspend fun notifyRateLimited(retryAfterMs: Long?) {
        pacerMutex.withLock {
            currentDelayMs = (currentDelayMs * 2).coerceAtMost(PACER_MAX_DELAY_MS)
            val penalty = retryAfterMs ?: currentDelayMs
            nextAllowedAtMs = maxOf(nextAllowedAtMs, System.currentTimeMillis() + penalty)
        }
    }

    suspend fun translate(request: TranslationRequest): TranslationResult =
        withContext(Dispatchers.IO) {
            val source = request.sourceLanguageCode
                ?.let(::normalizeTranslationLanguageCode)
                ?: AUTO_TRANSLATION_LANGUAGE
            val target = resolveTranslationLanguageCode(request.targetLanguageCode)
                ?: throw TranslationException("Unsupported target language.")

            if (Log.isLoggable(BOOK_TRANSLATION_LOG, Log.VERBOSE)) {
                Log.v(
                    BOOK_TRANSLATION_LOG,
                    "Google Translate request started: source=$source target=$target " +
                            "chars=${request.text.length}"
                )
            }
            var connection: HttpURLConnection? = null

            try {
                awaitPace()
                val activeConnection = openConnection(
                    sourceLanguageCode = source,
                    targetLanguageCode = target,
                    text = request.text
                ).also { connection = it }
                val responseCode = activeConnection.responseCode
                val responseBody = activeConnection.readBody(responseCode)
                if (Log.isLoggable(BOOK_TRANSLATION_LOG, Log.VERBOSE)) {
                    Log.v(
                        BOOK_TRANSLATION_LOG,
                        "Google Translate response: source=$source target=$target " +
                                "code=$responseCode bodyChars=${responseBody.length}"
                    )
                }
                if (responseCode !in 200..299) {
                    throw googleTranslateException(responseCode, activeConnection.retryAfterMs())
                }

                val result = parseTranslationResponse(
                    responseBody = responseBody,
                    fallbackSourceLanguageCode = source,
                    targetLanguageCode = target
                )
                notifySuccess()
                result
            } catch (exception: TranslationException) {
                if (exception is TranslationRateLimitedException) {
                    notifyRateLimited(exception.retryAfterMs)
                }
                Log.e(
                    BOOK_TRANSLATION_LOG,
                    "Google Translate request failed: source=$source target=$target",
                    exception
                )
                throw exception
            } catch (exception: Exception) {
                Log.e(
                    BOOK_TRANSLATION_LOG,
                    "Google Translate request crashed: source=$source target=$target",
                    exception
                )
                throw TranslationException("Could not translate text.", exception)
            } finally {
                connection?.disconnect()
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
            BufferedReader(InputStreamReader(input, Charsets.UTF_8)).use { it.readText() }
        }.orEmpty()
    }

    private fun HttpURLConnection.retryAfterMs(): Long? {
        val header = getHeaderField("Retry-After")?.trim()?.takeIf { it.isNotEmpty() } ?: return null
        // The endpoint sends Retry-After as integer seconds; ignore the rarely-used HTTP-date form.
        val seconds = header.toLongOrNull() ?: return null
        return (seconds * 1_000L).coerceIn(0L, PACER_MAX_RETRY_AFTER_MS)
    }

    private fun googleTranslateException(
        responseCode: Int,
        retryAfterMs: Long?
    ): TranslationException =
        when (responseCode) {
            HttpURLConnection.HTTP_UNAVAILABLE,
            HTTP_TOO_MANY_REQUESTS -> TranslationRateLimitedException(
                message = "Google Translate rate limit reached. Try again later, or switch to ML Kit offline translation.",
                retryAfterMs = retryAfterMs
            )

            HttpURLConnection.HTTP_FORBIDDEN -> TranslationRateLimitedException(
                message = "Google Translate blocked this request. Try again later, or switch to ML Kit offline translation.",
                retryAfterMs = retryAfterMs
            )

            else -> TranslationException("Google Translate request failed with HTTP $responseCode.")
        }

    private fun parseTranslationResponse(
        responseBody: String,
        fallbackSourceLanguageCode: String,
        targetLanguageCode: String
    ): TranslationResult {
        // Google may answer HTTP 200 with an HTML captcha / "unusual traffic" page when it soft-blocks
        // a client. That isn't valid JSON, so guard the cast and surface it as a rate-limit so the UI
        // can suggest waiting or switching to ML Kit, instead of a generic "could not translate".
        val root = runCatching { JsonParser.parseString(responseBody).asJsonArray }
            .getOrElse {
                throw TranslationRateLimitedException(
                    "Google Translate blocked this request. Try again later, or switch to ML Kit offline translation."
                )
            }
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

private const val HTTP_TOO_MANY_REQUESTS = 429
