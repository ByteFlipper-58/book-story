/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.reader

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import com.byteflipper.everbook.domain.reader.ReaderText
import com.byteflipper.everbook.domain.util.HorizontalAlignment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun LazyItemScope.ReaderLayoutTextImage(
    entry: ReaderText.Image,
    sidePadding: Dp,
    imagesCornersRoundness: Dp,
    imagesAlignment: HorizontalAlignment,
    imagesWidth: Float,
    imagesColorEffects: ColorFilter?
) {
    var lazyBitmap by remember(entry.imagePath, entry.imageBitmap) {
        mutableStateOf<ImageBitmap?>(entry.imageBitmap)
    }

    LaunchedEffect(entry.imagePath, entry.imageBitmap) {
        val path = entry.imagePath ?: return@LaunchedEffect
        if (lazyBitmap != null) return@LaunchedEffect
        lazyBitmap = withContext(Dispatchers.IO) {
            BitmapFactory.decodeFile(
                path,
                BitmapFactory.Options().apply {
                    inPreferredConfig = Bitmap.Config.RGB_565
                }
            )?.asImageBitmap()
        }
    }

    val imageBitmap = lazyBitmap ?: return

    Box(
        modifier = Modifier
            .animateItem(
                fadeInSpec = null,
                fadeOutSpec = null
            )
            .padding(horizontal = sidePadding)
            .fillMaxWidth(),
        contentAlignment = imagesAlignment.alignment
    ) {
        Image(
            modifier = Modifier
                .clip(RoundedCornerShape(imagesCornersRoundness))
                .fillMaxWidth(imagesWidth),
            bitmap = imageBitmap,
            contentDescription = null,
            colorFilter = imagesColorEffects,
            contentScale = ContentScale.FillWidth
        )
    }
}
