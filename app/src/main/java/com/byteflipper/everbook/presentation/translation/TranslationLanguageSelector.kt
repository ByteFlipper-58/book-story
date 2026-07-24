/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)

package com.byteflipper.everbook.presentation.translation
import androidx.compose.ui.res.painterResource

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.byteflipper.everbook.R
import com.byteflipper.everbook.domain.translation.AUTO_TRANSLATION_LANGUAGE
import com.byteflipper.everbook.domain.translation.DEVICE_TRANSLATION_LANGUAGE
import com.byteflipper.everbook.domain.translation.TranslationLanguage
import com.byteflipper.everbook.domain.translation.supportedDeviceTranslationLanguageCode
import com.byteflipper.everbook.presentation.core.components.common.StyledText

@Composable
fun TranslationLanguageSelector(
    sourceLanguageCode: String,
    targetLanguageCode: String,
    languages: List<TranslationLanguage>,
    allowAutoSource: Boolean = true,
    allowDeviceTarget: Boolean = true,
    onSourceLanguageClick: () -> Unit,
    onTargetLanguageClick: () -> Unit,
    onSwapLanguages: () -> Unit,
    modifier: Modifier = Modifier
) {
    val distinctLanguages = remember(languages) {
        languages.distinctBy { it.code }.sortedBy { it.name.lowercase() }
    }
    val supportedCodes = remember(distinctLanguages) {
        distinctLanguages.mapTo(mutableSetOf()) { it.code }
    }
    val deviceLanguageSupported = remember(supportedCodes) {
        supportedDeviceTranslationLanguageCode(supportedCodes) != null
    }
    val canSwap = sourceLanguageCode != AUTO_TRANSLATION_LANGUAGE &&
            sourceLanguageCode != DEVICE_TRANSLATION_LANGUAGE &&
            targetLanguageCode != DEVICE_TRANSLATION_LANGUAGE &&
            distinctLanguages.any { it.code == sourceLanguageCode } &&
            distinctLanguages.any { it.code == targetLanguageCode }

    val sourceTitle = languageTitle(
        languageCode = sourceLanguageCode,
        languages = distinctLanguages,
        allowAuto = allowAutoSource,
        allowDevice = false,
        deviceLanguageSupported = false
    )
    val targetTitle = languageTitle(
        languageCode = targetLanguageCode,
        languages = distinctLanguages,
        allowAuto = false,
        allowDevice = allowDeviceTarget,
        deviceLanguageSupported = deviceLanguageSupported
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 8.dp)
    ) {
        val gap = 8.dp
        val swapButtonSize = 48.dp
        val languageButtonWidth = (maxWidth - swapButtonSize - gap * 2) / 2

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            userScrollEnabled = false,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(gap)
        ) {
            item(
                key = languageButtonKey(
                    languageCode = sourceLanguageCode,
                    otherLanguageCode = targetLanguageCode,
                    slotKey = "source"
                )
            ) {
                LanguageButton(
                    value = sourceTitle,
                    modifier = Modifier
                        .width(languageButtonWidth)
                        .animateItem(),
                    onClick = onSourceLanguageClick
                )
            }

            item(key = "swap") {
                IconButton(
                    modifier = Modifier
                        .size(swapButtonSize)
                        .animateItem(),
                    enabled = canSwap,
                    onClick = onSwapLanguages,
                    colors = IconButtonDefaults.filledTonalIconButtonColors()
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_swap_horiz_rounded_24px),
                        contentDescription = stringResource(
                            id = R.string.translation_swap_languages_content_desc
                        )
                    )
                }
            }

            item(
                key = languageButtonKey(
                    languageCode = targetLanguageCode,
                    otherLanguageCode = sourceLanguageCode,
                    slotKey = "target"
                )
            ) {
                LanguageButton(
                    value = targetTitle,
                    modifier = Modifier
                        .width(languageButtonWidth)
                        .animateItem(),
                    onClick = onTargetLanguageClick
                )
            }
        }
    }
}

@Composable
private fun LanguageButton(
    value: String,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .heightIn(min = 56.dp)
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            StyledText(
                text = value,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.labelLarge.copy(
                    textAlign = TextAlign.Center
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun languageButtonKey(
    languageCode: String,
    otherLanguageCode: String,
    slotKey: String
): String =
    if (languageCode == otherLanguageCode) {
        "${slotKey}_$languageCode"
    } else {
        "language_$languageCode"
    }

@Composable
fun translationLanguageTitle(
    languageCode: String,
    languages: List<TranslationLanguage>,
    allowAuto: Boolean,
    allowDevice: Boolean,
    deviceLanguageSupported: Boolean = true
): String = languageTitle(
    languageCode = languageCode,
    languages = languages,
    allowAuto = allowAuto,
    allowDevice = allowDevice,
    deviceLanguageSupported = deviceLanguageSupported
)

@Composable
private fun languageTitle(
    languageCode: String,
    languages: List<TranslationLanguage>,
    allowAuto: Boolean,
    allowDevice: Boolean,
    deviceLanguageSupported: Boolean
): String {
    if (allowAuto && languageCode == AUTO_TRANSLATION_LANGUAGE) {
        return stringResource(id = R.string.translation_language_auto)
    }
    if (allowDevice && languageCode == DEVICE_TRANSLATION_LANGUAGE && deviceLanguageSupported) {
        return stringResource(id = R.string.translation_language_device)
    }
    if (languageCode == DEVICE_TRANSLATION_LANGUAGE) {
        return stringResource(id = R.string.translation_language_choose)
    }
    return languages.firstOrNull { it.code == languageCode }?.name ?: languageCode.uppercase()
}
