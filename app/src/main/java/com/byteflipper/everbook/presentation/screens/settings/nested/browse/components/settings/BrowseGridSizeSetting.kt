package com.byteflipper.everbook.presentation.screens.settings.nested.browse.components.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.byteflipper.everbook.R
import com.byteflipper.everbook.presentation.core.components.settings.SliderWithTitle
import com.byteflipper.everbook.presentation.data.MainEvent
import com.byteflipper.everbook.presentation.data.MainViewModel
import com.byteflipper.everbook.presentation.screens.settings.nested.browse.data.BrowseLayout
import com.byteflipper.everbook.presentation.ui.ExpandingTransition

/**
 * Browse Grid Size setting.
 * Lets user specify grid size to be displayed.
 */
@Composable
fun BrowseGridSizeSetting() {
    val state = MainViewModel.getState()
    val onMainEvent = MainViewModel.getEvent()

    ExpandingTransition(visible = state.value.browseLayout == BrowseLayout.GRID) {
        SliderWithTitle(
            value = state.value.browseGridSize
                    to " ${stringResource(R.string.browse_grid_size_per_row)}",
            valuePlaceholder = stringResource(id = R.string.browse_grid_size_auto),
            showPlaceholder = state.value.browseAutoGridSize,
            fromValue = 0,
            toValue = 10,
            title = stringResource(id = R.string.browse_grid_size_option),
            onValueChange = {
                onMainEvent(MainEvent.OnChangeBrowseAutoGridSize(it == 0))
                onMainEvent(
                    MainEvent.OnChangeBrowseGridSize(it)
                )
            }
        )
    }
}