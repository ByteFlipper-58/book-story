package com.byteflipper.everbook.presentation.start

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.byteflipper.everbook.presentation.core.components.common.LazyColumnWithScrollbar

@Composable
fun StartSettingsLayout(
    content: LazyListScope.() -> Unit
) {
    LazyColumnWithScrollbar(Modifier.fillMaxSize()) {
        content()
    }
}