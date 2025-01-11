package com.byteflipper.everbook.ui.history

import androidx.compose.runtime.Immutable
import com.byteflipper.everbook.domain.history.GroupedHistory
import com.byteflipper.everbook.domain.util.Dialog

@Immutable
data class HistoryState(
    val history: List<GroupedHistory> = emptyList(),

    val isRefreshing: Boolean = false,
    val isLoading: Boolean = true,

    val showSearch: Boolean = false,
    val searchQuery: String = "",
    val hasFocused: Boolean = false,

    val dialog: Dialog? = null
)