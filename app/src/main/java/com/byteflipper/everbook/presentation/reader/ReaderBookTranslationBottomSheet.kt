/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.reader

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.byteflipper.everbook.R
import com.byteflipper.everbook.domain.translation.AUTO_TRANSLATION_LANGUAGE
import com.byteflipper.everbook.domain.translation.BookTranslation
import com.byteflipper.everbook.domain.translation.DEVICE_TRANSLATION_LANGUAGE
import com.byteflipper.everbook.domain.translation.TranslationFeature
import com.byteflipper.everbook.domain.translation.TranslationProviderMode
import com.byteflipper.everbook.domain.translation.provideGoogleTranslateLanguages
import com.byteflipper.everbook.domain.ui.ButtonItem
import com.byteflipper.everbook.presentation.core.components.common.StyledText
import com.byteflipper.everbook.presentation.core.components.modal_bottom_sheet.ModalBottomSheet
import com.byteflipper.everbook.presentation.core.components.settings.SegmentedButtonWithTitle
import com.byteflipper.everbook.presentation.core.components.settings.SwitchWithTitle
import com.byteflipper.everbook.presentation.navigator.LocalNavigator
import com.byteflipper.everbook.presentation.settings.components.SettingsSubcategoryNote
import com.byteflipper.everbook.presentation.settings.translator.components.nativeTranslationLanguages
import com.byteflipper.everbook.presentation.translation.TranslationLanguageSelector
import com.byteflipper.everbook.presentation.translation.language_selection.TranslationLanguageSelectionRole
import com.byteflipper.everbook.ui.reader.ReaderBookTranslationDisplayMode
import com.byteflipper.everbook.ui.reader.ReaderBookTranslationState
import com.byteflipper.everbook.ui.settings.TranslatorSettingsModel
import com.byteflipper.everbook.ui.theme.dynamicListItemColor
import com.byteflipper.everbook.ui.translation.TranslationLanguageSelectionScreen
import com.byteflipper.everbook.ui.translation.TranslationLanguageSelectionTarget

