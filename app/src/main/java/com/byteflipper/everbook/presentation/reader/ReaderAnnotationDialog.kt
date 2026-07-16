/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.reader

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import com.byteflipper.everbook.R
import com.byteflipper.everbook.domain.reader.Bookmark
import com.byteflipper.everbook.presentation.core.components.common.StyledText
import com.byteflipper.everbook.presentation.core.components.modal_bottom_sheet.ModalBottomSheet
import com.byteflipper.everbook.ui.reader.ReaderEvent

@Composable
fun ReaderAnnotationDialog(
    selectedText: String?,
    editingAnnotation: Bookmark?,
    initialColorArgb: Int?,
    colors: List<Int>,
    saveAnnotation: (ReaderEvent.OnSaveAnnotation) -> Unit,
    managePalette: () -> Unit,
    dismiss: (ReaderEvent.OnDismissAnnotationEditor) -> Unit
) {
    val quote = selectedText ?: editingAnnotation?.quotedText ?: return
    var selectedColor by remember(selectedText, editingAnnotation, initialColorArgb) {
        mutableStateOf(
            editingAnnotation?.colorArgb.takeIf { editingAnnotation?.isHighlight == true }
                ?: initialColorArgb
        )
    }
    var note by remember(selectedText, editingAnnotation) {
        mutableStateOf(editingAnnotation?.note.orEmpty())
    }
    val isEditingExistingNote = !editingAnnotation?.note.isNullOrBlank()
    val noteFocusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(editingAnnotation?.id, isEditingExistingNote) {
        if (isEditingExistingNote) {
            noteFocusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    fun save(colorArgb: Int?) {
        saveAnnotation(ReaderEvent.OnSaveAnnotation(note = note, colorArgb = colorArgb))
    }

    ModalBottomSheet(
        onDismissRequest = { dismiss(ReaderEvent.OnDismissAnnotationEditor) },
        sheetGesturesEnabled = true
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (!isEditingExistingNote) {
                Text(
                    text = quote.take(160),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            StyledText(
                text = stringResource(id = R.string.annotation_style),
                style = MaterialTheme.typography.labelLarge
            )

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    AnnotationIconAction(
                        imageVector = Icons.Outlined.Bookmark,
                        contentDescription = stringResource(id = R.string.annotation_no_color),
                        selected = selectedColor == null,
                        onClick = { selectedColor = null }
                    )
                }
                items(colors) { argb ->
                    ReaderHighlightColorSwatch(
                        color = Color(argb),
                        selected = selectedColor == argb,
                        size = 40.dp,
                        onClick = { selectedColor = argb }
                    )
                }
                item {
                    AnnotationPaletteAction(onClick = managePalette)
                }
            }

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                minLines = 3,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(noteFocusRequester),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                ),
                label = { StyledText(text = stringResource(id = R.string.annotation_note)) },
                placeholder = { StyledText(text = stringResource(id = R.string.note_hint)) }
            )
            Button(
                onClick = { save(selectedColor) },
                modifier = Modifier.fillMaxWidth()
            ) {
                StyledText(text = stringResource(id = R.string.done))
            }
        }
    }
}

@Composable
private fun AnnotationIconAction(
    imageVector: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val outline = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
    Box(
        modifier = Modifier
            .size(40.dp)
            .border(if (selected) 3.dp else 1.dp, outline, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        IconButton(onClick = onClick) {
            Icon(
                imageVector = imageVector,
                contentDescription = contentDescription,
                tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AnnotationPaletteAction(onClick: () -> Unit) {
    OutlinedIconButton(
        onClick = onClick,
        modifier = Modifier.size(40.dp)
    ) {
        Icon(
            imageVector = Icons.Outlined.Palette,
            contentDescription = stringResource(R.string.highlight_palette_manage)
        )
    }
}
