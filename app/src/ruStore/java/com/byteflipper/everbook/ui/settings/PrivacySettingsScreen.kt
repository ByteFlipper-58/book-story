/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.ui.settings

import android.os.Parcelable
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.byteflipper.everbook.R
import com.byteflipper.everbook.data.privacy.RuStorePrivacyConsentManager
import com.byteflipper.everbook.domain.navigator.Screen
import com.byteflipper.everbook.presentation.core.components.common.LazyColumnWithScrollbar
import com.byteflipper.everbook.presentation.core.components.common.PrivacyPolicyWebView
import com.byteflipper.everbook.presentation.core.components.common.StyledText
import com.byteflipper.everbook.presentation.core.components.modal_bottom_sheet.ModalBottomSheet
import com.byteflipper.everbook.presentation.core.components.top_bar.collapsibleTopAppBarScrollBehavior
import com.byteflipper.everbook.presentation.navigator.LocalNavigator
import com.byteflipper.everbook.presentation.navigator.NavigatorBackIconButton
import com.byteflipper.everbook.presentation.settings.components.SettingsSubcategory
import com.byteflipper.everbook.presentation.settings.components.SettingsSubcategoryNote
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.parcelize.Parcelize
import javax.inject.Inject

@Parcelize
object PrivacySettingsScreen : Screen, Parcelable {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val model = hiltViewModel<PrivacySettingsModel>()
        val personalizedAds = model.personalizedAdsEnabled.collectAsStateWithLifecycle()
        val (scrollBehavior, listState) = TopAppBarDefaults.collapsibleTopAppBarScrollBehavior()

        PrivacySettingsContent(
            listState = listState,
            scrollBehavior = scrollBehavior,
            personalizedAdsEnabled = personalizedAds.value,
            setPersonalizedAds = model::setPersonalizedAds,
            navigateBack = { navigator.pop() }
        )
    }
}

@HiltViewModel
class PrivacySettingsModel @Inject constructor(
    private val privacyConsentManager: RuStorePrivacyConsentManager
) : ViewModel() {
    val personalizedAdsEnabled = privacyConsentManager.personalizedAdsEnabled

    fun setPersonalizedAds(enabled: Boolean) {
        privacyConsentManager.setPersonalizedAds(enabled)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PrivacySettingsContent(
    listState: LazyListState,
    scrollBehavior: TopAppBarScrollBehavior,
    personalizedAdsEnabled: Boolean,
    setPersonalizedAds: (Boolean) -> Unit,
    navigateBack: () -> Unit
) {
    val showPrivacyPolicy = remember { mutableStateOf(false) }

    if (showPrivacyPolicy.value) {
        PrivacyPolicyBottomSheet(
            dismissBottomSheet = { showPrivacyPolicy.value = false }
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
            state = listState
        ) {
            SettingsSubcategory(
                titleColor = { MaterialTheme.colorScheme.primary },
                title = { stringResource(id = R.string.privacy_ads_subcategory) },
                showTitle = true,
                showDivider = true
            ) {
                item {
                    AdPersonalizationItem(
                        personalizedAdsEnabled = personalizedAdsEnabled,
                        setPersonalizedAds = setPersonalizedAds
                    )
                }
                item {
                    SettingsSubcategoryNote(
                        text = stringResource(id = R.string.privacy_ads_personalization_note)
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
                        showPrivacyPolicy = { showPrivacyPolicy.value = true }
                    )
                }
            }
        }
    }
}

@Composable
private fun AdPersonalizationItem(
    personalizedAdsEnabled: Boolean,
    setPersonalizedAds: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { setPersonalizedAds(!personalizedAdsEnabled) }
            .padding(horizontal = 18.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            StyledText(
                text = stringResource(id = R.string.privacy_ads_personalization_title),
                style = MaterialTheme.typography.titleMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            StyledText(
                text = stringResource(id = R.string.privacy_ads_personalization_desc),
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Switch(
            checked = personalizedAdsEnabled,
            onCheckedChange = setPersonalizedAds
        )
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
