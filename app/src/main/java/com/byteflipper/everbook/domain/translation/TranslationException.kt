/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.domain.translation

open class TranslationException(message: String, cause: Throwable? = null) : Exception(message, cause)

class TranslationRateLimitedException(
    message: String,
    cause: Throwable? = null,
    /** Server-advised wait (from a `Retry-After` header), if any, in milliseconds. */
    val retryAfterMs: Long? = null
) : TranslationException(message, cause)

/**
 * Signals that a full-book translation could not finish this run because the provider is
 * persistently rate-limiting. It is not a failure: the worker should ask WorkManager to reschedule
 * (with backoff) and the executor resumes from already-translated entries on the next run.
 */
class BookTranslationRescheduleException(
    message: String
) : TranslationException(message)
