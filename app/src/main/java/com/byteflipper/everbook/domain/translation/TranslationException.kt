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
    cause: Throwable? = null
) : TranslationException(message, cause)
