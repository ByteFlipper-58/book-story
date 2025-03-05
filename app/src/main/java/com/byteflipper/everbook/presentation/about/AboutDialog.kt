/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.about

import androidx.compose.runtime.Composable
import com.byteflipper.everbook.data.remote.dto.LatestReleaseInfo
import com.byteflipper.everbook.domain.util.Dialog
import com.byteflipper.everbook.ui.about.AboutEvent
import com.byteflipper.everbook.ui.about.AboutScreen

@Composable
fun AboutDialog(
    dialog: Dialog? = null,
    updateInfo: LatestReleaseInfo?,
    navigateToBrowserPage: (AboutEvent.OnNavigateToBrowserPage) -> Unit,
    dismissDialog: (AboutEvent.OnDismissDialog) -> Unit
) {
    when (dialog) {
        AboutScreen.UPDATE_DIALOG -> {
            AboutUpdateDialog(
                updateInfo = updateInfo,
                navigateToBrowserPage = navigateToBrowserPage,
                dismissDialog = dismissDialog
            )
        }
    }
}