private const val BOOK_TRANSLATION_LOG = "BookTranslation"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderBookTranslationBottomSheet(
    state: ReaderBookTranslationState,
    startTranslation: () -> Unit,
    showTranslatedBook: () -> Unit,
    showOriginalBook: () -> Unit,
    confirmGoogleWarning: () -> Unit,
    dismissGoogleWarning: () -> Unit,
    cancelTranslation: (Long) -> Unit,
    pauseTranslation: (Long) -> Unit,
    resumeTranslation: (Long) -> Unit,
    retryTranslation: (Long) -> Unit,
    changeProviderMode: (String) -> Unit,
    changeSourceLanguage: (String) -> Unit,
    changeTargetLanguage: (String) -> Unit,
    swapLanguages: () -> Unit,
    changeWifiOnly: (Boolean) -> Unit,
    dismissError: () -> Unit,
    dismissBottomSheet: () -> Unit
) {
    val retryTranslationState = state.retryTranslation
    val runningTranslation = state.runningTranslation
    val pausedTranslation = state.pausedTranslation

    if (state.showGoogleWarning) {
        BasicAlertDialog(onDismissRequest = dismissGoogleWarning) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surfaceContainerHigh
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    StyledText(
                        text = stringResource(id = R.string.book_translation_google_warning_title),
                        style = MaterialTheme.typography.headlineSmall
                    )
                    StyledText(
                        text = stringResource(id = R.string.book_translation_google_warning_desc),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = dismissGoogleWarning) {
                            StyledText(
                                text = stringResource(id = R.string.cancel),
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                        TextButton(onClick = confirmGoogleWarning) {
                            StyledText(
                                text = stringResource(id = R.string.ok),
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }
        }
    }

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
                BookTranslationProviderOption(
                    providerMode = state.providerMode,
                    changeProviderMode = changeProviderMode
                )
            }

            item {
                BookTranslationLanguageSelector(
                    bookId = state.currentTranslation?.bookId,
                    providerMode = state.providerMode,
                    sourceLanguageCode = state.sourceLanguageCode,
                    targetLanguageCode = state.targetLanguageCode,
                    changeSourceLanguage = changeSourceLanguage,
                    changeTargetLanguage = changeTargetLanguage,
                    swapLanguages = swapLanguages
                )
            }

            if (state.providerMode == TranslationProviderMode.IN_APP) {
                item {
                    SwitchWithTitle(
                        selected = state.requireWifi,
                        title = stringResource(id = R.string.translation_wifi_only_option),
                        description = stringResource(id = R.string.translation_wifi_only_option_desc),
                        onClick = { changeWifiOnly(!state.requireWifi) }
                    )
                }
            }

            if (state.providerMode == TranslationProviderMode.GOOGLE_TRANSLATE) {
                item {
                    SettingsSubcategoryNote(
                        text = stringResource(id = R.string.book_translation_google_notice),
                        horizontalPadding = 18.dp
                    )
                }
            }

            if (state.errorMessage != null) {
                item {
                    BookTranslationError(
                        message = state.errorMessage,
                        dismissError = dismissError
                    )
                }
            }

            if (retryTranslationState != null) {
                item {
                    RetryBookTranslation(
                        translation = retryTranslationState,
                        retryTranslation = retryTranslation
                    )
                }
            }

            item {
                BookTranslationActionButton(
                    state = state,
                    retryTranslation = retryTranslation,
                    cancelTranslation = cancelTranslation,
                    switchToInAppTranslation = {
                        changeProviderMode(TranslationProviderMode.IN_APP.name)
                    },
                    onClick = {
                        val currentTranslation = state.currentTranslation
                        when {
                            state.isStarting || state.isApplyingTranslation -> Log.i(
                                BOOK_TRANSLATION_LOG,
                                "Translate book button ignored: busy " +
                                        "isStarting=${state.isStarting} " +
                                        "isApplying=${state.isApplyingTranslation}"
                            )

                            currentTranslation?.isBusy == true -> Log.i(
                                BOOK_TRANSLATION_LOG,
                                "Translate book button ignored: translation is already running " +
                                        "translationId=${currentTranslation.id} " +
                                        "status=${currentTranslation.status}"
                            )

                            currentTranslation?.canResume == true -> {
                                Log.i(
                                    BOOK_TRANSLATION_LOG,
                                    "Resume book translation button clicked: " +
                                            "translationId=${currentTranslation.id}"
                                )
                                resumeTranslation(currentTranslation.id)
                            }

                            currentTranslation?.canRead == true &&
                                    state.displayMode == ReaderBookTranslationDisplayMode.TRANSLATED &&
                                    state.activeTranslationId == currentTranslation.id -> {
                                Log.i(
                                    BOOK_TRANSLATION_LOG,
                                    "Show original book button clicked: " +
                                            "translationId=${currentTranslation.id}"
                                )
                                showOriginalBook()
                            }

                            currentTranslation?.canRead == true -> {
                                Log.i(
                                    BOOK_TRANSLATION_LOG,
                                    "Show translated book button clicked: " +
                                            "translationId=${currentTranslation.id}"
                                )
                                showTranslatedBook()
                            }

                            else -> {
                                Log.i(
                                    BOOK_TRANSLATION_LOG,
                                    "Translate book button clicked: provider=${state.providerMode} " +
                                            "source=${state.sourceLanguageCode} " +
                                            "target=${state.targetLanguageCode} " +
                                            "wifiOnly=${state.requireWifi}"
                                )
                                startTranslation()
                            }
                        }
                    }
                )
            }

            if (runningTranslation != null || pausedTranslation != null) {
                item {
                    BookTranslationInlineActions(
                        runningTranslation = runningTranslation,
                        pausedTranslation = pausedTranslation,
                        pauseTranslation = pauseTranslation,
                        resumeTranslation = resumeTranslation,
                        cancelTranslation = cancelTranslation
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BookTranslationActionButton(
    state: ReaderBookTranslationState,
    retryTranslation: (Long) -> Unit,
    cancelTranslation: (Long) -> Unit,
    switchToInAppTranslation: () -> Unit,
    onClick: () -> Unit
) {
    val isStarting = state.isStarting
    val isApplying = state.isApplyingTranslation
    val translation = state.currentTranslation
    val busyTranslation = state.runningTranslation
    val pausedTranslation = state.pausedTranslation
    val completedTranslation = state.completedTranslation
    val rateLimitedTranslation = state.rateLimitedTranslation
    var showRateLimitActions by remember(rateLimitedTranslation?.id) {
        mutableStateOf(false)
    }
    val progressTranslation = busyTranslation ?: pausedTranslation ?: rateLimitedTranslation
        ?.takeIf { it.completedUnits > 0 }
    val canClick = state.isBookTextReadyForTranslation ||
            isStarting ||
            isApplying ||
            busyTranslation != null ||
            pausedTranslation != null ||
            completedTranslation != null ||
            rateLimitedTranslation != null
    val buttonText = when {
        isApplying -> stringResource(id = R.string.book_translation_applying)
        pausedTranslation != null -> stringResource(id = R.string.book_translation_resume)
        busyTranslation != null -> stringResource(
            id = R.string.book_translation_progress,
            busyTranslation.completedUnits,
            busyTranslation.totalUnits
        )

        rateLimitedTranslation != null && rateLimitedTranslation.completedUnits > 0 ->
            stringResource(
                id = R.string.book_translation_google_rate_limited_progress,
                rateLimitedTranslation.completedUnits,
                rateLimitedTranslation.totalUnits
            )

        rateLimitedTranslation != null ->
            stringResource(id = R.string.book_translation_google_rate_limited)

        !state.isBookTextReadyForTranslation -> {
            stringResource(id = R.string.book_translation_not_ready)
        }

        completedTranslation != null && state.displayMode == ReaderBookTranslationDisplayMode.TRANSLATED &&
                state.activeTranslationId == completedTranslation.id ->
            stringResource(id = R.string.book_translation_show_original)

        completedTranslation != null -> stringResource(id = R.string.book_translation_show_translation)
        progressTranslation != null -> stringResource(
            id = R.string.book_translation_progress,
            progressTranslation.completedUnits,
            progressTranslation.totalUnits
        )

        else -> stringResource(id = R.string.book_translation_start)
    }
    val containerColor = when {
        rateLimitedTranslation != null -> MaterialTheme.colorScheme.tertiaryContainer
        canClick -> MaterialTheme.colorScheme.secondaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val contentColor = when {
        rateLimitedTranslation != null -> MaterialTheme.colorScheme.onTertiaryContainer
        canClick -> MaterialTheme.colorScheme.onSecondaryContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val progressColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)

    Surface(
        onClick = {
            Log.i(
                BOOK_TRANSLATION_LOG,
                "Translate book action tapped: isStarting=$isStarting isApplying=$isApplying " +
                        "translationId=${translation?.id} status=${translation?.status} " +
                        "busy=${translation?.isBusy} canRead=${translation?.canRead} " +
                        "textReady=${state.isBookTextReadyForTranslation} " +
                        "displayMode=${state.displayMode} activeId=${state.activeTranslationId}"
            )
            if (!canClick) {
                Log.i(BOOK_TRANSLATION_LOG, "Translate book action ignored: book text is not ready")
            } else if (rateLimitedTranslation != null) {
                showRateLimitActions = true
            } else {
                onClick()
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 12.dp)
            .height(52.dp),
        shape = MaterialTheme.shapes.extraLarge,
        color = containerColor,
        contentColor = contentColor
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            if (progressTranslation != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progressTranslation.progress.coerceIn(0f, 1f))
                        .height(52.dp)
                        .align(Alignment.CenterStart)
                        .background(progressColor)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 16.dp
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (isStarting || isApplying) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = contentColor
                    )
                } else if (pausedTranslation != null) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                } else if (rateLimitedTranslation != null) {
                    Icon(
                        imageVector = Icons.Default.WarningAmber,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                } else if (progressTranslation == null && state.isBookTextReadyForTranslation) {
                    Icon(
                        imageVector = Icons.Default.Translate,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
                if (
                    isStarting ||
                    isApplying ||
                    pausedTranslation != null ||
                    rateLimitedTranslation != null ||
                    (progressTranslation == null && state.isBookTextReadyForTranslation)
                ) {
                    Spacer(modifier = Modifier.width(8.dp))
                }
                StyledText(
                    text = buttonText,
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .then(
                            if (rateLimitedTranslation != null) {
                                Modifier.basicMarquee(iterations = Int.MAX_VALUE)
                            } else {
                                Modifier
                            }
                        ),
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = contentColor,
                        textAlign = TextAlign.Center
                    ),
                    maxLines = 1
                )
            }
        }
    }

    if (rateLimitedTranslation != null && showRateLimitActions) {
        BookTranslationRateLimitActionsBottomSheet(
            translation = rateLimitedTranslation,
            canSwitchToInApp = TranslationProviderMode.IN_APP in
                    TranslationFeature.AVAILABLE_FULL_BOOK_PROVIDER_MODES,
            retryTranslation = retryTranslation,
            switchToInAppTranslation = switchToInAppTranslation,
            cancelTranslation = cancelTranslation,
            dismissBottomSheet = {
                showRateLimitActions = false
            }
        )
    }
}

@Composable
private fun BookTranslationRateLimitActionsBottomSheet(
    translation: BookTranslation,
    canSwitchToInApp: Boolean,
    retryTranslation: (Long) -> Unit,
    switchToInAppTranslation: () -> Unit,
    cancelTranslation: (Long) -> Unit,
    dismissBottomSheet: () -> Unit
) {
    ModalBottomSheet(
        modifier = Modifier.fillMaxWidth(),
        onDismissRequest = dismissBottomSheet,
        sheetGesturesEnabled = true
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            StyledText(
                text = stringResource(id = R.string.book_translation_google_rate_limited),
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                style = MaterialTheme.typography.titleLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                maxLines = 1
            )
            StyledText(
                text = stringResource(id = R.string.book_translation_google_rate_limited_desc),
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            BookTranslationRateLimitAction(
                index = 0,
                icon = Icons.Default.Refresh,
                title = stringResource(id = R.string.book_translation_retry),
                description = stringResource(id = R.string.book_translation_retry_rate_limited_desc),
                onClick = {
                    dismissBottomSheet()
                    retryTranslation(translation.id)
                }
            )

            if (canSwitchToInApp) {
                BookTranslationRateLimitAction(
                    index = 1,
                    icon = Icons.Default.Translate,
                    title = stringResource(id = R.string.book_translation_switch_to_in_app),
                    description = stringResource(id = R.string.book_translation_switch_to_in_app_desc),
                    onClick = {
                        dismissBottomSheet()
                        switchToInAppTranslation()
                    }
                )
            }

            BookTranslationRateLimitAction(
                index = 2,
                icon = Icons.Default.Cancel,
                title = stringResource(id = R.string.cancel),
                description = stringResource(id = R.string.book_translation_cancel_rate_limited_desc),
                onClick = {
                    dismissBottomSheet()
                    cancelTranslation(translation.id)
                }
            )
        }
    }
}

@Composable
private fun BookTranslationRateLimitAction(
    index: Int,
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .clip(MaterialTheme.shapes.extraLarge)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.dynamicListItemColor(index))
                .padding(10.dp)
                .size(22.dp),
            tint = MaterialTheme.colorScheme.onSurface
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            StyledText(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                maxLines = 1
            )
            StyledText(
                text = description,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                maxLines = 2
            )
        }
    }
}

@Composable
private fun BookTranslationInlineActions(
    runningTranslation: BookTranslation?,
    pausedTranslation: BookTranslation?,
    pauseTranslation: (Long) -> Unit,
    resumeTranslation: (Long) -> Unit,
    cancelTranslation: (Long) -> Unit
) {
    val translation = runningTranslation ?: pausedTranslation ?: return
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TextButton(
            modifier = Modifier.weight(1f),
            onClick = {
                if (runningTranslation != null) {
                    pauseTranslation(translation.id)
                } else {
                    resumeTranslation(translation.id)
                }
            }
        ) {
            Icon(
                imageVector = if (runningTranslation != null) {
                    Icons.Default.Pause
                } else {
                    Icons.Default.PlayArrow
                },
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            StyledText(
                text = if (runningTranslation != null) {
                    stringResource(id = R.string.book_translation_pause)
                } else {
                    stringResource(id = R.string.book_translation_resume)
                },
                style = MaterialTheme.typography.labelLarge
            )
        }

        TextButton(
            modifier = Modifier.weight(1f),
            onClick = { cancelTranslation(translation.id) }
        ) {
            Icon(
                imageVector = Icons.Default.Cancel,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            StyledText(
                text = stringResource(id = R.string.cancel),
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
private fun RetryBookTranslation(
    translation: BookTranslation,
    retryTranslation: (Long) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 8.dp),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.errorContainer
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            StyledText(
                text = stringResource(id = R.string.book_translation_failed),
                style = MaterialTheme.typography.titleSmall.copy(
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            )
            translation.errorMessage?.takeIf { it.isNotBlank() }?.let { message ->
                StyledText(
                    text = message,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                )
            }
            TextButton(onClick = { retryTranslation(translation.id) }) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                StyledText(
                    text = stringResource(id = R.string.book_translation_retry),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
private fun BookTranslationProviderOption(
    providerMode: TranslationProviderMode,
    changeProviderMode: (String) -> Unit
) {
    SegmentedButtonWithTitle(
        title = stringResource(id = R.string.translation_provider_option),
        buttons = TranslationFeature.AVAILABLE_FULL_BOOK_PROVIDER_MODES.map {
            ButtonItem(
                id = it.name,
                title = providerTitle(it),
                textStyle = MaterialTheme.typography.labelLarge,
                selected = it == providerMode
            )
        },
        onClick = { changeProviderMode(it.id) }
    )
}

@Composable
private fun BookTranslationLanguageSelector(
    bookId: Int?,
    providerMode: TranslationProviderMode,
    sourceLanguageCode: String,
    targetLanguageCode: String,
    changeSourceLanguage: (String) -> Unit,
    changeTargetLanguage: (String) -> Unit,
    swapLanguages: () -> Unit
) {
    val navigator = LocalNavigator.current
    val model = hiltViewModel<TranslatorSettingsModel>()
    val modelState = model.state.collectAsStateWithLifecycle()
    val languages = if (providerMode == TranslationProviderMode.IN_APP) {
        modelState.value.models.nativeTranslationLanguages()
    } else {
        provideGoogleTranslateLanguages()
    }

    LaunchedEffect(providerMode, languages, sourceLanguageCode, targetLanguageCode) {
        if (languages.isEmpty()) return@LaunchedEffect
        val supportedCodes = languages.mapTo(mutableSetOf()) { it.code }
        if (sourceLanguageCode != AUTO_TRANSLATION_LANGUAGE && sourceLanguageCode !in supportedCodes) {
            changeSourceLanguage(AUTO_TRANSLATION_LANGUAGE)
        }
        if (targetLanguageCode != DEVICE_TRANSLATION_LANGUAGE && targetLanguageCode !in supportedCodes) {
            changeTargetLanguage(DEVICE_TRANSLATION_LANGUAGE)
        }
    }

    TranslationLanguageSelector(
        sourceLanguageCode = sourceLanguageCode,
        targetLanguageCode = targetLanguageCode,
        languages = languages,
        allowAutoSource = true,
        allowDeviceTarget = true,
        onSourceLanguageClick = {
            navigator.push(
                TranslationLanguageSelectionScreen(
                    target = TranslationLanguageSelectionTarget.BOOK_TRANSLATION,
                    role = TranslationLanguageSelectionRole.SOURCE,
                    bookId = bookId
                )
            )
        },
        onTargetLanguageClick = {
            navigator.push(
                TranslationLanguageSelectionScreen(
                    target = TranslationLanguageSelectionTarget.BOOK_TRANSLATION,
                    role = TranslationLanguageSelectionRole.TARGET,
                    bookId = bookId
                )
            )
        },
        onSwapLanguages = swapLanguages
    )
}

@Composable
private fun BookTranslationError(
    message: String,
    dismissError: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.ErrorOutline,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        StyledText(
            text = message,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodySmall.copy(
                color = MaterialTheme.colorScheme.error
            )
        )
        TextButton(onClick = dismissError) {
            StyledText(
                text = stringResource(id = R.string.close),
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
private fun providerTitle(providerMode: TranslationProviderMode): String =
    when (providerMode) {
        TranslationProviderMode.IN_APP ->
            stringResource(id = R.string.translation_provider_in_app)

        TranslationProviderMode.GOOGLE_TRANSLATE ->
            stringResource(id = R.string.translation_provider_google_translate)

        TranslationProviderMode.EXTERNAL ->
            stringResource(id = R.string.translation_provider_external)
    }
