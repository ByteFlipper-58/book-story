package com.byteflipper.everbook.domain.about

import androidx.compose.runtime.Immutable
import com.byteflipper.everbook.domain.ui.UIText

@Immutable
data class Credit(
    val name: String,
    val source: String?,
    val credits: List<UIText>,
    val website: String?
)