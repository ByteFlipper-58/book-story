package com.byteflipper.everbook.domain.browse

import androidx.compose.runtime.Immutable

@Immutable
data class SelectableFile(
    val name: String,
    val path: String,
    val size: Long,
    val lastModified: Long,
    val selected: Boolean
)