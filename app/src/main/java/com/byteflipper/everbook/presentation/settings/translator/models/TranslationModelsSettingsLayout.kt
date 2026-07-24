/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

@file:OptIn(
    androidx.compose.foundation.ExperimentalFoundationApi::class,
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class
)

package com.byteflipper.everbook.presentation.settings.translator.models
import androidx.compose.ui.res.painterResource
import androidx.annotation.DrawableRes

import android.util.Log
import androidx.compose.ui.platform.LocalContext
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.byteflipper.everbook.R
import com.byteflipper.everbook.domain.translation.TranslationModelState
import com.byteflipper.everbook.data.translation.isConnectedToValidatedWifi
import com.byteflipper.everbook.presentation.core.components.common.LazyColumnWithScrollbar
import com.byteflipper.everbook.presentation.core.components.common.SearchTextField
import com.byteflipper.everbook.presentation.core.components.common.StyledText
import com.byteflipper.everbook.presentation.settings.components.SettingsSubcategoryNote
import com.byteflipper.everbook.ui.main.MainModel
import com.byteflipper.everbook.ui.main.MainEvent
import com.byteflipper.everbook.presentation.translation.TranslationWifiRequiredBottomSheet
import com.byteflipper.everbook.ui.settings.TranslationModelFilter
import com.byteflipper.everbook.ui.settings.TranslatorSettingsEvent
import com.byteflipper.everbook.ui.settings.TranslatorSettingsModel

private const val TRANSLATION_MODELS_LOG = "TranslationModels"

@Composable
fun TranslationModelsSettingsLayout(
    listState: LazyListState,
    paddingValues: PaddingValues
) {
    val model = hiltViewModel<TranslatorSettingsModel>()
    val mainModel = hiltViewModel<MainModel>()
    val state = model.state.collectAsStateWithLifecycle()
    val mainState = mainModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var pendingWifiDownloadCode by remember { mutableStateOf<String?>(null) }

    pendingWifiDownloadCode?.let { languageCode ->
        TranslationWifiRequiredBottomSheet(
            wifiOnly = mainState.value.translationWifiOnly,
            onWifiOnlyChange = { enabled ->
                mainModel.onEvent(MainEvent.OnChangeTranslationWifiOnly(enabled))
            },
            onContinue = {
                model.onEvent(
                    TranslatorSettingsEvent.OnDownloadModel(
                        languageCode = languageCode,
                        requireWifi = false
                    )
                )
                pendingWifiDownloadCode = null
            },
            onDismiss = { pendingWifiDownloadCode = null }
        )
    }

    LazyColumnWithScrollbar(
        Modifier
            .fillMaxSize()
            .padding(top = paddingValues.calculateTopPadding()),
        state = listState,
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        if (state.value.modelManagerAvailable) {
            stickyHeader {
                TranslationModelsStickyControls(
                    query = state.value.query,
                    selectedFilter = state.value.filter,
                    onQueryChange = {
                        model.onEvent(TranslatorSettingsEvent.OnChangeModelSearchQuery(it))
                    },
                    onFilterChange = {
                        model.onEvent(TranslatorSettingsEvent.OnChangeModelFilter(it))
                    }
                )
            }
        }

        state.value.errorMessage?.let { error ->
            item {
                TranslationModelsError(
                    error = error,
                    onRetry = {
                        model.onEvent(TranslatorSettingsEvent.OnRefreshModels)
                    },
                    onDismiss = {
                        model.onEvent(TranslatorSettingsEvent.OnDismissModelError)
                    }
                )
            }
        }

        if (state.value.modelManagerAvailable) {
            item {
                SettingsSubcategoryNote(
                    text = stringResource(id = R.string.translation_model_download_help),
                    verticalPadding = 12.dp
                )
            }
            if (state.value.filteredModels.isEmpty() && !state.value.isLoadingModels) {
                item {
                    TranslationModelsEmpty()
                }
            }

            items(
                items = state.value.filteredModels,
                key = { it.language.code }
            ) { translationModel ->
                TranslationModelItem(
                    model = translationModel,
                    busy = translationModel.language.code in state.value.busyLanguageCodes,
                    requireWifi = mainState.value.translationWifiOnly,
                    onDownload = {
                        if (mainState.value.translationWifiOnly &&
                            !context.isConnectedToValidatedWifi()
                        ) {
                            pendingWifiDownloadCode = translationModel.language.code
                        } else {
                            model.onEvent(
                                TranslatorSettingsEvent.OnDownloadModel(
                                    languageCode = translationModel.language.code,
                                    requireWifi = mainState.value.translationWifiOnly
                                )
                            )
                        }
                    },
                    onDelete = {
                        model.onEvent(
                            TranslatorSettingsEvent.OnDeleteModel(
                                languageCode = translationModel.language.code
                            )
                        )
                    }
                )
            }
        } else {
            item {
                SettingsSubcategoryNote(
                    text = stringResource(id = R.string.translation_offline_models_unavailable),
                    verticalPadding = 12.dp
                )
            }
        }
    }
}
@Composable
private fun TranslationModelsStickyControls(
    query: String,
    selectedFilter: TranslationModelFilter,
    onQueryChange: (String) -> Unit,
    onFilterChange: (TranslationModelFilter) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column {
            TranslationModelsSearch(
                query = query,
                onQueryChange = onQueryChange
            )
            TranslationModelsFilters(
                selectedFilter = selectedFilter,
                onFilterChange = onFilterChange
            )
        }
    }
}

