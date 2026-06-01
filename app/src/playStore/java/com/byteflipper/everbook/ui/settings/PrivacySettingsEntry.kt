/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.ui.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.byteflipper.everbook.R
import com.byteflipper.everbook.presentation.settings.SettingsLayoutItem

@Composable
fun PrivacySettingsEntry(
    index: Int,
    navigateToPrivacySettings: () -> Unit
) {
    SettingsLayoutItem(
        index = index,
        icon = Icons.Outlined.PrivacyTip,
        title = stringResource(id = R.string.privacy_settings),
        description = stringResource(id = R.string.privacy_settings_desc),
        onClick = navigateToPrivacySettings
    )
}
