package com.byteflipper.everbook.domain.history

import androidx.compose.runtime.Immutable

@Immutable
data class GroupedHistory(
    val title: String,
    val history: List<History>
)
