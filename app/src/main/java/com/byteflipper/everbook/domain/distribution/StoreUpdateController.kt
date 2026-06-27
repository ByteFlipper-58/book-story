/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.domain.distribution

import androidx.activity.ComponentActivity

/**
 * Store-specific in-app update and review flows. Implemented per distribution flavor:
 * a real implementation on stores that expose such APIs (e.g. RuStore), and no-ops elsewhere.
 */
interface StoreUpdateController {
    /** Called once the main activity is ready — a good moment to check for app updates. */
    fun onActivityReady(activity: ComponentActivity)

    /** Called whenever the reader is opened — used to gate an in-app review request. */
    fun onReaderOpened(activity: ComponentActivity)

    /**
     * Whether this flavour can launch an in-app review flow. True for store flavours
     * (Play Store / RuStore); false elsewhere (e.g. everbook), where the About screen hides the
     * "leave a review" entry.
     */
    val supportsInAppReview: Boolean

    /**
     * Launch the store's in-app review flow on explicit user request (the About screen button).
     * Unlike [onReaderOpened], this is not gated by usage thresholds and may be invoked repeatedly.
     * No-op on flavours where [supportsInAppReview] is false.
     */
    fun requestReview(activity: ComponentActivity)
}
