/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.reader
import androidx.compose.ui.res.painterResource

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.byteflipper.everbook.R
import com.byteflipper.everbook.domain.reader.HighlightPalette
import com.byteflipper.everbook.presentation.core.components.modal_bottom_sheet.ModalBottomSheet
import com.byteflipper.everbook.presentation.core.components.settings.ColorPickerWithTitle
import kotlin.random.Random

@Composable
fun ReaderHighlightPaletteEditor(
    visible: Boolean,
    colors: List<Int>,
    updateColors: (List<Int>) -> Unit,
    dismiss: (Int) -> Unit
) {
    if (!visible) return

    var workingColors by remember { mutableStateOf(colors.ifEmpty { ReaderHighlightColors.defaultArgbPalette }) }
    var selectedIndex by remember { mutableIntStateOf(0) }
    selectedIndex = selectedIndex.coerceIn(0, workingColors.lastIndex)
    val selectedColor = Color(workingColors[selectedIndex])

    fun commit(updated: List<Int>, nextIndex: Int = selectedIndex) {
        val selectedArgb = updated.getOrNull(nextIndex)
        val normalized = HighlightPalette.normalize(updated)
        workingColors = normalized
        selectedIndex = selectedArgb
            ?.let(normalized::indexOf)
            ?.takeIf { it >= 0 }
            ?: nextIndex.coerceIn(0, normalized.lastIndex)
        updateColors(normalized)
    }

    fun updateSelected(color: Color) {
        val updated = workingColors.toMutableList()
        updated[selectedIndex] = color.copy(alpha = 1f).toArgb()
        commit(updated)
    }

    ModalBottomSheet(
        onDismissRequest = { dismiss(selectedColor.toArgb()) },
        sheetGesturesEnabled = true,
        skipPartiallyExpanded = false
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 620.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.highlight_palette_title),
                style = MaterialTheme.typography.titleLarge
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                workingColors.forEachIndexed { index, argb ->
                    ReaderHighlightColorSwatch(
                        color = Color(argb),
                        selected = index == selectedIndex,
                        size = 40.dp,
                        onClick = { selectedIndex = index }
                    )
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surfaceContainerHigh
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(selectedColor, CircleShape)
                                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                        )
                        Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
                            Text(
                                text = stringResource(R.string.highlight_palette_preview),
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "#%08X".format(selectedColor.toArgb()),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(
                            onClick = {
                                updateSelected(
                                    Color(
                                        red = Random.nextInt(256),
                                        green = Random.nextInt(256),
                                        blue = Random.nextInt(256)
                                    )
                                )
                            }
                        ) {
                            Icon(painterResource(R.drawable.ic_shuffle_rounded_24px), stringResource(R.string.shuffle_color_preset_content_desc))
                        }
                        IconButton(
                            enabled = workingColors.size < HighlightPalette.maxSize,
                            onClick = {
                                var newColor: Int
                                do {
                                    newColor = Color(
                                        red = Random.nextInt(256),
                                        green = Random.nextInt(256),
                                        blue = Random.nextInt(256)
                                    ).toArgb()
                                } while (newColor in workingColors)
                                commit(workingColors + newColor, workingColors.size)
                            }
                        ) {
                            Icon(painterResource(R.drawable.ic_add_rounded_24px), stringResource(R.string.highlight_palette_add))
                        }
                        IconButton(
                            enabled = workingColors.size > 1,
                            onClick = {
                                val updated = workingColors.toMutableList().also { it.removeAt(selectedIndex) }
                                commit(updated, selectedIndex.coerceAtMost(updated.lastIndex))
                            }
                        ) {
                            Icon(painterResource(R.drawable.ic_delete_rounded_24px), stringResource(R.string.delete))
                        }
                    }

                    ColorPickerWithTitle(
                        value = selectedColor,
                        presetId = selectedIndex,
                        title = stringResource(R.string.highlight_palette_color),
                        horizontalPadding = 16.dp,
                        verticalPadding = 0.dp,
                        onValueChange = ::updateSelected
                    )
                }
            }
        }
    }
}
