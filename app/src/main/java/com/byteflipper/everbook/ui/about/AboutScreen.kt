/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.ui.about

import android.os.Parcelable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NewReleases
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.byteflipper.everbook.R
import com.byteflipper.everbook.domain.navigator.Screen
import com.byteflipper.everbook.presentation.about.AboutContent
import com.byteflipper.everbook.presentation.core.components.common.StyledText
import com.byteflipper.everbook.presentation.core.components.modal_bottom_sheet.ModalBottomSheet
import com.byteflipper.everbook.presentation.core.components.top_bar.collapsibleTopAppBarScrollBehavior
import com.byteflipper.everbook.presentation.core.util.LocalActivity
import com.byteflipper.everbook.presentation.navigator.LocalNavigator
import com.byteflipper.everbook.ui.changelog.ChangelogScreen
import com.byteflipper.everbook.ui.credits.CreditsScreen
import com.byteflipper.everbook.ui.licenses.LicensesScreen
import kotlinx.parcelize.Parcelize

@Parcelize
object AboutScreen : Screen, Parcelable {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val activity = LocalActivity.current
        val screenModel = hiltViewModel<AboutModel>()
        val state by screenModel.state.collectAsStateWithLifecycle()

        val (scrollBehavior, listState) = TopAppBarDefaults.collapsibleTopAppBarScrollBehavior()

        // Update available bottom sheet (everbook only)
        state.updateSheet?.let { sheet ->
            UpdateAvailableBottomSheet(
                version = sheet.version,
                onUpdate = { screenModel.startUpdate(activity) },
                onDismiss = { screenModel.dismissUpdateSheet() }
            )
        }

        AboutContent(
            scrollBehavior = scrollBehavior,
            listState = listState,
            isCheckingUpdate = state.isCheckingUpdate,
            canLeaveReview = state.canLeaveReview,
            navigateToBrowserPage = screenModel::onEvent,
            onCheckForUpdate = { event -> screenModel.onEvent(event) },
            onLeaveReview = { event -> screenModel.onEvent(event) },
            navigateToLicenses = { navigator.push(LicensesScreen) },
            navigateToCredits = { navigator.push(CreditsScreen) },
            navigateToChangelog = { navigator.push(ChangelogScreen()) },
            navigateBack = { navigator.pop() }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UpdateAvailableBottomSheet(
    version: String,
    onUpdate: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetGesturesEnabled = true,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.NewReleases,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                StyledText(
                    text = stringResource(id = R.string.update_available_title),
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
            StyledText(
                text = stringResource(id = R.string.update_available_desc),
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            StyledText(
                text = stringResource(id = R.string.update_available_version, version),
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.primary
                )
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
            ) {
                TextButton(onClick = onDismiss) {
                    Text(text = stringResource(id = R.string.later))
                }
                FilledTonalButton(onClick = onUpdate) {
                    Text(text = stringResource(id = R.string.update_action))
                }
            }
        }
    }
}
