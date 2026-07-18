/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.settings.reader.font.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.clickable
import androidx.compose.ui.unit.dp
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.byteflipper.everbook.R
import com.byteflipper.everbook.presentation.core.constants.provideFonts
import com.byteflipper.everbook.data.font.CustomFontStore
import com.byteflipper.everbook.presentation.settings.components.SettingsSubcategoryTitle
import com.byteflipper.everbook.ui.main.MainEvent
import com.byteflipper.everbook.ui.main.MainModel

@Composable
fun FontFamilyOption() {
    val mainModel = hiltViewModel<MainModel>()
    val state = mainModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var refreshToken by remember { mutableStateOf(0) }
    val fonts = remember(context, refreshToken) { provideFonts(context) }
    val importFonts = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        if (uris.isNotEmpty()) {
            scope.launch {
                val importedFonts = withContext(Dispatchers.IO) {
                    uris.mapNotNull { CustomFontStore.import(context, it) }
                }
                if (importedFonts.isNotEmpty()) {
                    refreshToken++
                    mainModel.onEvent(MainEvent.OnChangeFontFamily(importedFonts.last().id))
                }
            }
        }
    }

    val fontFamily = remember(state.value.fontFamily, fonts) {
        fonts.run {
            find {
                it.id == state.value.fontFamily
            } ?: get(0)
        }
    }

    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 8.dp)
    ) {
        SettingsSubcategoryTitle(
            title = stringResource(R.string.font_family_option),
            padding = 0.dp
        )
        Spacer(Modifier.height(8.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            fonts.forEach { item ->
                val selected = item.id == fontFamily.id
                FilterChip(
                    modifier = Modifier.height(36.dp),
                    selected = selected,
                    label = {
                        Text(
                            text = item.fontName.asString(),
                            style = MaterialTheme.typography.labelLarge.copy(fontFamily = item.font),
                            maxLines = 1
                        )
                    },
                    trailingIcon = if (selected && item.id.startsWith("custom:")) {
                        {
                            Icon(
                                painter = painterResource(R.drawable.ic_close_rounded_24px),
                                contentDescription = stringResource(R.string.remove_font),
                                modifier = Modifier.clickable {
                                    CustomFontStore.delete(context, item.id)
                                    refreshToken++
                                    mainModel.onEvent(MainEvent.OnChangeFontFamily(provideFonts().first().id))
                                }
                            )
                        }
                    } else null,
                    onClick = { mainModel.onEvent(MainEvent.OnChangeFontFamily(item.id)) }
                )
            }
            FilterChip(
                modifier = Modifier.height(36.dp),
                selected = false,
                label = { Text(stringResource(R.string.import_font)) },
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_add_rounded_24px),
                        contentDescription = null
                    )
                },
                onClick = {
                    importFonts.launch(
                        arrayOf("font/ttf", "font/otf", "application/x-font-ttf", "application/x-font-opentype")
                    )
                }
            )
        }
    }
}
