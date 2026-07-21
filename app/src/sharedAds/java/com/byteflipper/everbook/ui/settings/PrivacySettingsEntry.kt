/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.ui.settings

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
        icon = R.drawable.ic_privacy_tip_rounded_24px,
        title = stringResource(id = R.string.privacy_settings),
        description = stringResource(id = R.string.privacy_settings_desc),
        onClick = navigateToPrivacySettings
    )
}
