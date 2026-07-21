/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.ui.settings

import android.os.Parcelable
import androidx.activity.ComponentActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyListState
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.byteflipper.everbook.BuildConfig
import com.byteflipper.everbook.R
import com.byteflipper.everbook.data.ads.AdMobAppOpenAdManager
import com.byteflipper.everbook.domain.navigator.Screen
import com.byteflipper.everbook.domain.privacy.PrivacyConsentManager
import com.google.android.gms.ads.MobileAds
import com.byteflipper.everbook.presentation.core.components.common.LazyColumnWithScrollbar
import com.byteflipper.everbook.presentation.core.components.common.PrivacyPolicyWebView
import com.byteflipper.everbook.presentation.core.components.common.StyledText
import com.byteflipper.everbook.presentation.core.components.modal_bottom_sheet.ModalBottomSheet
import com.byteflipper.everbook.presentation.core.components.settings.SwitchWithTitle
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
        val appOpenAdsEnabled = model.appOpenAdsEnabled.collectAsStateWithLifecycle()
        var showAdInspector by rememberSaveable { mutableStateOf(BuildConfig.DEBUG) }
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
            appOpenAdsEnabled = appOpenAdsEnabled.value,
            showAdInspector = showAdInspector,
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
            openAdInspector = {
                MobileAds.openAdInspector(activity) { error ->
                    error?.message?.showToast(activity, longToast = false)
                }
            },
            setAppOpenAdsEnabled = model::setAppOpenAdsEnabled,
            unlockAdInspector = { showAdInspector = true },
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
    private val privacyConsentManager: PrivacyConsentManager,
    private val appOpenAdManager: AdMobAppOpenAdManager
) : ViewModel() {
    private val _state = MutableStateFlow(PrivacySettingsState())

    val state: StateFlow<PrivacySettingsState> = _state
    val privacyOptionsRequired = privacyConsentManager.privacyOptionsRequired
    val appOpenAdsEnabled = appOpenAdManager.isEnabled

    fun setAppOpenAdsEnabled(enabled: Boolean) {
        appOpenAdManager.setEnabled(enabled)
    }

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
    appOpenAdsEnabled: Boolean,
    showAdInspector: Boolean,
    refresh: () -> Unit,
    showPrivacyOptions: () -> Unit,
    openAdInspector: () -> Unit,
    setAppOpenAdsEnabled: (Boolean) -> Unit,
    unlockAdInspector: () -> Unit,
    navigateBack: () -> Unit
) {
    val showPrivacyPolicy = remember { mutableStateOf(false) }

    if (showPrivacyPolicy.value) {
        PrivacyPolicyBottomSheet(
            dismissBottomSheet = {
                showPrivacyPolicy.value = false
            }
        )
    }

    Scaffold(
        Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .windowInsetsPadding(WindowInsets.navigationBars),
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            LargeTopAppBar(
                title = {
                    Box(
                        modifier = Modifier.pointerInput(Unit) {
                            detectTapGestures(onLongPress = { unlockAdInspector() })
                        }
                    ) {
                        StyledText(stringResource(id = R.string.privacy_settings))
                    }
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
            state = listState
        ) {
            SettingsSubcategory(
                titleColor = { MaterialTheme.colorScheme.primary },
                title = { stringResource(id = R.string.privacy_ads_subcategory) },
                showTitle = true,
                showDivider = true
            ) {
                item {
                    PrivacyOptionsItem(
                        isLoading = isLoading,
                        canRequestAds = canRequestAds,
                        privacyOptionsRequired = privacyOptionsRequired,
                        showAdInspector = showAdInspector,
                        refresh = refresh,
                        showPrivacyOptions = showPrivacyOptions,
                        openAdInspector = openAdInspector
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
                item {
                    AppOpenAdsItem(
                        enabled = appOpenAdsEnabled,
                        onEnabledChange = setAppOpenAdsEnabled
                    )
                }
            }
            SettingsSubcategory(
                titleColor = { MaterialTheme.colorScheme.primary },
                title = { stringResource(id = R.string.privacy_policy_subcategory) },
                showTitle = true,
                showDivider = false
            ) {
                item {
                    PrivacyPolicyItem(
                        showPrivacyPolicy = {
                            showPrivacyPolicy.value = true
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AppOpenAdsItem(
    enabled: Boolean,
    onEnabledChange: (Boolean) -> Unit
) {
    SwitchWithTitle(
        selected = enabled,
        title = stringResource(R.string.app_open_ads_title),
        description = stringResource(R.string.app_open_ads_desc),
        onClick = { onEnabledChange(!enabled) }
    )
}

@Composable
private fun PrivacyOptionsItem(
    isLoading: Boolean,
    canRequestAds: Boolean,
    privacyOptionsRequired: Boolean,
    showAdInspector: Boolean,
    refresh: () -> Unit,
    showPrivacyOptions: () -> Unit,
    openAdInspector: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
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
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.End
        ) {
            OutlinedButton(
                enabled = !isLoading,
                onClick = refresh,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_refresh_rounded_24px),
                    contentDescription = null
                )
                Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                Text(text = stringResource(id = R.string.refresh))
            }
            FilledTonalButton(
                enabled = privacyOptionsRequired && !isLoading,
                onClick = showPrivacyOptions,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_tune_rounded_24px),
                    contentDescription = null
                )
                Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                Text(text = stringResource(id = R.string.privacy_options_manage))
            }
            if (showAdInspector) {
                OutlinedButton(
                    enabled = !isLoading,
                    onClick = openAdInspector,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = stringResource(id = R.string.ad_inspector_open))
                }
            }
        }
    }
}

@Composable
private fun PrivacyPolicyItem(
    showPrivacyPolicy: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showPrivacyPolicy() }
            .padding(horizontal = 18.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            StyledText(
                text = stringResource(id = R.string.privacy_policy_title),
                style = MaterialTheme.typography.titleMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            StyledText(
                text = stringResource(id = R.string.privacy_policy_desc),
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

@Composable
private fun PrivacyPolicyBottomSheet(
    dismissBottomSheet: () -> Unit
) {
    ModalBottomSheet(
        modifier = Modifier.fillMaxHeight(0.94f),
        hasFixedHeight = true,
        onDismissRequest = dismissBottomSheet,
        sheetGesturesEnabled = false,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp)
                .padding(bottom = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StyledText(
                text = stringResource(id = R.string.privacy_policy_title),
                style = MaterialTheme.typography.titleLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                PrivacyPolicyWebView(modifier = Modifier.fillMaxSize())
            }
        }
    }
}
