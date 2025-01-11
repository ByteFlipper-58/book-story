package com.byteflipper.everbook.presentation.book_info

import android.net.Uri
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.byteflipper.everbook.presentation.core.components.common.AsyncCoverImage

@Composable
fun BookInfoLayoutBackground(height: Dp, image: Uri) {
    val background = MaterialTheme.colorScheme.surface

    AsyncCoverImage(
        uri = image,
        animationDurationMillis = 300,
        contentDescription = null,
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .drawWithContent {
                drawContent()
                drawRect(
                    brush = Brush.verticalGradient(
                        0f to Color.Transparent,
                        0.8f to background
                    )
                )
            }
            .blur(3.dp),
        alpha = 0.4f
    )
}