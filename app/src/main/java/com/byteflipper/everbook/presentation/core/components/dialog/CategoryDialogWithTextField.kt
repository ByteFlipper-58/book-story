/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.core.components.dialog

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.byteflipper.everbook.R
import com.byteflipper.everbook.presentation.core.components.common.StyledText
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun CategoryDialogWithTextField(
    title: String,
    placeholder: String,
    initialValue: String,
    icon: ImageVector? = null,
    description: String? = null,
    lengthLimit: Int = 50,
    onDismiss: () -> Unit,
    onAction: (String) -> Unit
) {
    val state = remember(initialValue) {
        mutableStateOf(
            TextFieldValue(
                text = initialValue,
                selection = TextRange(0, initialValue.length)
            )
        )
    }
    val focusRequester = remember { FocusRequester() }
    val focused = remember { mutableStateOf(false) }

    Dialog(
        title = title,
        icon = icon,
        description = description,
        onDismiss = { onDismiss() },
        actionEnabled = state.value.text.isNotBlank(),
        disableOnClick = false,
        onAction = {
            if (state.value.text == initialValue) {
                onDismiss()
                return@Dialog
            }

            onAction(state.value.text.trim())
            onDismiss()
        },
        withContent = true,
        items = {
            item {
                OutlinedTextField(
                    value = state.value,
                    onValueChange = {
                        if (it.text.length < lengthLimit || it.text.length < state.value.text.length) {
                            state.value = it
                        }
                    },
                    keyboardOptions = KeyboardOptions(KeyboardCapitalization.Sentences),
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                        .onGloballyPositioned {
                            if (!focused.value) {
                                focusRequester.requestFocus()
                                focused.value = true
                            }
                        },
                    colors = TextFieldDefaults.colors(
                        errorContainerColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    ),
                    placeholder = {
                        StyledText(
                            text = placeholder,
                            maxLines = 1
                        )
                    },
                    singleLine = true
                )
            }
        }
    )
} 