@Composable
private fun TranslationModelsSearch(
    query: String,
    onQueryChange: (String) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 8.dp),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainerHighest
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_search_rounded_24px),
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(12.dp))
            SearchTextField(
                modifier = Modifier.weight(1f),
                initialQuery = query,
                onQueryChange = onQueryChange,
                onSearch = {}
            )
        }
    }
}

@Composable
private fun TranslationModelsFilters(
    selectedFilter: TranslationModelFilter,
    onFilterChange: (TranslationModelFilter) -> Unit
) {
    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TranslationModelFilter.entries.forEach { filter ->
            FilterChip(
                selected = filter == selectedFilter,
                onClick = { onFilterChange(filter) },
                label = {
                    StyledText(
                        text = stringResource(
                            id = when (filter) {
                                TranslationModelFilter.ALL ->
                                    R.string.translation_models_filter_all

                                TranslationModelFilter.DOWNLOADED ->
                                    R.string.translation_models_filter_downloaded

                                TranslationModelFilter.NOT_DOWNLOADED ->
                                    R.string.translation_models_filter_not_downloaded
                            }
                        ),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            )
        }
    }
}

@Composable
private fun LazyItemScope.TranslationModelItem(
    model: TranslationModelState,
    busy: Boolean,
    requireWifi: Boolean,
    onDownload: () -> Unit,
    onDelete: () -> Unit
) {
    val enabled = model.supported && !busy
    val action = if (model.downloaded) onDelete else onDownload

    Column(
        modifier = Modifier
            .animateItem()
            .fillMaxWidth()
            .clickable(enabled = enabled) {
                Log.i(
                    TRANSLATION_MODELS_LOG,
                    "Model row clicked: language=${model.language.code} " +
                            "downloaded=${model.downloaded} supported=${model.supported} " +
                            "busy=$busy wifiOnly=$requireWifi " +
                            "operation=${if (model.downloaded) "delete" else "download"}"
                )
                action()
            }
            .padding(horizontal = 18.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TranslationModelStatusIcon(
                downloaded = model.downloaded,
                supported = model.supported,
                busy = busy
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                StyledText(
                    text = model.language.name,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                StyledText(
                    text = stringResource(
                        id = when {
                            !model.supported -> R.string.translation_model_unsupported
                            busy -> R.string.translation_model_busy
                            model.downloaded -> R.string.translation_model_downloaded
                            requireWifi -> R.string.translation_model_not_downloaded_wifi
                            else -> R.string.translation_model_not_downloaded
                        }
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }

            TranslationModelActionIcon(
                downloaded = model.downloaded,
                supported = model.supported,
                busy = busy
            )
        }
    }
    HorizontalDivider(modifier = Modifier.padding(start = 58.dp, end = 18.dp))
}

@Composable
private fun TranslationModelStatusIcon(
    downloaded: Boolean,
    supported: Boolean,
    busy: Boolean
) {
    AnimatedContent(
        targetState = busy,
        label = "TranslationModelStatusIcon"
    ) { isBusy ->
        if (isBusy) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp
            )
        } else {
            Icon(
                painter = when {
                    !supported -> painterResource(R.drawable.ic_error_rounded_24px)
                    downloaded -> painterResource(R.drawable.ic_check_circle_rounded_24px)
                    else -> painterResource(R.drawable.ic_cloud_download_rounded_24px)
                },
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = when {
                    !supported -> MaterialTheme.colorScheme.error
                    downloaded -> MaterialTheme.colorScheme.secondary
                    else -> MaterialTheme.colorScheme.tertiary
                }
            )
        }
    }
}

@Composable
private fun TranslationModelActionIcon(
    downloaded: Boolean,
    supported: Boolean,
    busy: Boolean
) {
    if (!supported || busy) return

    TranslationModelIcon(
        icon = if (downloaded) {
            R.drawable.ic_delete_rounded_24px
        } else {
            R.drawable.ic_download_for_offline_rounded_24px
        },
        tint = if (downloaded) {
            MaterialTheme.colorScheme.error
        } else {
            MaterialTheme.colorScheme.primary
        }
    )
}

@Composable
private fun TranslationModelIcon(
    @androidx.annotation.DrawableRes icon: Int,
    tint: androidx.compose.ui.graphics.Color
) {
    Icon(
        painter = androidx.compose.ui.res.painterResource(id = icon),
        contentDescription = null,
        modifier = Modifier.size(22.dp),
        tint = tint
    )
}

@Composable
private fun TranslationModelsEmpty() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StyledText(
            text = stringResource(id = R.string.translation_models_empty),
            style = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }
}

@Composable
private fun TranslationModelsError(
    error: String,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        StyledText(
            text = error,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodySmall.copy(
                color = MaterialTheme.colorScheme.error
            )
        )
        TextButton(onClick = onRetry) {
            StyledText(text = stringResource(id = R.string.retry))
        }
        TextButton(onClick = onDismiss) {
            StyledText(text = stringResource(id = R.string.close))
        }
    }
}
