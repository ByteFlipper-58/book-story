/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.about

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.byteflipper.everbook.R
import com.byteflipper.everbook.presentation.core.components.common.LazyColumnWithScrollbar
import com.byteflipper.everbook.presentation.core.constants.provideContributorsPage
import com.byteflipper.everbook.presentation.core.constants.provideIssuesPage
import com.byteflipper.everbook.presentation.core.constants.provideReleasesPage
import com.byteflipper.everbook.presentation.core.constants.provideSupportPage
import com.byteflipper.everbook.presentation.core.constants.provideTranslationPage
import com.byteflipper.everbook.ui.about.AboutEvent

@Composable
fun AboutLayout(
    paddingValues: PaddingValues,
    listState: LazyListState,
    navigateToBrowserPage: (AboutEvent.OnNavigateToBrowserPage) -> Unit,
    navigateToLicenses: () -> Unit,
    navigateToCredits: () -> Unit
) {
    val context = LocalContext.current

    LazyColumnWithScrollbar(
        Modifier
            .fillMaxSize()
            .padding(top = paddingValues.calculateTopPadding()),
        state = listState
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painterResource(id = R.drawable.app_icon),
                    contentDescription = stringResource(id = R.string.app_icon_content_desc),
                    modifier = Modifier
                        .padding(14.dp)
                        .size(120.dp),
                    tint = MaterialTheme.colorScheme.secondary
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp)) // Reduced spacer
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AboutItem(
                        title = stringResource(id = R.string.app_version_option),
                        description = "Book's Story v${stringResource(id = R.string.app_version)}",
                    ) {
                        navigateToBrowserPage(
                            AboutEvent.OnNavigateToBrowserPage(
                                page = provideReleasesPage(),
                                context = context
                            )
                        )
                    }

                    AboutItem(
                        title = stringResource(id = R.string.report_bug_option),
                        description = null
                    ) {
                        navigateToBrowserPage(
                            AboutEvent.OnNavigateToBrowserPage(
                                page = provideIssuesPage(),
                                context = context
                            )
                        )
                    }

                    AboutItem(
                        title = stringResource(id = R.string.contributors_option),
                        description = null
                    ) {
                        navigateToBrowserPage(
                            AboutEvent.OnNavigateToBrowserPage(
                                page = provideContributorsPage(),
                                context = context
                            )
                        )
                    }

                    AboutItem(
                        title = stringResource(id = R.string.licenses_option),
                        description = null
                    ) {
                        navigateToLicenses()
                    }

                    AboutItem(
                        title = stringResource(id = R.string.credits_option),
                        description = null
                    ) {
                        navigateToCredits()
                    }

                    AboutItem(
                        title = stringResource(id = R.string.help_translate_option),
                        description = null
                    ) {
                        navigateToBrowserPage(
                            AboutEvent.OnNavigateToBrowserPage(
                                page = provideTranslationPage(),
                                context = context
                            )
                        )
                    }

                    AboutItem(
                        title = stringResource(id = R.string.support_development_option),
                        description = null
                    ) {
                        navigateToBrowserPage(
                            AboutEvent.OnNavigateToBrowserPage(
                                page = provideSupportPage(),
                                context = context
                            )
                        )
                    }

                    AboutBadges(
                        navigateToBrowserPage = navigateToBrowserPage
                    )
                }
            }
        }
    }
}