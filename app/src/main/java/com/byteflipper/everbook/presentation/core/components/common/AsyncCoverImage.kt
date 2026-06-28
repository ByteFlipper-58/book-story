/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.core.components.common

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.byteflipper.everbook.R

/**
 * Custom Cover Image. Has smooth appearing.
 */
@Composable
fun AsyncCoverImage(
    uri: Uri,
    animationDurationMillis: Int = 100,
    contentDescription: String? = stringResource(id = R.string.cover_image_content_desc),
    modifier: Modifier,
    alpha: Float = 1f,
) {
    val context = LocalContext.current
    // Build the request once per uri. Otherwise every recomposition (e.g. the burst of grid items
    // composed when a pager page scrolls into view) allocates a fresh builder. A stable memory-cache
    // key also guarantees instant cache hits when the same cover is shown again.
    val request = remember(uri) {
        ImageRequest.Builder(context)
            .data(uri)
            .memoryCacheKey(uri.toString())
            .crossfade(animationDurationMillis)
            .build()
    }

    AsyncImage(
        model = request,
        contentDescription = contentDescription,
        modifier = modifier,
        alpha = alpha,
        contentScale = ContentScale.Crop
    )
}