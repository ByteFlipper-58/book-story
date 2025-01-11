package com.byteflipper.everbook.domain.browse

import androidx.compose.runtime.Immutable
import com.byteflipper.everbook.domain.util.Selected
import java.io.File

@Immutable
data class SelectableFile(
    val fileOrDirectory: File,
    val parentDirectory: File,
    val isDirectory: Boolean,
    val isFavorite: Boolean,
    val isSelected: Selected
)