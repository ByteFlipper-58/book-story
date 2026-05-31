/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.ui.settings

import android.os.Parcelable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.byteflipper.everbook.R
import com.byteflipper.everbook.domain.config.AdRemoteConfigKeys
import com.byteflipper.everbook.domain.config.RemoteFeatureConfig
import com.byteflipper.everbook.domain.navigator.Screen
import com.byteflipper.everbook.presentation.core.components.common.LazyColumnWithScrollbar
import com.byteflipper.everbook.presentation.core.components.common.StyledText
import com.byteflipper.everbook.presentation.core.components.top_bar.collapsibleTopAppBarScrollBehavior
import com.byteflipper.everbook.presentation.navigator.LocalNavigator
import com.byteflipper.everbook.presentation.navigator.NavigatorBackIconButton
import com.byteflipper.everbook.presentation.settings.components.SettingsSubcategory
import com.byteflipper.everbook.presentation.settings.components.SettingsSubcategoryNote
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.parcelize.Parcelize
import javax.inject.Inject

@Parcelize
object DistributionDebugSettingsScreen : Screen, Parcelable {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val model = hiltViewModel<DistributionDebugSettingsModel>()
        val adsEnabled = model.adsEnabled.collectAsStateWithLifecycle()
        val readerEntryInterstitialAdsEnabled =
            model.readerEntryInterstitialAdsEnabled.collectAsStateWithLifecycle()
        val readerEntryInterstitialShowInterval =
            model.readerEntryInterstitialShowInterval.collectAsStateWithLifecycle()
        val readerNativeAdConfig =
            model.readerNativeAdConfig.collectAsStateWithLifecycle()
        val adSessionConfig =
            model.adSessionConfig.collectAsStateWithLifecycle()
        val (scrollBehavior, listState) = TopAppBarDefaults.collapsibleTopAppBarScrollBehavior()

        DistributionDebugSettingsContent(
            listState = listState,
            scrollBehavior = scrollBehavior,
            lines = listOf(
                "${AdRemoteConfigKeys.ADS_ENABLED} = ${adsEnabled.value}",
                "${AdRemoteConfigKeys.READER_ENTRY_INTERSTITIAL_ADS_ENABLED} = ${readerEntryInterstitialAdsEnabled.value}",
                "${AdRemoteConfigKeys.READER_ENTRY_INTERSTITIAL_SHOW_INTERVAL} = ${readerEntryInterstitialShowInterval.value}",
                "${AdRemoteConfigKeys.AD_GLOBAL_COOLDOWN_SECONDS} = ${adSessionConfig.value.globalCooldownSeconds}",
                "${AdRemoteConfigKeys.READER_NATIVE_ADS_ENABLED} = ${readerNativeAdConfig.value.enabled}",
                "${AdRemoteConfigKeys.READER_NATIVE_TEXT_FIRST_MIN_UNITS} = ${readerNativeAdConfig.value.textFirstMinUnits}",
                "${AdRemoteConfigKeys.READER_NATIVE_TEXT_NEXT_MIN_UNITS} = ${readerNativeAdConfig.value.textNextMinUnits}",
                "${AdRemoteConfigKeys.READER_NATIVE_TEXT_MAX_PER_SESSION} = ${readerNativeAdConfig.value.textMaxPerSession}",
                "${AdRemoteConfigKeys.READER_NATIVE_TEXT_END_GUARD_UNITS} = ${readerNativeAdConfig.value.textEndGuardUnits}",
                "${AdRemoteConfigKeys.READER_NATIVE_TEXT_LOOKAHEAD_UNITS} = ${readerNativeAdConfig.value.textLookaheadUnits}",
                "${AdRemoteConfigKeys.READER_NATIVE_PDF_FIRST_MIN_PAGES} = ${readerNativeAdConfig.value.pdfFirstMinPages}",
                "${AdRemoteConfigKeys.READER_NATIVE_PDF_NEXT_MIN_PAGES} = ${readerNativeAdConfig.value.pdfNextMinPages}",
                "${AdRemoteConfigKeys.READER_NATIVE_PDF_MAX_PER_SESSION} = ${readerNativeAdConfig.value.pdfMaxPerSession}",
                "${AdRemoteConfigKeys.READER_NATIVE_PDF_END_GUARD_PAGES} = ${readerNativeAdConfig.value.pdfEndGuardPages}",
                "${AdRemoteConfigKeys.READER_NATIVE_PDF_LOOKAHEAD_PAGES} = ${readerNativeAdConfig.value.pdfLookaheadPages}"
            ),
            navigateBack = {
                navigator.pop()
            }
        )
    }
}

@HiltViewModel
class DistributionDebugSettingsModel @Inject constructor(
    remoteFeatureConfig: RemoteFeatureConfig
) : ViewModel() {
    val adsEnabled = remoteFeatureConfig.adsEnabled
    val readerEntryInterstitialAdsEnabled = remoteFeatureConfig.readerEntryInterstitialAdsEnabled
    val readerEntryInterstitialShowInterval = remoteFeatureConfig.readerEntryInterstitialShowInterval
    val readerNativeAdConfig = remoteFeatureConfig.readerNativeAdConfig
    val adSessionConfig = remoteFeatureConfig.adSessionConfig
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DistributionDebugSettingsContent(
    listState: LazyListState,
    scrollBehavior: TopAppBarScrollBehavior,
    lines: List<String>,
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
                    StyledText(stringResource(id = R.string.ad_debug_settings))
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
                title = { stringResource(id = R.string.ad_debug_remote_config) },
                showTitle = true,
                showDivider = false
            ) {
                item {
                    SettingsSubcategoryNote(
                        text = lines.joinToString(separator = "\n"),
                        verticalPadding = 8.dp
                    )
                }
            }
        }
    }
}
