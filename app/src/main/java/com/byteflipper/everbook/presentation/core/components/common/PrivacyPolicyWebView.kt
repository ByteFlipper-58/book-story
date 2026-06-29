/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.core.components.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext

/**
 * Renders the bundled `privacy_policy.html` asset with dynamic theme-aware colors.
 */
@Composable
fun PrivacyPolicyWebView(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    val html = remember {
        runCatching {
            context.assets.open("privacy_policy.html")
                .bufferedReader()
                .use { it.readText() }
        }.getOrDefault("")
    }

    ThemedHtmlView(
        html = html,
        modifier = modifier,
        baseUrl = "file:///android_asset/"
    )
}
