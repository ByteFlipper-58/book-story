/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

@file:Suppress("FunctionName")

package com.byteflipper.everbook.presentation.start

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.byteflipper.everbook.R
import com.byteflipper.everbook.presentation.core.components.common.StyledText
import com.byteflipper.everbook.ui.about.AboutEvent
import com.byteflipper.everbook.ui.about.AboutModel

fun LazyListScope.StartSettingsLayoutSourceCode(aboutModel: AboutModel) {
    item {
        SourceCodeIcon()
    }
    
    item {
        SourceCodeTitle()
    }
    
    item {
        SourceCodeDescription()
    }
    
    item {
        SourceCodeButton(aboutModel)
    }
}

@Composable
private fun SourceCodeIcon() {
    Spacer(modifier = Modifier.height(24.dp))
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Code,
            contentDescription = "Source Code Icon",
            modifier = Modifier.size(100.dp),
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun SourceCodeTitle() {
    Text(
        modifier = Modifier
            .padding(top = 16.dp)
            .padding(horizontal = 16.dp)
            .fillMaxWidth(),
        text = stringResource(id = R.string.start_fork_info_title),
        color = MaterialTheme.colorScheme.onSurface,
        style = MaterialTheme.typography.headlineSmall,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun SourceCodeDescription() {
    Text(
        modifier = Modifier
            .padding(top = 8.dp)
            .padding(horizontal = 24.dp)
            .fillMaxWidth(),
        text = stringResource(id = R.string.start_fork_info_desc),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun SourceCodeButton(aboutModel: AboutModel) {
    val context = LocalContext.current
    
    Spacer(modifier = Modifier.height(8.dp))
    OutlinedButton (
        modifier = Modifier
            .padding(horizontal = 32.dp)
            .fillMaxWidth(),
        onClick = {
            aboutModel.onEvent(
                AboutEvent.OnNavigateToBrowserPage(
                    page = "https://github.com/Acclorite/book-story",
                    context = context
                )
            )
        },
        shape = MaterialTheme.shapes.medium,
    ) {
        StyledText(text = stringResource(id = R.string.start_source_code))
    }
    Spacer(modifier = Modifier.height(16.dp))
} 