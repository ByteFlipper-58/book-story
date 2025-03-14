package com.byteflipper.everbook.domain.browse.file

import androidx.compose.runtime.Immutable
import com.byteflipper.everbook.domain.browse.SelectableFile

@Immutable
data class GroupedFiles(
    val header: String,
    val pinned: Boolean,
    val files: List<SelectableFile>
)