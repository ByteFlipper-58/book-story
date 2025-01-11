package com.byteflipper.everbook.presentation.settings.appearance

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.byteflipper.everbook.presentation.core.components.common.LazyColumnWithScrollbar

@Composable
fun AppearanceSettingsLayout(
    listState: LazyListState,
    paddingValues: PaddingValues
) {
    LazyColumnWithScrollbar(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = paddingValues.calculateTopPadding()),
        state = listState
    ) {
        AppearanceSettingsCategory()
    }
}