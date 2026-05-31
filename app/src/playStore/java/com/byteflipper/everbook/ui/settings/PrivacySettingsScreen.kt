/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.ui.settings

import android.os.Parcelable
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.byteflipper.everbook.R
import com.byteflipper.everbook.domain.navigator.Screen
import com.byteflipper.everbook.domain.privacy.PrivacyConsentManager
import com.byteflipper.everbook.presentation.core.components.common.LazyColumnWithScrollbar
import com.byteflipper.everbook.presentation.core.components.common.StyledText
import com.byteflipper.everbook.presentation.core.components.top_bar.collapsibleTopAppBarScrollBehavior
import com.byteflipper.everbook.presentation.core.util.LocalActivity
import com.byteflipper.everbook.presentation.core.util.showToast
import com.byteflipper.everbook.presentation.navigator.LocalNavigator
import com.byteflipper.everbook.presentation.navigator.NavigatorBackIconButton
import com.byteflipper.everbook.presentation.settings.components.SettingsSubcategory
import com.byteflipper.everbook.presentation.settings.components.SettingsSubcategoryNote
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.parcelize.Parcelize
import javax.inject.Inject

@Parcelize
object PrivacySettingsScreen : Screen, Parcelable {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val activity = LocalActivity.current
        val model = hiltViewModel<PrivacySettingsModel>()
        val state = model.state.collectAsStateWithLifecycle()
        val privacyOptionsRequired =
            model.privacyOptionsRequired.collectAsStateWithLifecycle()
        val (scrollBehavior, listState) = TopAppBarDefaults.collapsibleTopAppBarScrollBehavior()

        LaunchedEffect(activity) {
            model.refresh(activity)
        }

        PrivacySettingsContent(
            listState = listState,
            scrollBehavior = scrollBehavior,
            isLoading = state.value.isLoading,
            canRequestAds = state.value.canRequestAds,
            privacyOptionsRequired = privacyOptionsRequired.value,
            refresh = {
                model.refresh(activity)
            },
            showPrivacyOptions = {
                model.showPrivacyOptions(
                    activity = activity,
                    onMessage = { message ->
                        message.showToast(activity, longToast = false)
                    }
                )
            },
            navigateBack = {
                navigator.pop()
            }
        )
    }
}

data class PrivacySettingsState(
    val isLoading: Boolean = false,
    val canRequestAds: Boolean = false
)

@HiltViewModel
class PrivacySettingsModel @Inject constructor(
    private val privacyConsentManager: PrivacyConsentManager
) : ViewModel() {
    private val _state = MutableStateFlow(PrivacySettingsState())

    val state: StateFlow<PrivacySettingsState> = _state
    val privacyOptionsRequired = privacyConsentManager.privacyOptionsRequired

    fun refresh(activity: ComponentActivity) {
        if (_state.value.isLoading) return

        _state.update { it.copy(isLoading = true) }
        privacyConsentManager.refreshPrivacyOptionsRequirement(activity) {
            _state.update {
                it.copy(
                    isLoading = false,
                    canRequestAds = privacyConsentManager.canRequestAds(activity)
                )
            }
        }
    }

    fun showPrivacyOptions(
        activity: ComponentActivity,
        onMessage: (String) -> Unit
    ) {
        if (_state.value.isLoading) return

        _state.update { it.copy(isLoading = true) }
        privacyConsentManager.showPrivacyOptions(activity) { errorMessage ->
            _state.update {
                it.copy(
                    isLoading = false,
                    canRequestAds = privacyConsentManager.canRequestAds(activity)
                )
            }
            onMessage(
                errorMessage ?: activity.getString(R.string.privacy_options_updated)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PrivacySettingsContent(
    listState: LazyListState,
    scrollBehavior: TopAppBarScrollBehavior,
    isLoading: Boolean,
    canRequestAds: Boolean,
    privacyOptionsRequired: Boolean,
    refresh: () -> Unit,
    showPrivacyOptions: () -> Unit,
    navigateBack: () -> Unit
) {
    Scaffold(
        Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .windowInsetsPadding(WindowInsets.navigationBars),
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            LargeTopAppBar(
                title = {
                    StyledText(stringResource(id = R.string.privacy_settings))
                },
                navigationIcon = {
                    NavigatorBackIconButton(navigateBack = navigateBack)
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        }
    ) { paddingValues ->
        LazyColumnWithScrollbar(
            Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding()),
            state = listState,
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            SettingsSubcategory(
                titleColor = { MaterialTheme.colorScheme.onSurface },
                title = { stringResource(id = R.string.privacy_ads_subcategory) },
                showTitle = true,
                showDivider = false
            ) {
                item {
                    PrivacyOptionsItem(
                        isLoading = isLoading,
                        canRequestAds = canRequestAds,
                        privacyOptionsRequired = privacyOptionsRequired,
                        refresh = refresh,
                        showPrivacyOptions = showPrivacyOptions
                    )
                }
                item {
                    SettingsSubcategoryNote(
                        text = stringResource(
                            id = if (privacyOptionsRequired) {
                                R.string.privacy_options_required_note
                            } else {
                                R.string.privacy_options_not_required_note
                            }
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun PrivacyOptionsItem(
    isLoading: Boolean,
    canRequestAds: Boolean,
    privacyOptionsRequired: Boolean,
    refresh: () -> Unit,
    showPrivacyOptions: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        StyledText(
            text = stringResource(id = R.string.privacy_ads_consent_title),
            style = MaterialTheme.typography.titleMedium.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        StyledText(
            text = stringResource(
                id = if (canRequestAds) {
                    R.string.privacy_ads_consent_desc_ready
                } else {
                    R.string.privacy_ads_consent_desc_limited
                }
            ),
            style = MaterialTheme.typography.bodySmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                enabled = !isLoading,
                onClick = refresh
            ) {
                Icon(
                    imageVector = Icons.Outlined.Refresh,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = stringResource(id = R.string.refresh))
            }
            Spacer(modifier = Modifier.width(8.dp))
            FilledTonalButton(
                enabled = privacyOptionsRequired && !isLoading,
                onClick = showPrivacyOptions
            ) {
                Icon(
                    imageVector = Icons.Outlined.Tune,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = stringResource(id = R.string.privacy_options_manage))
            }
        }
    }
}
