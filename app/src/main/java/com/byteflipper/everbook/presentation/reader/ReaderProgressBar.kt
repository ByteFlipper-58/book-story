package com.byteflipper.everbook.presentation.reader

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import com.byteflipper.everbook.domain.util.HorizontalAlignment

@Composable
fun ReaderProgressBar(
    progress: String,
    progressBarPadding: Dp,
    progressBarAlignment: HorizontalAlignment,
    progressBarFontSize: TextUnit,
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
        contentAlignment = progressBarAlignment.alignment
    ) {
        DisableSelection {
            Text(
                text = progress,
                color = fontColor,
                fontSize = progressBarFontSize,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}