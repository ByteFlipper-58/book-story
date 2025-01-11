package com.byteflipper.everbook.domain.browse

import androidx.compose.runtime.Immutable
import com.byteflipper.everbook.domain.ui.UIText
import java.io.File

@Immutable
data class FileWithTitle(
    val title: UIText,
    val file: File
)