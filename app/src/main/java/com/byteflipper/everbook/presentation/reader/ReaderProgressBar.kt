package com.byteflipper.everbook.presentation.reader

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp

@Composable
fun ReaderProgressBar(
    progress: String,
    progressBarPadding: Dp,
    fontColor: Color,
    sidePadding: Dp
) {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(
                horizontal = sidePadding,
                vertical = progressBarPadding
            ),
        contentAlignment = Alignment.Center
    ) {
        DisableSelection {
            Text(
                progress,
                style = MaterialTheme.typography.bodyLarge,
                color = fontColor,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}