/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.about

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.byteflipper.everbook.R
import com.byteflipper.everbook.presentation.core.constants.Constants
import com.byteflipper.everbook.presentation.core.constants.provideAboutBadges
import com.byteflipper.everbook.presentation.core.util.showToast
import com.byteflipper.everbook.ui.about.AboutEvent

@Composable
fun AboutBadges(
    navigateToBrowserPage: (AboutEvent.OnNavigateToBrowserPage) -> Unit
) {
    val context = LocalContext.current

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        items(
            Constants.provideAboutBadges(),
            key = { it.id }
        ) { badge ->
            AboutBadgeItem(badge = badge) {
                when (badge.id) {
                    "tryzub" -> {
                        context.getString(R.string.slava_ukraini)
                            .showToast(context = context, longToast = false)
                    }

                    else -> {
                        badge.url?.let {
                            navigateToBrowserPage(
                                AboutEvent.OnNavigateToBrowserPage(
                                    page = it,
                                    context = context
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}