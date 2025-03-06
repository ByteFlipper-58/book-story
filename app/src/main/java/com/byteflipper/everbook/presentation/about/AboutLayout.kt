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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import com.byteflipper.everbook.R
import com.byteflipper.everbook.presentation.core.components.common.LazyColumnWithScrollbar
import com.byteflipper.everbook.presentation.core.constants.Constants
import com.byteflipper.everbook.presentation.core.constants.provideContributorsPage
import com.byteflipper.everbook.presentation.core.constants.provideIssuesPage
import com.byteflipper.everbook.presentation.core.constants.provideReleasesPage
import com.byteflipper.everbook.presentation.core.constants.provideTranslationPage
import com.byteflipper.everbook.ui.about.AboutEvent

@OptIn(ExperimentalMaterial3Api::class)
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
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painterResource(id = R.drawable.app_icon),
                    contentDescription = stringResource(id = R.string.app_icon_content_desc),
                    modifier = Modifier
                        .padding(horizontal = 14.dp)
                        .padding(
                            top = 40.dp,
                            bottom = 50.dp
                        )
                        .size(180.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(
                        MaterialTheme.shapes.extraLarge.copy(
                            bottomStart = CornerSize(0),
                            bottomEnd = CornerSize(0)
                        )
                    )
                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                    .padding(24.dp)
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AboutItem(
                    title = stringResource(id = R.string.app_version_option),
                    description = buildAnnotatedString {
                        append(
                            stringResource(
                                id = R.string.app_version_option_desc_1,
                                context.getString(R.string.app_version)
                            )
                        )
                    },
                    isOnClickEnabled = false
                )

                AboutItem(
                    title = stringResource(id = R.string.report_bug_option),
                    description = null
                ) {
                    navigateToBrowserPage(
                        AboutEvent.OnNavigateToBrowserPage(
                            page = Constants.provideIssuesPage(),
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
                            page = Constants.provideContributorsPage(),
                            context = context
                        )
                    )
                }

                AboutItem(
                    title = stringResource(id = R.string.whats_new_option),
                    description = null
                ) {
                    navigateToBrowserPage(
                        AboutEvent.OnNavigateToBrowserPage(
                            page = Constants.provideReleasesPage(),
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
                            page = Constants.provideTranslationPage(),
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