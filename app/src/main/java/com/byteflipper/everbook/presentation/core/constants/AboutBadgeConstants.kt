/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.core.constants

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import com.byteflipper.everbook.R
import com.byteflipper.everbook.domain.about.Badge

fun provideAboutBadges() = listOf(
    Badge(
        id = "x",
        drawable = R.drawable.x_logo,
        imageVector = null,
        contentDescription = R.string.x_content_desc,
        url = "https://www.x.com/byteflipper"
    ),
    Badge(
        id = "vk",
        drawable = R.drawable.vk_24,
        imageVector = null,
        contentDescription = R.string.vk_content_desc,
        url = "https://vk.com/byteflipper/"
    ),
    Badge(
        id = "byteflipper",
        drawable = R.drawable.byteflipper_logo,
        imageVector = null,
        contentDescription = R.string.byteflipper_site_content_desc,
        url = "https://byteflipper.com"
    ),
    Badge(
        id = "telegram",
        drawable = R.drawable.telegram_24,
        imageVector = null,
        contentDescription = R.string.telegram_chennel_content_desc,
        url = "https://t.me/ByteFlipper"
    ),
    Badge(
        id = "github_profile",
        drawable = null,
        imageVector = Icons.Default.Person,
        contentDescription = R.string.github_profile_content_desc,
        url = "https://www.github.com/ByteFlipper-58"
    ),
)