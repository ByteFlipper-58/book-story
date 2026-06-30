/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
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
import androidx.compose.ui.viewinterop.AndroidView
import kotlin.math.roundToInt

@Composable
fun ThemedHtmlView(
    html: String,
    modifier: Modifier = Modifier,
    baseUrl: String? = null
) {
    val bodyColor = MaterialTheme.colorScheme.onSurface
    val linkColor = MaterialTheme.colorScheme.primary
    val codeBackground = MaterialTheme.colorScheme.surfaceVariant
    val codeColor = MaterialTheme.colorScheme.onSurfaceVariant

    val themedHtml = remember(html, bodyColor, linkColor, codeBackground, codeColor) {
        val overrideStyle = """
            <style>
                body { color: ${bodyColor.toCssRgb()} !important; background: transparent !important; }
                h1, h2, h3, h4, h5, h6 { color: ${bodyColor.toCssRgb()} !important; }
                p, li, span, div { color: ${bodyColor.toCssRgb()} !important; }
                a { color: ${linkColor.toCssRgb()} !important; }
                code, pre {
                    background: ${codeBackground.toCssRgb()} !important;
                    color: ${codeColor.toCssRgb()} !important;
                }
            </style>
        """.trimIndent()

        if (html.contains("</head>", ignoreCase = true)) {
            html.replaceFirst("</head>", "$overrideStyle</head>")
        } else {
            "$overrideStyle$html"
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
                setBackgroundColor(AndroidColor.TRANSPARENT)
            }
        },
        update = { webView ->
            if (webView.tag != themedHtml) {
                webView.tag = themedHtml
                webView.loadDataWithBaseURL(baseUrl, themedHtml, "text/html", "utf-8", null)
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
