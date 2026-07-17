/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.reader
import androidx.compose.ui.res.painterResource
import androidx.annotation.DrawableRes

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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.byteflipper.everbook.R
import com.byteflipper.everbook.domain.translation.AUTO_TRANSLATION_LANGUAGE
import com.byteflipper.everbook.domain.translation.BookTranslation
import com.byteflipper.everbook.domain.translation.BookTranslationStatus
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
import com.byteflipper.everbook.ui.reader.ReaderEvent
import com.byteflipper.everbook.ui.reader.ReaderModel
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
    var showTranslationsSheet by remember { mutableStateOf(false) }

    if (state.showGoogleWarning) {
        ModalBottomSheet(
            onDismissRequest = dismissGoogleWarning,
            sheetGesturesEnabled = true
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_warning_rounded_24px),
                    contentDescription = null,
                    modifier = Modifier
                        .size(32.dp)
                        .align(Alignment.CenterHorizontally),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                StyledText(
                    text = stringResource(id = R.string.book_translation_google_warning_title),
                    style = MaterialTheme.typography.headlineSmall.copy(
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.fillMaxWidth()
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
                    pauseTranslation = pauseTranslation,
                    resumeTranslation = resumeTranslation,
                    switchToInAppTranslation = {
                        changeProviderMode(TranslationProviderMode.IN_APP.name)
                    },
                    openTranslationsManager = {
                        showTranslationsSheet = true
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
        }
    }

    if (showTranslationsSheet) {
        BookTranslationsManagerBottomSheet(
            translations = state.allTranslations,
            activeTranslationId = state.currentTranslation?.id,
            dismissBottomSheet = { showTranslationsSheet = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BookTranslationActionButton(
    state: ReaderBookTranslationState,
    retryTranslation: (Long) -> Unit,
    cancelTranslation: (Long) -> Unit,
    pauseTranslation: (Long) -> Unit,
    resumeTranslation: (Long) -> Unit,
    switchToInAppTranslation: () -> Unit,
    openTranslationsManager: () -> Unit,
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

    // While a translation is running or paused, the main button becomes the control: tap the
    // leading icon to pause/resume, the trailing icon to cancel. Progress sits in the middle with
    // the fill behind. Replaces the separate pause/cancel button row.
    if (!isStarting && !isApplying && (busyTranslation != null || pausedTranslation != null)) {
        BookTranslationProgressControl(
            control = busyTranslation ?: pausedTranslation!!,
            isRunning = busyTranslation != null,
            containerColor = containerColor,
            contentColor = contentColor,
            progressColor = progressColor,
            pauseTranslation = pauseTranslation,
            resumeTranslation = resumeTranslation,
            cancelTranslation = cancelTranslation
        )
        return
    }

    // Split-button: show a narrow "tail" with the translations count + chevron when the book has
    // any existing translations AND the main button is in a tap-to-act state (not while a worker
    // is running/paused — that state already uses the in-place pause/cancel controls).
    val showManagerTail = state.allTranslations.isNotEmpty() &&
            !isStarting && !isApplying && busyTranslation == null && pausedTranslation == null
    val mainShape = if (showManagerTail) {
        RoundedCornerShape(topStart = 26.dp, bottomStart = 26.dp, topEnd = 8.dp, bottomEnd = 8.dp)
    } else {
        MaterialTheme.shapes.extraLarge
    }
    val tailShape = RoundedCornerShape(
        topStart = 8.dp, bottomStart = 8.dp, topEnd = 26.dp, bottomEnd = 26.dp
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 12.dp)
            .height(52.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
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
                .weight(1f)
                .fillMaxHeight(),
            shape = mainShape,
            color = containerColor,
            contentColor = contentColor
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            if (progressTranslation != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progressTranslation.progress.coerceIn(0f, 1f))
                        .fillMaxHeight()
                        .align(Alignment.CenterStart)
                        .background(progressColor)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
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
                        painter = painterResource(R.drawable.ic_play_arrow_rounded_24px),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                } else if (rateLimitedTranslation != null) {
                    Icon(
                        painter = painterResource(R.drawable.ic_warning_rounded_24px),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                } else if (progressTranslation == null && state.isBookTextReadyForTranslation) {
                    Icon(
                        painter = painterResource(R.drawable.ic_translate_rounded_24px),
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

        if (showManagerTail) {
            Surface(
                onClick = openTranslationsManager,
                modifier = Modifier.fillMaxHeight(),
                shape = tailShape,
                color = containerColor,
                contentColor = contentColor
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_translate_rounded_24px),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = contentColor
                    )
                    Icon(
                        painter = painterResource(R.drawable.ic_keyboard_arrow_up_rounded_24px),
                        contentDescription = stringResource(
                            id = R.string.book_translation_manager_title
                        ),
                        modifier = Modifier.size(18.dp),
                        tint = contentColor
                    )
                }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BookTranslationsManagerBottomSheet(
    translations: List<BookTranslation>,
    activeTranslationId: Long?,
    dismissBottomSheet: () -> Unit
) {
    // Reuse the reader's shared ReaderModel (same instance the bottom sheet is driven by) so the
    // mini-sheet can switch/delete translations without threading callbacks through the chain.
    val readerModel = hiltViewModel<ReaderModel>()
    var pendingDelete by remember { mutableStateOf<BookTranslation?>(null) }

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
                text = stringResource(id = R.string.book_translation_manager_title),
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                style = MaterialTheme.typography.titleLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(4.dp))

            translations.forEach { translation ->
                BookTranslationManagerRow(
                    translation = translation,
                    isActive = translation.id == activeTranslationId,
                    onSelect = {
                        readerModel.onEvent(
                            ReaderEvent.OnSelectBookTranslation(translation.id)
                        )
                        dismissBottomSheet()
                    },
                    onDelete = { pendingDelete = translation }
                )
            }
        }
    }

    pendingDelete?.let { target ->
        BasicAlertDialog(onDismissRequest = { pendingDelete = null }) {
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
                        text = stringResource(
                            id = R.string.book_translation_manager_delete_confirm_title
                        ),
                        style = MaterialTheme.typography.headlineSmall
                    )
                    StyledText(
                        text = stringResource(
                            id = R.string.book_translation_manager_delete_confirm_desc,
                            target.sourceLanguageCode ?: AUTO_TRANSLATION_LANGUAGE,
                            target.targetLanguageCode,
                            providerTitle(target.providerMode)
                        ),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { pendingDelete = null }) {
                            StyledText(text = stringResource(id = R.string.cancel))
                        }
                        TextButton(
                            onClick = {
                                readerModel.onEvent(
                                    ReaderEvent.OnDeleteBookTranslation(target.id)
                                )
                                pendingDelete = null
                            }
                        ) {
                            StyledText(
                                text = stringResource(
                                    id = R.string.book_translation_manager_delete
                                ),
                                style = MaterialTheme.typography.labelLarge.copy(
                                    color = MaterialTheme.colorScheme.error
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BookTranslationManagerRow(
    translation: BookTranslation,
    isActive: Boolean,
    onSelect: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 2.dp)
            .clip(MaterialTheme.shapes.extraLarge)
            .clickable(onClick = onSelect)
            .padding(start = 16.dp, end = 4.dp, top = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                StyledText(
                    text = "${(translation.sourceLanguageCode ?: AUTO_TRANSLATION_LANGUAGE)
                        .uppercase()} → ${translation.targetLanguageCode.uppercase()}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                if (isActive) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        painter = painterResource(R.drawable.ic_check_circle_rounded_24px),
                        contentDescription = stringResource(
                            id = R.string.book_translation_manager_active
                        ),
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            StyledText(
                text = managerRowSubtitle(translation),
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
        IconButton(onClick = onDelete) {
            Icon(
                painter = painterResource(R.drawable.ic_delete_rounded_24px),
                contentDescription = stringResource(
                    id = R.string.book_translation_manager_delete
                ),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun managerRowSubtitle(translation: BookTranslation): String {
    val provider = providerTitle(translation.providerMode)
    val status = stringResource(id = managerStatusRes(translation.status))
    val progress = if (
        translation.totalUnits > 0 &&
        translation.status != BookTranslationStatus.COMPLETED
    ) {
        " · ${translation.completedUnits}/${translation.totalUnits}"
    } else ""
    return "$provider · $status$progress"
}

private fun managerStatusRes(status: BookTranslationStatus): Int = when (status) {
    BookTranslationStatus.QUEUED, BookTranslationStatus.PENDING ->
        R.string.book_translation_status_queued
    BookTranslationStatus.RUNNING -> R.string.book_translation_status_running
    BookTranslationStatus.PAUSED -> R.string.book_translation_status_paused
    BookTranslationStatus.COMPLETED -> R.string.book_translation_status_completed
    BookTranslationStatus.FAILED -> R.string.book_translation_status_failed
    BookTranslationStatus.CANCELLED -> R.string.book_translation_status_cancelled
    BookTranslationStatus.STALE -> R.string.book_translation_status_stale
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
                icon = R.drawable.ic_refresh_rounded_24px,
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
                    icon = R.drawable.ic_translate_rounded_24px,
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
                icon = R.drawable.ic_cancel_rounded_24px,
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
    @androidx.annotation.DrawableRes icon: Int,
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
            painter = androidx.compose.ui.res.painterResource(id = icon),
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
private fun BookTranslationProgressControl(
    control: BookTranslation,
    isRunning: Boolean,
    containerColor: Color,
    contentColor: Color,
    progressColor: Color,
    pauseTranslation: (Long) -> Unit,
    resumeTranslation: (Long) -> Unit,
    cancelTranslation: (Long) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 12.dp)
            .height(52.dp),
        shape = MaterialTheme.shapes.extraLarge,
        color = containerColor,
        contentColor = contentColor
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(control.progress.coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .align(Alignment.CenterStart)
                    .background(progressColor)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        if (isRunning) {
                            pauseTranslation(control.id)
                        } else {
                            resumeTranslation(control.id)
                        }
                    }
                ) {
                    Icon(
                        painter = if (isRunning) {
                            painterResource(R.drawable.ic_pause_rounded_24px)
                        } else {
                            painterResource(R.drawable.ic_play_arrow_rounded_24px)
                        },
                        contentDescription = stringResource(
                            id = if (isRunning) {
                                R.string.book_translation_pause
                            } else {
                                R.string.book_translation_resume
                            }
                        ),
                        tint = contentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                StyledText(
                    text = stringResource(
                        id = R.string.book_translation_progress,
                        control.completedUnits,
                        control.totalUnits
                    ),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = contentColor,
                        textAlign = TextAlign.Center
                    ),
                    maxLines = 1
                )
                IconButton(onClick = { cancelTranslation(control.id) }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_cancel_rounded_24px),
                        contentDescription = stringResource(id = R.string.cancel),
                        tint = contentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
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
                    painter = painterResource(R.drawable.ic_refresh_rounded_24px),
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
            painter = painterResource(R.drawable.ic_error_rounded_24px),
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
