/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * SPDX-License-Identifier: GPL-3.0-only
 */

@file:Suppress("FunctionName")

package com.byteflipper.everbook.presentation.start

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.byteflipper.everbook.R
import com.byteflipper.everbook.presentation.core.components.common.PrivacyPolicyWebView

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
        PrivacyPolicyWebView()
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
