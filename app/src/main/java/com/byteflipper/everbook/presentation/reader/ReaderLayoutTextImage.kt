package com.byteflipper.everbook.presentation.reader

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import com.byteflipper.everbook.domain.reader.ReaderImagesAlignment
import com.byteflipper.everbook.domain.reader.ReaderText

@Composable
fun LazyItemScope.ReaderLayoutTextImage(
    entry: ReaderText.Image,
    sidePadding: Dp,
    imagesCornersRoundness: Dp,
    imagesAlignment: ReaderImagesAlignment,
    imagesWidth: Float
) {
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
            bitmap = entry.imageBitmap,
            contentDescription = null,
            contentScale = ContentScale.FillWidth
        )
    }
}