/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.core.components.common

import android.graphics.Color as AndroidColor
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import kotlin.math.roundToInt

/**
 * Renders the bundled `privacy_policy.html` asset in a [WebView] while keeping its text colors in
 * sync with the **in-app** theme palette.
 *
 * The asset only styles itself for the *system* light/dark mode via `@media (prefers-color-scheme)`.
 * EverBook lets the user pick a palette (light / dark / AMOLED / contrast) independently of the
 * system setting, so on a mismatch the hard-coded text color blends into the surface. To fix that we
 * inject an overriding `<style>` built from the current [MaterialTheme] colors before loading the
 * document, instead of relying on the system color scheme.
 */
@Composable
fun PrivacyPolicyWebView(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    val bodyColor = MaterialTheme.colorScheme.onSurface
    val linkColor = MaterialTheme.colorScheme.primary
    val codeBackground = MaterialTheme.colorScheme.surfaceVariant
    val codeColor = MaterialTheme.colorScheme.onSurfaceVariant

    val html = remember(bodyColor, linkColor, codeBackground, codeColor) {
        val raw = runCatching {
            context.assets.open("privacy_policy.html")
                .bufferedReader()
                .use { it.readText() }
        }.getOrDefault("")

        // `!important` so the override wins over both the base rules and the prefers-color-scheme
        // media query already present in the asset.
        val overrideStyle = """
            <style>
                body { color: ${bodyColor.toCssRgb()} !important; background: transparent !important; }
                h1, h2, h3 { color: ${bodyColor.toCssRgb()} !important; }
                a { color: ${linkColor.toCssRgb()} !important; }
                code {
                    background: ${codeBackground.toCssRgb()} !important;
                    color: ${codeColor.toCssRgb()} !important;
                }
            </style>
        """.trimIndent()

        if (raw.contains("</head>", ignoreCase = true)) {
            raw.replaceFirst("</head>", "$overrideStyle</head>")
        } else {
            "$overrideStyle$raw"
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            WebView(ctx).apply {
                settings.apply {
                    javaScriptEnabled = false
                    domStorageEnabled = false
                    cacheMode = WebSettings.LOAD_NO_CACHE
                    builtInZoomControls = false
                    displayZoomControls = false
                    textZoom = 100
                }
                isVerticalScrollBarEnabled = true
                overScrollMode = WebView.OVER_SCROLL_IF_CONTENT_SCROLLS
                // Let the underlying Compose surface show through; the injected CSS handles text.
                setBackgroundColor(AndroidColor.TRANSPARENT)
            }
        },
        update = { webView ->
            // Reload only when the themed markup actually changed (e.g. palette switch), so unrelated
            // recompositions don't reset the user's scroll position.
            if (webView.tag != html) {
                webView.tag = html
                webView.loadDataWithBaseURL(
                    "file:///android_asset/",
                    html,
                    "text/html",
                    "utf-8",
                    null
                )
            }
        }
    )
}

private fun Color.toCssRgb(): String {
    val r = (red * 255).roundToInt()
    val g = (green * 255).roundToInt()
    val b = (blue * 255).roundToInt()
    return "rgb($r, $g, $b)"
}
