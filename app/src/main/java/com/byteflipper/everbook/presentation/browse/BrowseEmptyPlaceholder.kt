/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.browse

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.byteflipper.everbook.R
import com.byteflipper.everbook.presentation.core.components.common.AnimatedVisibility
import com.byteflipper.everbook.presentation.core.components.placeholder.EmptyPlaceholder
import com.byteflipper.everbook.presentation.core.components.placeholder.ErrorPlaceholder
import com.byteflipper.everbook.ui.browse.BrowseEvent
import com.byteflipper.everbook.ui.theme.Transitions

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun BoxScope.BrowseEmptyPlaceholder(
    filesEmpty: Boolean,
    dialogHidden: Boolean,
    isError: Boolean,
    isLoading: Boolean,
    isRefreshing: Boolean,
    storagePermissionState: PermissionState,
    permissionCheck: (BrowseEvent.OnPermissionCheck) -> Unit,
    navigateToHelp: () -> Unit
) {
    AnimatedVisibility(
        visible = isError,
        modifier = Modifier.align(Alignment.Center),
        enter = Transitions.DefaultTransitionIn,
        exit = Transitions.NoExitAnimation
    ) {
        ErrorPlaceholder(
            modifier = Modifier.align(Alignment.Center),
            errorMessage = stringResource(id = R.string.error_permission),
            icon = painterResource(id = R.drawable.error),
            actionTitle = stringResource(id = R.string.grant_permission)
        ) {
            permissionCheck(
                BrowseEvent.OnPermissionCheck(
                    storagePermissionState = storagePermissionState
                )
            )
        }
    }

    AnimatedVisibility(
        visible = !isLoading
                && dialogHidden
                && filesEmpty
                && !isError
                && !isRefreshing,
        modifier = Modifier.align(Alignment.Center),
        enter = Transitions.DefaultTransitionIn,
        exit = Transitions.NoExitAnimation
    ) {
        EmptyPlaceholder(
            message = stringResource(id = R.string.browse_empty),
            icon = painterResource(id = R.drawable.empty_browse),
            actionTitle = stringResource(id = R.string.get_help),
            action = {
                navigateToHelp()
            }
        )
    }
}