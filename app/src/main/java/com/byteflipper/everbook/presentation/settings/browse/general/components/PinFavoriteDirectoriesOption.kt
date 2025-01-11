package com.byteflipper.everbook.presentation.settings.browse.general.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.byteflipper.everbook.R
import com.byteflipper.everbook.domain.browse.BrowseFilesStructure
import com.byteflipper.everbook.presentation.core.components.settings.SwitchWithTitle
import com.byteflipper.everbook.ui.main.MainEvent
import com.byteflipper.everbook.ui.main.MainModel
import com.byteflipper.everbook.ui.theme.ExpandingTransition

@Composable
fun PinFavoriteDirectoriesOption() {
    val mainModel = hiltViewModel<MainModel>()
    val state = mainModel.state.collectAsStateWithLifecycle()

    ExpandingTransition(visible = state.value.browseFilesStructure == BrowseFilesStructure.DIRECTORIES) {
        SwitchWithTitle(
            selected = state.value.browsePinFavoriteDirectories,
            title = stringResource(id = R.string.browse_pin_favorite_directories_option),
            description = stringResource(id = R.string.browse_pin_favorite_directories_option_desc)
        ) {
            mainModel.onEvent(
                MainEvent.OnChangeBrowsePinFavoriteDirectories(
                    !state.value.browsePinFavoriteDirectories
                )
            )
        }
    }
}