/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.changelog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.byteflipper.everbook.R
import com.byteflipper.everbook.domain.util.Position
import com.byteflipper.everbook.domain.changelog.ChangelogBlock
import com.byteflipper.everbook.domain.changelog.ChangelogPage
import com.byteflipper.everbook.domain.changelog.ChangelogRelease
import com.byteflipper.everbook.presentation.core.components.common.LazyColumnWithScrollbar
import com.byteflipper.everbook.presentation.core.components.common.StyledText
import com.byteflipper.everbook.presentation.core.constants.provideLanguages
import com.byteflipper.everbook.ui.changelog.ChangelogEvent
import com.byteflipper.everbook.ui.changelog.ChangelogState
import java.util.Locale

@Composable
fun ChangelogLayout(
    state: ChangelogState,
    paddingValues: PaddingValues,
    listState: LazyListState,
    onEvent: (ChangelogEvent) -> Unit,
    navigateBack: () -> Unit
) {
    when {
        state.isLoading -> ChangelogLoading(paddingValues)
        state.selectedRelease == null -> ChangelogEmpty(
            paddingValues = paddingValues,
            listState = listState
        )

        else -> ChangelogReleaseLayout(
            paddingValues = paddingValues,
            listState = listState,
            release = state.selectedRelease,
            onEvent = onEvent,
            navigateBack = navigateBack
        )
    }
}

@Composable
private fun ChangelogLoading(paddingValues: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ChangelogEmpty(
    paddingValues: PaddingValues,
    listState: LazyListState
) {
    LazyColumnWithScrollbar(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = paddingValues.calculateTopPadding()),
        state = listState,
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 40.dp)
    ) {
        item {
            StyledText(
                text = stringResource(id = R.string.changelog_empty),
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

@Composable
private fun ChangelogReleaseLayout(
    paddingValues: PaddingValues,
    listState: LazyListState,
    release: ChangelogRelease,
    onEvent: (ChangelogEvent) -> Unit,
    navigateBack: () -> Unit
) {
    val pages = release.pages

    LaunchedEffect(release.versionCode, release.locale) {
        listState.scrollToItem(0)
    }

    LazyColumnWithScrollbar(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = paddingValues.calculateTopPadding()),
        state = listState,
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        itemsIndexed(
            pages,
            key = { index, page -> "${release.versionCode}-$index-${page.title}" }
        ) { index, page ->
            ChangelogPageItem(
                index = index,
                page = page,
                position = when (index) {
                    0 -> if (pages.size == 1) Position.SOLO else Position.TOP
                    pages.lastIndex -> Position.BOTTOM
                    else -> Position.CENTER
                }
            )
        }

        item {
            ChangelogLanguageSelector(
                release = release,
                onEvent = onEvent
            )
        }

        item {
            ChangelogDoneButton(navigateBack = navigateBack)
        }
    }
}

@Composable
private fun ChangelogLanguageSelector(
    release: ChangelogRelease,
    onEvent: (ChangelogEvent) -> Unit
) {
    if (release.availableLocales.size <= 1) return

    val languages = remember {
        provideLanguages().toMap()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 14.dp, bottom = 2.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StyledText(
            text = stringResource(id = R.string.changelog_language_title),
            style = MaterialTheme.typography.labelLarge.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            maxLines = 1
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(end = 2.dp)
        ) {
            items(release.availableLocales, key = { it }) { language ->
                FilterChip(
                    selected = language == release.locale,
                    onClick = {
                        onEvent(ChangelogEvent.OnSelectLanguage(language))
                    },
                    label = {
                        StyledText(
                            text = languages[language] ?: language.uppercase(Locale.ROOT),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun ChangelogPageItem(
    index: Int,
    page: ChangelogPage,
    position: Position
) {
    val extraLargeShape = MaterialTheme.shapes.extraLarge
    val shape = remember(position) {
        when (position) {
            Position.TOP -> extraLargeShape.copy(
                bottomStart = CornerSize(3.dp),
                bottomEnd = CornerSize(3.dp)
            )

            Position.CENTER -> RoundedCornerShape(3.dp)
            Position.SOLO -> extraLargeShape
            Position.BOTTOM -> extraLargeShape.copy(
                topStart = CornerSize(3.dp),
                topEnd = CornerSize(3.dp)
            )
        }
    }
    val paddingValues = remember(position) {
        when (position) {
            Position.TOP -> PaddingValues(top = 4.dp, bottom = 1.dp)
            Position.CENTER -> PaddingValues(vertical = 1.dp)
            Position.SOLO -> PaddingValues(vertical = 4.dp)
            Position.BOTTOM -> PaddingValues(bottom = 4.dp, top = 1.dp)
        }
    }

    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(paddingValues)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(horizontal = 18.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    .padding(horizontal = 9.dp, vertical = 6.dp)
            ) {
                StyledText(
                    text = (index + 1).toString().padStart(2, '0'),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                )
            }

            StyledText(
                text = page.title,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 28.sp
                )
            )
        }

        page.blocks.forEach { block ->
            ChangelogBlockItem(block = block)
        }
    }
}

@Composable
private fun ChangelogBlockItem(block: ChangelogBlock) {
    when (block) {
        is ChangelogBlock.Paragraph -> {
            StyledText(
                text = block.text,
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 26.sp
                )
            )
        }

        is ChangelogBlock.BulletList -> {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                block.items.forEach { bullet ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(top = 9.dp)
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.secondary)
                        )
                        StyledText(
                            text = bullet,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 23.sp
                            )
                        )
                    }
                }
            }
        }

        is ChangelogBlock.Heading -> {
            StyledText(
                text = block.text,
                style = MaterialTheme.typography.titleMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium
                )
            )
        }

        is ChangelogBlock.Quote -> {
            StyledText(
                text = block.text,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.large)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    .padding(14.dp),
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 23.sp
                )
            )
        }
    }
}

@Composable
private fun ChangelogDoneButton(
    navigateBack: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    ) {
        Button(onClick = navigateBack) {
            StyledText(text = stringResource(id = R.string.done))
        }
    }

    Spacer(modifier = Modifier.height(4.dp))
}
