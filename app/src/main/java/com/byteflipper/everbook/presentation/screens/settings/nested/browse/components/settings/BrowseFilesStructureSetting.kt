package com.byteflipper.everbook.presentation.screens.settings.nested.browse.components.settings

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.byteflipper.everbook.R
import com.byteflipper.everbook.domain.model.ButtonItem
import com.byteflipper.everbook.presentation.core.components.settings.SegmentedButtonWithTitle
import com.byteflipper.everbook.presentation.data.MainEvent
import com.byteflipper.everbook.presentation.data.MainViewModel
import com.byteflipper.everbook.presentation.screens.settings.nested.browse.data.BrowseFilesStructure

/**
 * Browse Files Structure setting.
 * Lets user choose between all and folders structure for Browse.
 */
@Composable
fun BrowseFilesStructureSetting() {
    val state = MainViewModel.getState()
    val onMainEvent = MainViewModel.getEvent()

    SegmentedButtonWithTitle(
        title = stringResource(id = R.string.browse_files_structure_option),
        buttons = BrowseFilesStructure.entries.map {
            ButtonItem(
                it.toString(),
                when (it) {
                    BrowseFilesStructure.ALL_FILES -> stringResource(id = R.string.files_structure_all)
                    BrowseFilesStructure.DIRECTORIES -> stringResource(id = R.string.files_structure_directory)
                },
                MaterialTheme.typography.labelLarge,
                it == state.value.browseFilesStructure
            )
        }
    ) {
        onMainEvent(
            MainEvent.OnChangeBrowseFilesStructure(
                it.id
            )
        )
    }
}