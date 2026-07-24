/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.reader
import androidx.compose.ui.res.painterResource

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.byteflipper.everbook.R
import com.byteflipper.everbook.domain.translation.TranslationProviderMode
import com.byteflipper.everbook.presentation.core.components.common.StyledText
import com.byteflipper.everbook.presentation.core.components.modal_bottom_sheet.ModalBottomSheet
import com.byteflipper.everbook.presentation.core.util.showToast
import com.byteflipper.everbook.presentation.settings.components.SettingsSubcategoryTitle
import com.byteflipper.everbook.ui.reader.ReaderTranslationState

@Composable
fun ReaderTranslationBottomSheet(
    translation: ReaderTranslationState,
    openExternalTranslator: () -> Unit,
    toggleTranslationOriginal: () -> Unit,
    dismissBottomSheet: () -> Unit
) {
    val context = LocalContext.current
    val displayedText = when {
        translation.showOriginal -> translation.text
        translation.isDownloadingModel &&
                translation.providerMode == TranslationProviderMode.IN_APP ->
            stringResource(id = R.string.translation_downloading_model)
        translation.isTranslating -> stringResource(id = R.string.translation_translating)
        translation.errorMessage != null -> translation.errorMessage
        else -> translation.translatedText ?: translation.text
    }
    val canToggleOriginal = translation.translatedText != null || translation.errorMessage != null
    val canCopy = displayedText.isNotBlank() && !translation.isTranslating

    ModalBottomSheet(
        modifier = Modifier.fillMaxWidth(),
        onDismissRequest = dismissBottomSheet,
        sheetGesturesEnabled = true
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(bottom = 12.dp)
        ) {
            item {
                SettingsSubcategoryTitle(
                    title = stringResource(id = R.string.translation_selection_title),
                    padding = 18.dp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (translation.isTranslating) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            StyledText(
                                text = displayedText,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    } else {
                        StyledText(
                            text = displayedText,
                            modifier = Modifier.fillMaxWidth(),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = if (translation.errorMessage == null) {
                                    MaterialTheme.colorScheme.onSurface
                                } else MaterialTheme.colorScheme.error
                            )
                        )
                    }

                    if (translation.providerMode == TranslationProviderMode.GOOGLE_TRANSLATE) {
                        StyledText(
                            text = stringResource(id = R.string.translation_powered_by_google),
                            modifier = Modifier.fillMaxWidth(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Start
                            )
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        ReaderTranslationActionButton(
                            text = stringResource(id = R.string.copy),
                            enabled = canCopy,
                            icon = {
                                Icon(
                                    painter = painterResource(R.drawable.ic_content_copy_rounded_24px),
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            onClick = {
                                copyTranslationText(context = context, text = displayedText)
                            }
                        )

                        ReaderTranslationActionButton(
                            text = stringResource(
                                id = if (translation.showOriginal) {
                                    R.string.translation_show_translation
                                } else {
                                    R.string.translation_show_original
                                }
                            ),
                            enabled = canToggleOriginal,
                            icon = {
                                Icon(
                                    painter = if (translation.showOriginal) {
                                        painterResource(R.drawable.ic_translate_rounded_24px)
                                    } else painterResource(R.drawable.ic_visibility_rounded_24px),
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            onClick = toggleTranslationOriginal
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ReaderTranslationActionButton(
                            text = stringResource(id = R.string.translation_open_external),
                            enabled = true,
                            icon = {
                                Icon(
                                    painter = painterResource(R.drawable.ic_open_in_new_rounded_24px),
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            onClick = openExternalTranslator
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        ReaderTranslationActionButton(
                            text = stringResource(id = R.string.close),
                            enabled = true,
                            icon = {
                                Icon(
                                    painter = painterResource(R.drawable.ic_close_rounded_24px),
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            onClick = dismissBottomSheet
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReaderTranslationActionButton(
    text: String,
    enabled: Boolean,
    icon: @Composable () -> Unit,
    onClick: () -> Unit
) {
    TextButton(
        enabled = enabled,
        onClick = onClick,
        modifier = Modifier.height(36.dp)
    ) {
        icon()
        Spacer(modifier = Modifier.width(4.dp))
        StyledText(
            text = text,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

private fun copyTranslationText(context: Context, text: String) {
    val clipboardManager =
        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboardManager.setPrimaryClip(ClipData.newPlainText("translation", text))
    context.getString(R.string.copied).showToast(context = context, longToast = false)
}
