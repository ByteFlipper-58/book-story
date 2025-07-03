/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * SPDX-License-Identifier: GPL-3.0-only
 */

@file:Suppress("FunctionName")

package com.byteflipper.everbook.presentation.start

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.byteflipper.everbook.R
import com.byteflipper.everbook.presentation.core.components.common.StyledText
import java.io.BufferedReader
import java.io.InputStreamReader
import androidx.compose.foundation.clickable
import androidx.compose.ui.viewinterop.AndroidView
import android.webkit.WebView
import android.webkit.WebSettings

fun LazyListScope.StartSettingsLayoutPrivacyPolicy(
    accepted: Boolean,
    onToggle: (Boolean) -> Unit
) {
    item { PrivacyPolicyTextContainer() }
    item { AcceptPrivacyPolicyCheck(accepted, onToggle) }
}

@Composable
private fun PrivacyPolicyTextContainer() {
    Box(
        modifier = Modifier
            .padding(top = 16.dp)
            .padding(horizontal = 24.dp)
            .heightIn(min = 300.dp) // минимальная высота, затем WebView скроллится внутри
    ) {
        AndroidView(
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
                    // Загружаем файл из assets
                    loadUrl("file:///android_asset/privacy_policy.html")
                    // Применяем фон, чтобы соответствовать теме
                    setBackgroundColor(android.graphics.Color.TRANSPARENT)
                }
            },
            update = { /* no-op */ }
        )
    }
}

@Composable
private fun AcceptPrivacyPolicyCheck(
    accepted: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle(!accepted) }
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = accepted,
            onCheckedChange = { onToggle(it) }
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(id = R.string.start_privacy_policy_accept),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
} 